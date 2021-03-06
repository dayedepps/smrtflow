import java.nio.file.Paths
import java.util.UUID

import com.pacbio.secondary.analysis.jobs.{AnalysisJobStates, JobModels}
import com.pacbio.secondary.smrtlink.actors.TestDalProvider
import com.pacbio.secondary.smrtlink.database.TableModels
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.testkit.TestUtils
import com.pacificbiosciences.pacbiobasedatamodel.{SupportedAcquisitionStates, SupportedRunStates}
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import slick.driver.PostgresDriver.api._
import spray.testkit.Specs2RouteTest
import org.joda.time.{DateTime => JodaDateTime}

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * This is a sanity test for the insertion and querying of the data from the
  * slick defined TableModels. If the db migrations differ from the Table Models,
  * this test should catch the error.
  *
  * This layer is necessary because of the degeneracy of defining the Table Models as
  * scala classes, then defining the migrations in db/V_**.scala. Some of these are
  * defined with raw sql, which can yield differences between the migrations and the TableModels.scala.
  *
  * Note, this has been updated to be re-runnable on an existing db (i.e., without
  * dropping and running the migrations). The assertions now filter for the specific
  * entity (often by uuid) that has been inserted into the db.
  *
  */
class DatabaseSpec extends Specification with Specs2RouteTest with NoTimeConversions with TestDalProvider with TestUtils{
  import JobModels._
  import TableModels._

  val rx = scala.util.Random

  // There's friction here with loading from config, versus the Datasource
  // which is required for the Migrations to be applied.
  val testdb = dbConfig.toDatabase
  step(setupDb(dbConfig))

  "Database" should {
    "Sanity test for inserting and querying the db" in {

      val now = JodaDateTime.now()
      val username = "user-name"
      val datasetTypeId = "dataset-type-id"

      // Generate a new project id and reference this in the tests
      // project names must be unique
      val projectName = s"project-name-${rx.nextInt(10000)}"

      val job = EngineJob(
        id = -1,
        UUID.randomUUID(),
        "job-name",
        "job-comment",
        createdAt = now,
        updatedAt = now,
        AnalysisJobStates.CREATED,
        "job-type-id",
        "/job/path",
        "{\"foo\":true}",
        Some("jsnow"), Some("0.1.0-SL"), Some("0.1.1-SL-TOOLS"))
      val event = JobEvent(UUID.randomUUID(), jobId = -1, AnalysisJobStates.CREATED, "job-created", createdAt = now)
      val tag = (-1, "tag-name")
      val jTag = (-1, -1)
      val project = Project(
        id = -1,
        projectName,
        "project-description",
        ProjectState.CREATED,
        createdAt = now,
        updatedAt = now,
        isActive = true)
      val projectUser = ProjectUser(projectId = -1, username, ProjectUserRole.OWNER)
      val dataset = EngineJobEntryPoint(jobId = -1, UUID.randomUUID(), datasetTypeId)
      val metadata = DataSetMetaDataSet(
        id = -1,
        UUID.randomUUID(),
        "dataset-name",
        "/dataset/path",
        createdAt = now,
        updatedAt = now,
        numRecords = 1,
        totalLength = 1,
        "tags",
        "version",
        "comments",
        "md5",
        createdBy = Some("testuser"),
        jobId = -1,
        projectId = -1,
        isActive = true)
      val subread = SubreadServiceSet(
        id = -1,
        UUID.randomUUID(),
        "cell-id",
        "metadata-context-id",
        "well-sample-name",
        "well-name",
        "bio-sample-name",
        cellIndex = 1,
        "instrument-id",
        "instrument-name",
        "run-name",
        "instrument-control-version")
      val hdf = HdfSubreadServiceSet(
        id = -1,
        UUID.randomUUID(),
        "cell-id",
        "metadata-context-id",
        "well-sample-name",
        "well-name",
        "bio-sample-name",
        cellIndex = 1,
        "instrument-id",
        "instrument-name",
        "run-name",
        "instrument-control-version")
      val reference = ReferenceServiceSet(id = -1, UUID.randomUUID(), "ploidy", "organism")
      val gmap = GmapReferenceServiceSet(id = -1, UUID.randomUUID(), "ploidy", "organism")
      val alignment = AlignmentServiceSet(id = -1, UUID.randomUUID())
      val barcode = BarcodeServiceSet(id = -1, UUID.randomUUID())
      val ccs = ConsensusReadServiceSet(id = -1, UUID.randomUUID())
      val consensus = ConsensusAlignmentServiceSet(id = -1, UUID.randomUUID())
      val contig = ContigServiceSet(id = -1, UUID.randomUUID())
      val datastoreFile = DataStoreServiceFile(
        UUID.randomUUID(),
        "file-type-id",
        "source-id",
        fileSize = 1,
        createdAt = now,
        modifiedAt = now,
        importedAt = now,
        "/datastore/file/path",
        jobId = -1,
        job.uuid,
        "name",
        "description")
      val runSummary = RunSummary(
        UUID.randomUUID(),
        "run-name",
        Some("summary"),
        Some(username),
        createdAt = Some(now),
        startedAt = Some(now),
        transfersCompletedAt = Some(now),
        completedAt = Some(now),
        SupportedRunStates.COMPLETE,
        totalCells = 1,
        numCellsCompleted = 1,
        numCellsFailed = 0,
        Some("instrument-name"),
        Some("instrument-serial-number"),
        Some("instrument-sw-version"),
        Some("primary-analysis-sw-version"),
        Some("context"),
        Some("termination-info"),
        reserved = false)
      val runDataModel = DataModelAndUniqueId("<run>data</run>", runSummary.uniqueId)
      val collection = CollectionMetadata(
        runSummary.uniqueId,
        UUID.randomUUID(),
        "well",
        "name",
        Some("summary"),
        Some("context"),
        Some(Paths.get("/collection/path")),
        SupportedAcquisitionStates.COMPLETE,
        Some("instrument-id"),
        Some("instrument-name"),
        movieMinutes = 30.0,
        Some(username),
        startedAt = Some(now),
        completedAt = Some(now),
        Some("termination-info"))
      val sample = Sample("details", UUID.randomUUID(), "name", username, createdAt = now)

      // TODO(smcclellan): JobResults table does not appear to be real?

      val putAll = testdb.run(
        for {
          jid <- engineJobs returning engineJobs.map(_.id) += job
          _   <- jobEvents += event.copy(jobId = jid)
          tid <- jobTags returning jobTags.map(_.id) += tag
          _   <- jobsTags += jTag.copy(_1 = jid, _2 = tid)
          pid <- projects returning projects.map(_.id) += project
          _   <- projectsUsers += projectUser.copy(projectId = pid)
          _   <- engineJobsDataSets += dataset.copy(jobId = jid)
          _   <- dsMetaData2 += metadata.copy(jobId = jid, projectId = pid)
          _   <- dsSubread2 += subread
          _   <- dsHdfSubread2 += hdf
          _   <- dsReference2 += reference
          _   <- dsGmapReference2 += gmap
          _   <- dsAlignment2 += alignment
          _   <- dsBarcode2 += barcode
          _   <- dsCCSread2 += ccs
          _   <- dsCCSAlignment2 += consensus
          _   <- dsContig2 += contig
          _   <- datastoreServiceFiles += datastoreFile.copy(jobId = jid)
          _   <- runSummaries += runSummary
          _   <- dataModels += runDataModel
          _   <- collectionMetadata += collection
          _   <- samples += sample
        } yield ()
      )

      Await.result(putAll, 10.seconds)
      //println("Successfully inserted test data")
      // This is a bit difficult to debug. If there's an empty list returned, there error is quite cryptic.
      //[info] ! Sanity test for inserting and querying the db
      //[error]    NoSuchElementException: : Invoker.first  (Invoker.scala:34)
      //[error] slick.jdbc.Invoker$class.first(Invoker.scala:34)
      //[error] slick.jdbc.StatementInvoker.first(StatementInvoker.scala:16)

      val ej = Await.result(testdb.run(engineJobs.filter(_.uuid === job.uuid).result.head), 1.second)
      val je = Await.result(testdb.run(jobEvents.filter(_.jobId === ej.id).result.head), 1.second)
      val ta = Await.result(testdb.run(jobTags.result.head), 1.second)
      val jt = Await.result(testdb.run(jobsTags.result.head), 1.second)
      val gp = Await.result(testdb.run(projects.filter(_.name === "General Project").result.head), 1.second)
      // Get the Project that this spec imported
      val pr = Await.result(testdb.run(projects.filter(_.name === projectName).result.head), 1.second)
      val pu = Await.result(testdb.run(projectsUsers.result.head), 1.second)
      val rt = Await.result(testdb.run(datasetMetaTypes.filter(_.shortName === "references").result.head), 1.second)
      val ds = Await.result(testdb.run(engineJobsDataSets.filter(_.jobId === ej.id).result.head), 1.second)
      val md = Await.result(testdb.run(dsMetaData2.filter(_.jobId === ej.id).filter(_.projectId === pr.id).result.head), 1.second)
      val su = Await.result(testdb.run(dsSubread2.filter(_.uuid === subread.uuid).result.head), 1.second)
      val hd = Await.result(testdb.run(dsHdfSubread2.filter(_.uuid === hdf.uuid).result.head), 1.second)
      val re = Await.result(testdb.run(dsReference2.filter(_.uuid === reference.uuid).result.head), 1.second)
      val gm = Await.result(testdb.run(dsGmapReference2.filter(_.uuid === gmap.uuid).result.head), 1.second)
      val al = Await.result(testdb.run(dsAlignment2.filter(_.uuid === alignment.uuid).result.head), 1.second)
      val ba = Await.result(testdb.run(dsBarcode2.filter(_.uuid === barcode.uuid).result.head), 1.second)
      val cc = Await.result(testdb.run(dsCCSread2.filter(_.uuid === ccs.uuid).result.head), 1.second)
      val ca = Await.result(testdb.run(dsCCSAlignment2.filter(_.uuid === consensus.uuid).result.head), 1.second)
      val co = Await.result(testdb.run(dsContig2.filter(_.uuid === contig.uuid).result.head), 1.second)
      val df = Await.result(testdb.run(datastoreServiceFiles.filter(_.jobId === ej.id).result.head), 1.second)
      val rs = Await.result(testdb.run(runSummaries.filter(_.uniqueId === runSummary.uniqueId).result.head), 1.second)
      val dm = Await.result(testdb.run(dataModels.filter(_.uniqueId === runDataModel.uniqueId).result.head), 1.second)
      val cm = Await.result(testdb.run(collectionMetadata.filter(_.uniqueId === collection.uniqueId).result.head), 1.second)
      val sa = Await.result(testdb.run(samples.filter(_.uniqueId === sample.uniqueId).result.head), 1.second)

      val jobId = ej.id
      val tagId = ta._1
      val projectId = pr.id
      val metadataId = md.id
      val subreadId = su.id
      val hdfId = hd.id
      val referenceId = re.id
      val gmapId = gm.id
      val alignmentId = al.id
      val barcodeId = ba.id
      val ccsId = cc.id
      val consensusId = ca.id
      val contigId = co.id

      ej === job.copy(id = jobId)
      je === event.copy(jobId = jobId)
      ta === tag.copy(_1 = tagId)
      //jt === jTag.copy(_1 = jobId, _2 = tagId)
      //gp.description === "General Project"
      //pr === project.copy(id = projectId)
      //pu === projectUser.copy(projectId = projectId)
      rt.id === "PacBio.DataSet.ReferenceSet"
      ds === dataset.copy(jobId = jobId)
      md === metadata.copy(id = metadataId, jobId = jobId, projectId = projectId)
      su === subread.copy(id = subreadId)
      hd === hdf.copy(id = hdfId)
      re === reference.copy(id = referenceId)
      gm === gmap.copy(id = gmapId)
      al === alignment.copy(id = alignmentId)
      ba === barcode.copy(id = barcodeId)
      cc === ccs.copy(id = ccsId)
      ca === consensus.copy(id = consensusId)
      co === contig.copy(id = contigId)
      df === datastoreFile.copy(jobId = jobId)
      rs === runSummary
      dm === runDataModel
      cm === collection
      sa === sample
    }
  }
}
