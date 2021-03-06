package com.pacbio.secondary.smrtserver.tools

import com.pacbio.secondary.smrtserver.client.{AnalysisClientJsonProtocol, AnalysisServiceAccessLayer}
import com.pacbio.secondary.analysis.tools._
import com.pacbio.secondary.analysis.pipelines._
import com.pacbio.secondary.analysis.jobs.JobModels._
import com.pacbio.secondary.analysis.converters._
import com.pacbio.secondary.analysis.constants.FileTypes
import com.pacbio.secondary.smrtlink.client.ClientUtils
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.common.models._
import akka.actor.ActorSystem
import org.joda.time.DateTime
import scopt.OptionParser
import com.typesafe.scalalogging.LazyLogging
import spray.httpx
import spray.json._
import spray.httpx.SprayJsonSupport

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Properties, Success, Try}
import scala.util.matching.Regex
import scala.xml.XML
import scala.io.Source
import scala.math._
import java.net.URL
import java.util.UUID
import java.io.{File, FileReader}
import java.nio.file.{Path, Paths}

import com.pacbio.common.services.PacBioServiceErrors.{ResourceNotFoundError, UnprocessableEntityError}
import com.pacbio.logging.{LoggerConfig, LoggerOptions}


object Modes {
  sealed trait Mode {
    val name: String
  }
  case object STATUS extends Mode {val name = "status"}
  case object IMPORT_DS extends Mode {val name = "import-dataset"}
  case object IMPORT_FASTA extends Mode {val name = "import-fasta"}
  case object IMPORT_BARCODES extends Mode {val name = "import-barcodes"}
  case object ANALYSIS extends Mode {val name = "run-analysis"}
  case object TEMPLATE extends Mode {val name = "emit-analysis-template"}
  case object PIPELINE extends Mode {val name = "run-pipeline"}
  case object SHOW_PIPELINES extends Mode {val name = "show-pipelines"}
  case object IMPORT_MOVIE extends Mode {val name = "import-rs-movie"}
  case object JOB extends Mode {val name = "get-job"}
  case object JOBS extends Mode {val name = "get-jobs"}
  case object TERMINATE_JOB extends Mode { val name = "terminate-job"} // This currently ONLY supports Analysis Jobs
  case object DELETE_JOB extends Mode { val name = "delete-job" } // also only analysis jobs
  case object DATASET extends Mode {val name = "get-dataset"}
  case object DATASETS extends Mode {val name = "get-datasets"}
  case object DELETE_DATASET extends Mode {val name = "delete-dataset"}
  case object CREATE_PROJECT extends Mode {val name = "create-project"}
  case object MANIFESTS extends Mode {val name = "get-manifests"}
  case object MANIFEST extends Mode {val name = "get-manifest"}
  case object UNKNOWN extends Mode {val name = "unknown"}
}

object PbServiceParser extends CommandLineToolVersion{
  import CommonModels._
  import CommonModelImplicits._

  val VERSION = "0.1.0"
  var TOOL_ID = "pbscala.tools.pbservice"

  private def getSizeMb(fileObj: File): Double = {
    fileObj.length / 1024.0 / 1024.0
  }

  def showDefaults(c: CustomConfig): Unit = {
    println(s"Defaults $c")
  }

  def showVersion: Unit = showToolVersion(TOOL_ID, VERSION)

  // is there a cleaner way to do this?
  private def entityIdOrUuid(entityId: String): IdAble = {
    try {
      IntIdAble(entityId.toInt)
    } catch {
      case e: Exception => {
        try {
          UUIDIdAble(UUID.fromString(entityId))
        } catch {
          case e: Exception => 0
        }
      }
    }
  }

  private def getToken(token: String): String = {
    if (Paths.get(token).toFile.isFile) {
      Source.fromFile(token).getLines.take(1).toList.head
    } else token
  }

  case class CustomConfig(
      mode: Modes.Mode = Modes.UNKNOWN,
      host: String,
      port: Int,
      block: Boolean = false,
      command: CustomConfig => Unit = showDefaults,
      datasetId: IdAble = 0,
      jobId: IdAble = 0,
      path: Path = null,
      name: String = "",
      organism: String = "",
      ploidy: String = "",
      maxItems: Int = 25,
      datasetType: String = "subreads",
      nonLocal: Option[String] = None,
      asJson: Boolean = false,
      dumpJobSettings: Boolean = false,
      pipelineId: String = "",
      jobTitle: String = "",
      entryPoints: Seq[String] = Seq(),
      presetXml: Option[Path] = None,
      maxTime: Int = -1,
      project: Option[String] = None,
      description: String = "",
      authToken: Option[String] = Properties.envOrNone("PB_SERVICE_AUTH_TOKEN"),
      manifestId: String = "smrtlink",
      showReports: Boolean = false,
      searchName: Option[String] = None,
      searchPath: Option[String] = None
  ) extends LoggerConfig


  lazy val defaultHost: String = Properties.envOrElse("PB_SERVICE_HOST", "localhost")
  lazy val defaultPort: Int = Properties.envOrElse("PB_SERVICE_PORT", "8070").toInt
  lazy val defaults = CustomConfig(null, defaultHost, defaultPort, maxTime=1800)

  lazy val parser = new OptionParser[CustomConfig]("pbservice") {

    private def validateId(entityId: String, entityType: String): Either[String, Unit] = {
      entityIdOrUuid(entityId) match {
        case IntIdAble(x) => if (x > 0) success else failure(s"${entityType} ID must be a positive integer or a UUID string")
        case UUIDIdAble(x) => success
      }
    }

    head("PacBio SMRTLink Services Client", VERSION)

    opt[String]("host") action { (x, c) =>
      c.copy(host = x)
    } text s"Hostname of smrtlink server (default: $defaultHost).  Override the default with env PB_SERVICE_HOST."

    opt[Int]("port") action { (x, c) =>
      c.copy(port = x)
    } text s"Services port on smrtlink server (default: $defaultPort).  Override default with env PB_SERVICE_PORT."

    // FIXME(nechols)(2016-09-21) disabled due to WSO2, will revisit later
    /*opt[String]("token") action { (t, c) =>
      c.copy(authToken = Some(getToken(t)))
    } text "Authentication token (required for project services)"*/

    opt[Unit]("json") action { (_, c) =>
      c.copy(asJson = true)
    } text "Display output as raw JSON"

    opt[Unit]("debug") action { (_, c) =>
      c.asInstanceOf[LoggerConfig].configure(c.logbackFile, c.logFile, true, c.logLevel).asInstanceOf[CustomConfig]
    } text "Display debugging log output"

    LoggerOptions.add(this.asInstanceOf[OptionParser[LoggerConfig]])

    cmd(Modes.STATUS.name) action { (_, c) =>
      c.copy(command = (c) => println("with " + c), mode = Modes.STATUS)
    }

    cmd(Modes.IMPORT_DS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_DS)
    } children(
      arg[File]("dataset-path") required() action { (p, c) =>
        c.copy(path = p.toPath)
      } text "DataSet XML path (or directory containing datasets)",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t)
      } text "Maximum time to poll for running job status",
      opt[String]("non-local") action { (t, c) =>
        c.copy(nonLocal = Some(t))
      } text "Import non-local dataset with specified type (e.g. PacBio.DataSet.SubreadSet)" /*,
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with this dataset"*/
    ) text "Import DataSet XML"

    cmd(Modes.IMPORT_FASTA.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_FASTA)
    } children(
      arg[File]("fasta-path") required() action { (p, c) =>
        c.copy(path = p.toPath)
      } text "FASTA path",
      opt[String]("name") action { (name, c) =>
        c.copy(name = name) // do we need to check that this is non-blank?
      } text "Name of ReferenceSet",
      opt[String]("organism") action { (organism, c) =>
        c.copy(organism = organism)
      } text "Organism",
      opt[String]("ploidy") action { (ploidy, c) =>
        c.copy(ploidy = ploidy)
      } text "Ploidy",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t)
      } text "Maximum time to poll for running job status" /*,
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with this reference" */
    ) text "Import Reference FASTA"

    cmd(Modes.IMPORT_BARCODES.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_BARCODES)
    } children(
      arg[File]("fasta-path") required() action { (p, c) =>
        c.copy(path = p.toPath)
      } text "FASTA path",
      arg[String]("name") required() action { (name, c) =>
        c.copy(name = name)
      } text "Name of BarcodeSet" /*,
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with these barcodes" */
    ) text "Import Barcodes FASTA"

    cmd(Modes.IMPORT_MOVIE.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_MOVIE)
    } children(
      arg[File]("metadata-xml-path") required() action { (p, c) =>
        c.copy(path = p.toPath)
      } text "Path to RS II movie metadata XML file (or directory)",
      opt[String]("name") action { (name, c) =>
        c.copy(name = name)
      } text "Name of imported HdfSubreadSet" /*,
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with this dataset"*/
    ) text "Import RS II movie metadata XML legacy format as HdfSubreadSet"

    cmd(Modes.ANALYSIS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.ANALYSIS)
    } children(
      arg[File]("json-file") required() action { (p, c) =>
        c.copy(path = p.toPath)
      } text "JSON config file", // TODO validate json format
      opt[Unit]("block") action { (_, c) =>
        c.copy(block = true)
      } text "Block until job completes",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t)
      } text "Maximum time to poll for running job status"
    ) text "Run a pbsmrtpipe analysis pipeline from a JSON config file"

    cmd(Modes.TERMINATE_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.TERMINATE_JOB)
    } children(
        arg[Int]("job-id") required() action { (p, c) =>
          c.copy(jobId = p)
        } text "SMRT Link Analysis Job Id"
        ) text "Terminate a SMRT Link Analysis Job By Int Id in the RUNNING state"

    cmd(Modes.DELETE_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DELETE_JOB)
    } children(
      arg[String]("job-id") required() action { (i, c) =>
        c.copy(jobId = entityIdOrUuid(i))
      } validate { i => validateId(i, "Job") } text "Job ID"
    ) text "Delete a pbsmrtpipe job, including all output files"

    cmd(Modes.TEMPLATE.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.TEMPLATE)
    } children(
    ) text "Emit an analysis.json template to stdout that can be run using 'run-analysis'"

    cmd(Modes.SHOW_PIPELINES.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.SHOW_PIPELINES)
    } text "Display a list of available pbsmrtpipe pipelines"

    cmd(Modes.PIPELINE.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.PIPELINE)
    } children(
      arg[String]("pipeline-id") required() action { (p, c) =>
        c.copy(pipelineId = p)
      } text "Pipeline ID to run",
      opt[String]('e', "entry-point") minOccurs(1) maxOccurs(1024) action { (e, c) =>
        c.copy(entryPoints = c.entryPoints :+ e)
      } text "Entry point (must be valid PacBio DataSet)",
      opt[File]("preset-xml") action { (x, c) =>
        c.copy(presetXml = Some(x.toPath))
      } text "XML file specifying pbsmrtpipe options",
      opt[String]("job-title") action { (t, c) =>
        c.copy(jobTitle = t)
      } text "Job title (will be displayed in UI)",
      opt[Unit]("block") action { (_, c) =>
        c.copy(block = true)
      } text "Block until job completes",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t)
      } text "Maximum time to poll for running job status"
    ) text "Run a pbsmrtpipe pipeline by name on the server"

    cmd(Modes.JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.JOB)
    } children(
      arg[String]("job-id") required() action { (i, c) =>
        c.copy(jobId = entityIdOrUuid(i))
      } validate { i => validateId(i, "Job") } text "Job ID",
      opt[Unit]("show-settings") action { (_, c) =>
        c.copy(dumpJobSettings = true)
      } text "Print JSON settings for job, suitable for input to 'pbservice run-analysis'",
      opt[Unit]("show-reports") action { (_, c) =>
        c.copy(showReports = true)
      } text "Display job report attributes"
    ) text "Show job details"

    cmd(Modes.JOBS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.JOBS)
    } children(
      opt[Int]('m', "max-items") action { (m, c) =>
        c.copy(maxItems = m)
      } text "Max number of jobs to show"
    )

    cmd(Modes.DATASET.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DATASET)
    } children(
      arg[String]("dataset-id") required() action { (i, c) =>
        c.copy(datasetId = entityIdOrUuid(i))
      } validate { i => validateId(i, "Dataset") } text "Dataset ID"
    ) text "Show dataset details"

    cmd(Modes.DATASETS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DATASETS)
    } children(
      opt[String]('t', "dataset-type") action { (t, c) =>
        c.copy(datasetType = t)
      } text "Dataset Meta type", // TODO validate
      opt[Int]('m', "max-items") action { (m, c) =>
        c.copy(maxItems = m)
      } text "Max number of Datasets to show",
      opt[String]("search-name") action { (n, c) =>
        c.copy(searchName = Some(n))
      } text "Search for datasets whose 'name' field matches the specified string",
      opt[String]("search-path") action { (p, c) =>
        c.copy(searchPath = Some(p))
      } text "Search for datasets whose 'path' field matches the specified string"
    )

    cmd(Modes.DELETE_DATASET.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DELETE_DATASET)
    } children(
      arg[String]("dataset-id") required() action { (i, c) =>
        c.copy(datasetId = entityIdOrUuid(i))
      } validate { i => validateId(i, "Dataset") } text "Dataset ID"
    ) text "Soft-delete of a dataset (won't remove files)"

    cmd(Modes.MANIFESTS.name) action {(_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.MANIFESTS)
    } text "Get a List of SMRT Link PacBio sComponent Versions"

    cmd(Modes.MANIFEST.name) action {(_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.MANIFEST)
    } children(
        opt[String]('i', "manifest-id") action { (t, c) =>
          c.copy(manifestId = t)
        } text s"Manifest By Id (Default: ${defaults.manifestId})"
        ) text "Get PacBio Component Manifest version by Id."

    // FIXME(nechols)(2016-09-21) disabled due to WSO2, will revisit later
    /*cmd(Modes.CREATE_PROJECT.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.CREATE_PROJECT)
    } children(
      arg[String]("name") required() action { (n, c) =>
        c.copy(name = n)
      } text "Project name",
      arg[String]("description") required() action { (d, c) =>
        c.copy(description = d)
      } text "Project description"
    ) text "Start a new project"*/

    opt[Unit]('h', "help") action { (x, c) =>
      showUsage
      sys.exit(0)
    } text "Show options and exit"

    opt[Unit]("version") action { (x, c) =>
      showVersion
      sys.exit(0)
    } text "Show tool version and exit"
  }
}


// TODO consolidate Try behavior
class PbService (val sal: AnalysisServiceAccessLayer,
                 val maxTime: Int = -1) extends LazyLogging with ClientUtils {
  import AnalysisClientJsonProtocol._
  import CommonModels._
  import CommonModelImplicits._

  protected val TIMEOUT = 30 seconds
  private lazy val entryPointsLookup = Map(
    "PacBio.DataSet.SubreadSet" -> "eid_subread",
    "PacBio.DataSet.ReferenceSet" -> "eid_ref_dataset",
    "PacBio.DataSet.BarcodeSet" -> "eid_barcode",
    "PacBio.DataSet.HdfSubreadSet" -> "eid_hdfsubread",
    "PacBio.DataSet.ConsensusReadSet" -> "eid_ccs",
    "PacBio.DataSet.AlignmentSet" -> "eid_alignment")
  private lazy val defaultPresets = PipelineTemplatePreset("default", "any",
    Seq[ServiceTaskOptionBase](),
    Seq[ServiceTaskOptionBase]())
  private lazy val rsMovieName = """m([0-9]{6})_([0-9a-z]{5,})_([0-9a-z]{5,})_c([0-9]{16,})_(\w\d)_(\w\d)""".r

  private def matchRsMovieName(file: File): Boolean =
    rsMovieName.findPrefixMatchOf(file.getName).isDefined

  // FIXME this is crude
  protected def errorExit(msg: String, exitCode: Int = -1): Int = {
    println(msg)
    exitCode
  }

  protected def printMsg(msg: String): Int = {
    println(msg)
    0
  }

  protected def showNumRecords(label: String, fn: () => Future[Int]): Unit = {
    Try { Await.result(fn(), TIMEOUT) } match {
      case Success(nrecords) => println(s"$label $nrecords")
      case Failure(err) => println(s"ERROR: couldn't retrieve $label")
    }
  }

  protected def printStatus(status: ServiceStatus, asJson: Boolean = false): Int = {
    if (asJson) {
      println(status.toJson.prettyPrint)
    } else{
      println(s"SMRTLink Services Version: ${status.version} \nStatus: ${status.message}\nDataSet Summary:")
      showNumRecords("SubreadSets", () => sal.getSubreadSets.map(_.length))
      showNumRecords("HdfSubreadSets", () => sal.getHdfSubreadSets.map(_.length))
      showNumRecords("ReferenceSets", () => sal.getReferenceSets.map(_.length))
      showNumRecords("BarcodeSets", () => sal.getBarcodeSets.map(_.length))
      showNumRecords("AlignmentSets", () => sal.getAlignmentSets.map(_.length))
      showNumRecords("ConsensusReadSets", () => sal.getConsensusReadSets.map(_.length))
      showNumRecords("ConsensusAlignmentSets", () => sal.getConsensusAlignmentSets.map(_.length))
      showNumRecords("ContigSets", () => sal.getContigSets.map(_.length))
      showNumRecords("GmapReferenceSets", () => sal.getGmapReferenceSets.map(_.length))
      println("SMRT Link Job Summary:")
      showNumRecords("import-dataset Jobs", () => sal.getImportJobs.map(_.length))
      showNumRecords("merge-dataset Jobs", () => sal.getMergeJobs.map(_.length))
      showNumRecords("convert-fasta-reference Jobs", () => sal.getFastaConvertJobs.map(_.length))
      showNumRecords("pbsmrtpipe Jobs", () => sal.getAnalysisJobs.map(_.length))
    }
    0
  }

  def runStatus(asJson: Boolean = false): Int = {
    Try { Await.result(sal.getStatus, TIMEOUT) } match {
      case Success(status) => printStatus(status, asJson)
      case Failure(err) => errorExit(err.getMessage)
    }
  }

  def runGetDataSetInfo(datasetId: IdAble, asJson: Boolean = false): Int = {
    Try { Await.result(sal.getDataSet(datasetId), TIMEOUT) } match {
      case Success(ds) => printDataSetInfo(ds, asJson)
      case Failure(err) => errorExit(s"Could not retrieve existing dataset record: $err")
    }
  }

  def runGetDataSets(dsType: String,
                     maxItems: Int,
                     asJson: Boolean = false,
                     searchName: Option[String] = None,
                     searchPath: Option[String] = None): Int = {
    def isMatching(ds: ServiceDataSetMetadata): Boolean = {
      val qName = searchName.map(n => ds.name contains n).getOrElse(true)
      val qPath = searchPath.map(p => ds.path contains p).getOrElse(true)
      qName && qPath
    }
    Try {
      dsType match {
        case "subreads" => Await.result(sal.getSubreadSets, TIMEOUT)
        case "hdfsubreads" => Await.result(sal.getHdfSubreadSets, TIMEOUT)
        case "barcodes" => Await.result(sal.getBarcodeSets, TIMEOUT)
        case "references" => Await.result(sal.getReferenceSets, TIMEOUT)
        case "gmapreferences" => Await.result(sal.getGmapReferenceSets, TIMEOUT)
        case "contigs" => Await.result(sal.getContigSets, TIMEOUT)
        case "ccsalignments" => Await.result(sal.getConsensusAlignmentSets, TIMEOUT)
        case "ccsreads" => Await.result(sal.getConsensusReadSets, TIMEOUT)
        //case _ => throw Exception("Not a valid dataset type")
      }
    } match {
      case Success(records) => {
        if (asJson) {
          var k = 1
          for (ds <- records.filter(r => isMatching(r))) {
            // XXX this is annoying - the records get interpreted as
            // Seq[ServiceDataSetMetaData], which can't be unmarshalled
            val sep = if (k < records.size) "," else ""
            dsType match {
              case "subreads" => println(ds.asInstanceOf[SubreadServiceDataSet].toJson.prettyPrint + sep)
              case "hdfsubreads" => println(ds.asInstanceOf[HdfSubreadServiceDataSet].toJson.prettyPrint + sep)
              case "barcodes" => println(ds.asInstanceOf[BarcodeServiceDataSet].toJson.prettyPrint + sep)
              case "references" => println(ds.asInstanceOf[ReferenceServiceDataSet].toJson.prettyPrint + sep)
              case "gmapreferences" => println(ds.asInstanceOf[GmapReferenceServiceDataSet].toJson.prettyPrint + sep)
              case "ccsreads" => println(ds.asInstanceOf[ConsensusReadServiceDataSet].toJson.prettyPrint + sep)
              case "ccsalignments" => println(ds.asInstanceOf[ConsensusAlignmentServiceDataSet].toJson.prettyPrint + sep)
              case "contigs" => println(ds.asInstanceOf[ContigServiceDataSet].toJson.prettyPrint + sep)
            }
            k += 1
          }
        } else {
          var k = 0
          val table = for {
            ds <- records.filter(r => isMatching(r)).reverse if k < maxItems
          } yield {
            k += 1
            Seq(ds.id.toString, ds.uuid.toString, ds.name, ds.path)
          }
          printTable(table, Seq("ID", "UUID", "Name", "Path"))
        }
        0
      }
      case Failure(err) => {
        errorExit(s"Error: ${err.getMessage}")
      }
    }
  }

  def runGetJobInfo(jobId: IdAble,
                    asJson: Boolean = false,
                    dumpJobSettings: Boolean = false,
                    showReports: Boolean = false): Int = {
    Try { Await.result(sal.getJob(jobId), TIMEOUT) } match {
      case Success(job) =>
        var rc = printJobInfo(job, asJson, dumpJobSettings)
        if (showReports && (rc == 0)) {
          rc = Try {
            Await.result(sal.getAnalysisJobReports(job.uuid), TIMEOUT)
          } match {
            case Success(rpts) => rpts.map { dsr =>
              Try {
                Await.result(sal.getAnalysisJobReport(job.uuid, dsr.dataStoreFile.uuid), TIMEOUT)
              } match {
                case Success(rpt) => showReportAttributes(rpt)
                case Failure(err) => errorExit(s"Couldn't retrieve report ${dsr.dataStoreFile.uuid} for job ${job.uuid}: $err")
              }
            }.reduceLeft(_ max _)
            case Failure(err) => errorExit(s"Could not retrieve reports: $err")
          }
        }
        rc
      case Failure(err) => errorExit(s"Could not retrieve job record: $err")
    }
  }

  def runGetJobs(maxItems: Int, asJson: Boolean = false): Int = {
    Try { Await.result(sal.getAnalysisJobs, TIMEOUT) } match {
      case Success(engineJobs) => {
        if (asJson) println(engineJobs.toJson.prettyPrint) else {
          val table = engineJobs.reverse.take(maxItems).map(job =>
            Seq(job.id.toString, job.state.toString, job.name, job.uuid.toString))
          printTable(table, Seq("ID", "State", "Name", "UUID"))
        }
        0
      }
      case Failure(err) => {
        errorExit(s"Could not retrieve jobs: ${err.getMessage}")
      }
    }
  }

  protected def waitForJob(jobId: IdAble): Int = {
    println(s"waiting for job ${jobId.toIdString} to complete...")
    sal.pollForJob(jobId, maxTime) match {
      case Success(msg) => runGetJobInfo(jobId)
      case Failure(err) => {
        runGetJobInfo(jobId)
        errorExit(err.getMessage)
      }
    }
  }

  private def importFasta(path: Path,
                          dsType: FileTypes.DataSetBaseType,
                          runJob: () => Future[EngineJob],
                          getDataStore: IdAble => Future[Seq[DataStoreServiceFile]],
                          projectName: Option[String],
                          barcodeMode: Boolean = false): Int = {
    val projectId = getProjectIdByName(projectName)
    if (projectId < 0) return errorExit("Can't continue with an invalid project.")
    val tx = for {
      contigs <- Try { PacBioFastaValidator.validate(path, barcodeMode) }
      job <- Try { Await.result(runJob(), TIMEOUT) }
      job <- sal.pollForJob(job.uuid, maxTime)
      dataStoreFiles <- Try { Await.result(getDataStore(job.uuid), TIMEOUT) }
    } yield dataStoreFiles

    tx match {
      case Success(dataStoreFiles) =>
        dataStoreFiles.find(_.fileTypeId == dsType.fileTypeId) match {
          case Some(ds) =>
            runGetDataSetInfo(ds.uuid)
            if (projectId > 0) addDataSetToProject(ds.uuid, projectId) else 0
          case None => errorExit(s"Couldn't find ${dsType.dsName}")
        }
      case Failure(err) => errorExit(s"Import job error: ${err.getMessage}")
    }
  }

  def runImportFasta(path: Path,
                     name: String,
                     organism: String,
                     ploidy: String,
                     projectName: Option[String] = None): Int = {
    // this really shouldn't be optional
    val nameFinal = if (name.isEmpty) "unknown" else name
    importFasta(path,
                FileTypes.DS_REFERENCE,
                () => sal.importFasta(path, nameFinal, organism, ploidy),
                sal.getImportFastaJobDataStore,
                projectName)
  }

  def runImportBarcodes(path: Path,
                        name: String,
                        projectName: Option[String] = None): Int =
    importFasta(path,
                FileTypes.DS_BARCODE,
                () => sal.importFastaBarcodes(path, name),
                sal.getImportBarcodesJobDataStore,
                projectName,
                barcodeMode = true)

  private def importXmlRecursive(path: Path,
                                 listFilesOfType: File => Array[File],
                                 doImportOne: Path => Int,
                                 doImportMany: Path => Int): Int = {
    val f = path.toFile
    if (f.isDirectory) {
      val xmlFiles = listFilesOfType(f)
      if (xmlFiles.isEmpty) {
        errorExit(s"No valid XML files found in ${f.getAbsolutePath}")
      } else {
        println(s"Found ${xmlFiles.length} matching XML files")
        val failed: ListBuffer[String] = ListBuffer()
        xmlFiles.foreach { xmlFile =>
          println(s"Importing ${xmlFile.getAbsolutePath}...")
          val rc = doImportMany(xmlFile.toPath)
          if (rc != 0) failed.append(xmlFile.getAbsolutePath.toString)
        }
        if (failed.nonEmpty) {
          println(s"${failed.size} import(s) failed:")
          failed.foreach { println }
          1
        } else 0
      }
    } else if (f.isFile) doImportOne(f.toPath)
    else errorExit(s"${f.getAbsolutePath} is not readable")
  }

  def runImportDataSetSafe(path: Path, projectId: Int = 0): Int = {
    val dsUuid = dsUuidFromPath(path)
    println(s"UUID: ${dsUuid.toString}")
    Try { Await.result(sal.getDataSet(dsUuid), TIMEOUT) } match {
      case Success(dsInfo) => {
        println(s"Dataset ${dsUuid.toString} already imported.")
        printDataSetInfo(dsInfo)
      }
      case Failure(err) => {
        println(s"No existing dataset record found")
        val dsType = dsMetaTypeFromPath(path)
        val rc = runImportDataSet(path, dsType)
        if (rc == 0) {
          runGetDataSetInfo(dsUuid)
          if (projectId > 0) addDataSetToProject(dsUuid, projectId) else 0
        } else rc
      }
    }
  }

  def runImportDataSet(path: Path, dsType: String): Int = {
    logger.info(dsType)
    Try { Await.result(sal.importDataSet(path, dsType), TIMEOUT) } match {
      case Success(jobInfo: EngineJob) => waitForJob(jobInfo.uuid)
      case Failure(err) => errorExit(s"Dataset import failed: $err")
    }
  }

  private def listDataSetFiles(f: File): Array[File] = {
    f.listFiles.filter((fn) =>
      Try { dsMetaTypeFromPath(fn.toPath) }.isSuccess
    ).toArray ++ f.listFiles.filter(_.isDirectory).flatMap(listDataSetFiles)
  }

  def runImportDataSets(path: Path,
                        nonLocal: Option[String],
                        projectName: Option[String] = None): Int = {
    val projectId = getProjectIdByName(projectName)
    if (projectId < 0) return errorExit("Can't continue with an invalid project.")
    nonLocal match {
      case Some(dsType) =>
        logger.info(s"Non-local file, importing as type $dsType")
        runImportDataSet(path, dsType)
      case _ =>
        importXmlRecursive(path, listDataSetFiles,
                           (p) => runImportDataSetSafe(p, projectId),
                           (p) => runImportDataSetSafe(p, projectId))
    }
  }

  def runImportRsMovie(path: Path, name: String, projectId: Int = 0): Int = {
    val fileName = path.toAbsolutePath
    if (fileName.endsWith(".fofn") && (name == "")) {
      return errorExit(s"--name argument is required when an FOFN is input")
    }
    val tx = for {
      finalName <- Try { if (name == "") dsNameFromMetadata(path) else name }
      job <- Try { Await.result(sal.convertRsMovie(path, name), TIMEOUT) }
      job <- sal.pollForJob(job.uuid)
      dataStoreFiles <- Try { Await.result(sal.getConvertRsMovieJobDataStore(job.uuid), TIMEOUT) }
    } yield dataStoreFiles

    tx match {
      case Success(dataStoreFiles) =>
        dataStoreFiles.find(_.fileTypeId == FileTypes.DS_HDF_SUBREADS.fileTypeId) match {
          case Some(ds) =>
            runGetDataSetInfo(ds.uuid)
            if (projectId > 0) addDataSetToProject(ds.uuid, projectId) else 0
          case None => errorExit(s"Couldn't find HdfSubreadSet")
        }
      case Failure(err) => errorExit(s"RSII movie import failed: ${err.getMessage}")
    }
  }

  private def listMovieMetadataFiles(f: File): Array[File] = {
    f.listFiles.filter((fn) =>
      matchRsMovieName(fn) && Try { dsNameFromMetadata(fn.toPath) }.isSuccess
    ).toArray ++ f.listFiles.filter(_.isDirectory).flatMap(listMovieMetadataFiles)
  }

  def runImportRsMovies(path: Path,
                        name: String,
                        projectName: Option[String] = None): Int = {
    val projectId = getProjectIdByName(projectName)
    if (projectId < 0) return errorExit("Can't continue with an invalid project.")
    def doImportMany(p: Path): Int = {
      if (name != "") errorExit("--name option not allowed when path is a directory")
      else runImportRsMovie(p, name, projectId)
    }
    importXmlRecursive(path, listMovieMetadataFiles,
                       (p) => runImportRsMovie(p, name, projectId),
                       (p) => doImportMany(p))
  }

  def addDataSetToProject(dsId: IdAble, projectId: Int,
                          verbose: Boolean = false): Int = {
    val tx = for {
      project <- Try { Await.result(sal.getProject(projectId), TIMEOUT) }
      ds <- Try { Await.result(sal.getDataSet(dsId), TIMEOUT) }
      request <- Try { project.asRequest.appendDataSet(ds.id) }
      projectWithDataSet <- Try { Await.result(sal.updateProject(projectId, request), TIMEOUT) }
      } yield projectWithDataSet

      tx match {
        case Success(p) =>
          if (verbose) printProjectInfo(p) else printMsg(s"Added dataset to project ${p.name}")
        case Failure(err) => errorExit(s"Couldn't add dataset to project: ${err.getMessage}")
    }
  }

  def runDeleteDataSet(datasetId: IdAble): Int = {
    Try { Await.result(sal.deleteDataSet(datasetId), TIMEOUT) } match {
      case Success(response) => printMsg(s"${response.message}")
      case Failure(err) => errorExit(s"Couldn't delete dataset: ${err.getMessage}")
    }
  }

  protected def getProjectIdByName(projectName: Option[String]): Int = {
    if (! projectName.isDefined) return 0
    Try { Await.result(sal.getProjects, TIMEOUT) } match {
      case Success(projects) =>
        projects.map(p => (p.name, p.id)).toMap.get(projectName.get) match {
          case Some(projectId) => projectId
          case None => errorExit(s"Can't find project named '${projectName.get}'", -1)
        }
      case Failure(err) => errorExit(s"Couldn't retrieve projects: ${err.getMessage}", -1)
    }
  }

  def runCreateProject(name: String, description: String): Int = {
    Try { Await.result(sal.createProject(name, description), TIMEOUT) } match {
      case Success(project) => printProjectInfo(project)
      case Failure(err) => errorExit(s"Couldn't create project: ${err.getMessage}")
    }
  }

  def runEmitAnalysisTemplate: Int = {
    val analysisOpts = {
      val ep = BoundServiceEntryPoint("eid_subread", "PacBio.DataSet.SubreadSet", Left(0))
      val eps = Seq(ep)
      val taskOptions = Seq[ServiceTaskOptionBase]()
      val workflowOptions = Seq[ServiceTaskOptionBase]()
      PbSmrtPipeServiceOptions(
        "My-job-name",
        "pbsmrtpipe.pipelines.mock_dev01",
        eps,
        taskOptions,
        workflowOptions)
    }
    println(analysisOpts.toJson.prettyPrint)
    // FIXME can we embed this in the Json somehow?
    println("datasetId should be an integer; to obtain the datasetId from a UUID, run 'pbservice get-dataset {UUID}'. The entryId(s) can be obtained by running 'pbsmrtpipe show-pipeline-templates {PIPELINE-ID}'")
    0
  }

  def runShowPipelines: Int = {
    Await.result(sal.getPipelineTemplates, TIMEOUT).sortWith(_.id > _.id).foreach { pt =>
      println(s"${pt.id}: ${pt.name}")
    }
    0
  }

  def runAnalysisPipeline(jsonPath: Path, block: Boolean): Int = {
    val jsonSrc = Source.fromFile(jsonPath.toFile).getLines.mkString
    val jsonAst = jsonSrc.parseJson
    val analysisOptions = jsonAst.convertTo[PbSmrtPipeServiceOptions]
    runAnalysisPipelineImpl(analysisOptions, block)
  }

  protected def validateEntryPoints(entryPoints: Seq[BoundServiceEntryPoint]): Int = {
    for (entryPoint <- entryPoints) {
      // FIXME
      val datasetId: IdAble = entryPoint.datasetId match {
        case Left(id) => IntIdAble(id)
        case Right(uuid) => UUIDIdAble(uuid)
      }
      Try {
        Await.result(sal.getDataSet(datasetId), TIMEOUT)
      } match {
        case Success(dsInfo) => {
          // TODO check metatype against input
          println(s"Found entry point ${entryPoint.entryId} (datasetId = ${datasetId})")
          printDataSetInfo(dsInfo)
        }
        case Failure(err) => {
          return errorExit(s"can't retrieve datasetId ${datasetId}")
        }
      }
    }
    0
  }

  protected def validatePipelineId(pipelineId: String): Int = {
    Try {
      Await.result(sal.getPipelineTemplate(pipelineId), TIMEOUT)
    } match {
      case Success(x) => printMsg(s"Found pipeline template ${pipelineId}")
      case Failure(err) => errorExit(s"Can't find pipeline template ${pipelineId}: ${err.getMessage}\nUse 'pbsmrtpipe show-templates' to display a list of available pipelines")
    }
  }

  protected def validatePipelineOptions(analysisOptions: PbSmrtPipeServiceOptions): Int = {
    max(validatePipelineId(analysisOptions.pipelineId),
        validateEntryPoints(analysisOptions.entryPoints))
  }

  protected def runAnalysisPipelineImpl(analysisOptions: PbSmrtPipeServiceOptions, block: Boolean = true, validate: Boolean = true): Int = {
    //println(analysisOptions)
    var xc = 0
    if (validate) {
      xc = validatePipelineOptions(analysisOptions)
      if (xc != 0) return errorExit("Analysis options failed validation")
    }
    Try {
      Await.result(sal.runAnalysisPipeline(analysisOptions), TIMEOUT)
    } match {
      case Success(jobInfo) => {
        println(s"Job ${jobInfo.id} UUID ${jobInfo.uuid} started")
        printJobInfo(jobInfo)
        if (block) waitForJob(jobInfo.uuid) else 0
      }
      case Failure(err) => errorExit(err.getMessage)
    }
  }

  protected def importEntryPoint(eid: String, xmlPath: Path): BoundServiceEntryPoint = {
    var dsType = dsMetaTypeFromPath(xmlPath)
    var dsUuid = dsUuidFromPath(xmlPath)
    var xc = runImportDataSetSafe(xmlPath)
    if (xc != 0) throw new Exception(s"Could not import dataset ${eid}:${xmlPath}")
    // this is stupidly inefficient
    val dsId = Try {
      Await.result(sal.getDataSet(dsUuid), TIMEOUT)
    } match {
      case Success(ds) => ds.id
      case Failure(err) => throw new Exception(err.getMessage)
    }
    BoundServiceEntryPoint(eid, dsType, Left(dsId))
  }

  protected def importEntryPointAutomatic(entryPoint: String): BoundServiceEntryPoint = {
    val epFields = entryPoint.split(':')
    if (epFields.length == 2) importEntryPoint(epFields(0),
                                               Paths.get(epFields(1)))
    else if (epFields.length == 1) {
      val xmlPath = Paths.get(epFields(0))
      val dsType = dsMetaTypeFromPath(xmlPath)
      val eid = entryPointsLookup(dsType)
      importEntryPoint(eid, xmlPath)
    } else throw new Exception(s"Can't interpret argument ${entryPoint}")
  }

  protected def getPipelinePresets(presetXml: Option[Path]): PipelineTemplatePreset = {
    presetXml match {
      case Some(path) => PipelineTemplatePresetLoader.loadFrom(path)
      case _ => defaultPresets
    }
  }

  // XXX there is a bit of a disconnect between how preset.xml is handled and
  // how options are actually passed to services, so we need to convert them
  // here
  protected def getPipelineServiceOptions(jobTitle: String, pipelineId: String,
      entryPoints: Seq[BoundServiceEntryPoint],
      presets: PipelineTemplatePreset): PbSmrtPipeServiceOptions = {
    Try {
      logger.debug("Getting pipeline options from server")
      Await.result(sal.getPipelineTemplate(pipelineId), TIMEOUT)
    } match {
      case Success(pipeline) => {
        val taskOptions = PipelineUtils.getPresetTaskOptions(pipeline, presets.taskOptions)
        val workflowOptions = Seq[ServiceTaskOptionBase]()
        PbSmrtPipeServiceOptions(jobTitle, pipelineId, entryPoints, taskOptions,
                                 workflowOptions)
      }
      case Failure(err) => throw new Exception(s"Failed to decipher pipeline options: ${err.getMessage}")
    }
  }

  def runPipeline(pipelineId: String, entryPoints: Seq[String], jobTitle: String,
                  presetXml: Option[Path] = None, block: Boolean = true,
                  validate: Boolean = true): Int = {
    if (entryPoints.isEmpty) return errorExit("At least one entry point is required")

    val pipelineIdFull = if (pipelineId.split('.').length != 3) s"pbsmrtpipe.pipelines.$pipelineId" else pipelineId

    println(s"pipeline ID: $pipelineIdFull")
    if (validatePipelineId(pipelineIdFull) != 0) return errorExit("Aborting")
    var jobTitleTmp = jobTitle
    if (jobTitle.length == 0) jobTitleTmp = s"pbservice-$pipelineIdFull"
    val tx = for {
      eps <- Try { entryPoints.map(importEntryPointAutomatic) }
      presets <- Try { getPipelinePresets(presetXml) }
      opts <- Try { getPipelineServiceOptions(jobTitleTmp, pipelineIdFull, eps, presets) }
      job <- Try { Await.result(sal.runAnalysisPipeline(opts), TIMEOUT) }
    } yield job

    tx match {
      case Success(job) => if (block) waitForJob(job.uuid) else printJobInfo(job)
      case Failure(err) => errorExit(s"Failed to run pipeline: ${err}")//.getMessage}")
    }
  }

  def runTerminateAnalysisJob(jobId: IdAble): Int = {
    println(s"Attempting to terminate Analysis Job ${jobId.toIdString}")
    // Only Int Job ids are supported
    def failIfUUID(i: IdAble): Future[Int] = {
      i match {
        case IntIdAble(n) => Future {n}
        case UUIDIdAble(uuid) => Future.failed(new UnprocessableEntityError("Job UUIDs are not supported. Use the Job Int id."))
      }
    }
    val fx = for {
      i <- failIfUUID(jobId)
      messageResponse <- sal.terminatePbsmrtpipeJob(i)
    } yield messageResponse

    Try {Await.result(fx, TIMEOUT) } match {
      case Success(m) => println(m.message); 0
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

  def runDeleteJob(jobId: IdAble): Int = {
    def deleteJob(job: EngineJob, nChildren: Int): Future[EngineJob] = {
      if (!job.isComplete) {
        throw new Exception(s"Can't delete this job because it hasn't completed - try 'pbservice terminate-job ${jobId.toIdString} ...' first")
      } else if (nChildren > 0) {
        throw new Exception(s"Can't delete job ${job.id} because ${nChildren} active jobs used its results as input")
      } else {
        sal.deleteJob(job.uuid)
      }
    }
    println(s"Attempting to delete job ${jobId.toIdString}")
    val fx = for {
      job <- Try { Await.result(sal.getJob(jobId), TIMEOUT) }
      children <- Try { Await.result(sal.getJobChildren(job.uuid), TIMEOUT) }
      deleteJob <- Try { Await.result(deleteJob(job, children.size), TIMEOUT) }
      _ <- sal.pollForJob(deleteJob.uuid, maxTime)
    } yield deleteJob

    fx match {
      case Success(j) => println(s"Job ${jobId.toIdString} deleted."); 0
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

  def manifestSummary(m: PacBioComponentManifest) = s"Component name:${m.name} id:${m.id} version:${m.version}"

  def manifestsSummary(manifests:Seq[PacBioComponentManifest]): String = {
    s"Components ${manifests.length}\n" + manifests.map(manifestSummary).reduce(_ + "\n" + _)
  }

  def runGetPacBioManifests: Int = {
    Try {Await.result[Seq[PacBioComponentManifest]](sal.getPacBioComponentManifests, TIMEOUT)} match {
      case Success(manifests) => println(manifestsSummary(manifests)); 0
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

  // This is to make it backward compatiblity. Remove this when the other systems are updated
  private def getManifestById(manifestId: String): Future[PacBioComponentManifest] = {
    sal.getPacBioComponentManifests.flatMap { manifests =>
      manifests.find(x => x.id == manifestId) match {
        case Some(m) => Future { m }
        case _ => Future.failed(new ResourceNotFoundError(s"Unable to find $manifestId"))
      }
    }
  }

  def runGetPacBioManifestById(ix: String): Int = {
    println(s"Attempting to get PacBio Component '$ix'")
    Try {Await.result[PacBioComponentManifest](getManifestById(ix), TIMEOUT)} match {
      case Success(manifest) => println(manifestSummary(manifest)); 0
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

}

object PbService {
  def apply (c: PbServiceParser.CustomConfig): Int = {
    implicit val actorSystem = ActorSystem("pbservice")
    // FIXME we need some kind of hostname validation here - supposedly URL
    // creation includes validation, but it wasn't failing on extra 'http://'
    val host = c.host.replaceFirst("http://", "")
    val url = new URL(s"http://$host:${c.port}")
    val sal = new AnalysisServiceAccessLayer(url, c.authToken)(actorSystem)
    val ps = new PbService(sal, c.maxTime)
    try {
      c.mode match {
        case Modes.STATUS => ps.runStatus(c.asJson)
        case Modes.IMPORT_DS => ps.runImportDataSets(c.path, c.nonLocal,
                                                     c.project)
        case Modes.IMPORT_FASTA => ps.runImportFasta(c.path, c.name, c.organism,
                                                     c.ploidy, c.project)
        case Modes.IMPORT_BARCODES => ps.runImportBarcodes(c.path, c.name,
                                                           c.project)
        case Modes.IMPORT_MOVIE => ps.runImportRsMovies(c.path, c.name,
                                                        c.project)
        case Modes.ANALYSIS => ps.runAnalysisPipeline(c.path, c.block)
        case Modes.TEMPLATE => ps.runEmitAnalysisTemplate
        case Modes.PIPELINE => ps.runPipeline(c.pipelineId, c.entryPoints,
                                              c.jobTitle, c.presetXml, c.block)
        case Modes.SHOW_PIPELINES => ps.runShowPipelines
        case Modes.JOB => ps.runGetJobInfo(c.jobId, c.asJson, c.dumpJobSettings, c.showReports)
        case Modes.JOBS => ps.runGetJobs(c.maxItems, c.asJson)
        case Modes.TERMINATE_JOB => ps.runTerminateAnalysisJob(c.jobId)
        case Modes.DELETE_JOB => ps.runDeleteJob(c.jobId)
        case Modes.DATASET => ps.runGetDataSetInfo(c.datasetId, c.asJson)
        case Modes.DATASETS => ps.runGetDataSets(c.datasetType, c.maxItems, c.asJson, c.searchName, c.searchPath)
        case Modes.DELETE_DATASET => ps.runDeleteDataSet(c.datasetId)
        case Modes.MANIFEST => ps.runGetPacBioManifestById(c.manifestId)
        case Modes.MANIFESTS => ps.runGetPacBioManifests
/*        case Modes.CREATE_PROJECT => ps.runCreateProject(c.name, c.description)*/
        case x => {
          println(s"Unsupported action '$x'")
          1
        }
      }
    } finally {
      actorSystem.shutdown()
    }
  }
}

object PbServiceApp extends App {
  def run(args: Seq[String]) = {
    val xc = PbServiceParser.parser.parse(args.toSeq, PbServiceParser.defaults) match {
      case Some(config) => PbService(config)
      case _ => 1
    }
    sys.exit(xc)
  }
  run(args)
}
