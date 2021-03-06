package com.pacbio.secondary.analysis.jobs

import java.io.{FileWriter, File, PrintWriter, StringWriter}
import java.net.InetAddress
import java.nio.file.Path
import java.util.UUID

import JobModels._
import akka.pattern.ask
import akka.actor.ActorRef
import akka.util.Timeout
import com.pacbio.secondary.analysis.engine.CommonMessages.{MessageResponse, ImportDataStoreFile, FailedMessage, SuccessMessage}
import com.pacbio.secondary.analysis.jobs._
import com.pacbio.secondary.analysis.tools.timeUtils
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import org.joda.time.{DateTime => JodaDateTime}

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}


trait JobResultWriter {
  def writeStdout(msg: String): Unit
  def writeLineStdout(msg: String) = writeStdout(msg + "\n")

  def writeStderr(msg: String): Unit
  def writeLineStderr(msg: String) = writeStderr(msg + "\n")
}

class NullJobResultsWriter extends JobResultWriter{
  def writeStdout(msg: String) = {}
  def writeStderr(msg: String) = {}
}

class PrinterJobResultsWriter extends JobResultWriter {
  def writeStdout(msg: String) = println(msg)

  def writeStderr(msg: String) = System.err.println(msg)
}

class FileJobResultsWriter(stdout: FileWriter, stderr: FileWriter) extends JobResultWriter {

  // This is a temporary hacky logging-ish model.
  private def toTimeStampMessage(msg: String, level: String = "INFO"): String =
    s"[$level] [${JodaDateTime.now()}] $msg"

  def writeStdout(msg: String) = stdout.append(toTimeStampMessage(msg))

  def writeStderr(msg: String) = {
    val logMsg = toTimeStampMessage(msg, level = "ERROR")
    stderr.append(msg)
    // This is to have the errors also be written the "log"
    stdout.append(logMsg)
    System.err.println(msg)
  }
}

/**
 * Job executing layer
 *
 * Created by mkocher on 4/24/15.
 */
trait JobExecutorComponent extends LazyLogging {

  val OUTPUT_FILE_PREFIX = "pbscala-engine"

  def runJob(job: CoreJobModel, pbJob: JobResourceBase, writer: JobResultWriter)(implicit ec: ExecutionContext): Either[ResultFailed, ResultSuccess]

  def runJobFromOpts(opts: BaseJobOptions, pbJob: JobResourceBase, writer: JobResultWriter)(implicit ec: ExecutionContext): Either[ResultFailed, ResultSuccess] = {
    runJob(opts.toJob, pbJob, writer)
  }
}

abstract class JobRunner extends JobExecutorComponent

/**
 * Simple Job Runner only runs the job, it doesn't import datasets
 * back into the system
 */
class SimpleJobRunner extends JobRunner with timeUtils {

  def runJob(job: CoreJobModel, pbJob: JobResourceBase, writer: JobResultWriter)(implicit ec: ExecutionContext): Either[ResultFailed, ResultSuccess] = {

    val msg = s"Running job-type ${job.jobTypeId.id} ${job.toString} in ${pbJob.path.toString}"
    logger.info(msg)
    writer.writeStdout(msg)

    val host = InetAddress.getLocalHost.getHostName
    val startedAt = JodaDateTime.now()

    val jobResult = Try {job.run(pbJob, writer)}

    val runTime = computeTimeDelta(JodaDateTime.now(), startedAt)
    def toM(state: AnalysisJobStates.JobStates) = s"Job ${pbJob.jobId} completed with state $state in $runTime sec."

    jobResult match {
      case Success(result) =>
        result match {
          case Right(x: ResultSuccess) => Right(x) // pass through
          case Right(x) => Right(ResultSuccess(pbJob.jobId, job.jobTypeId.toString, toM(AnalysisJobStates.SUCCESSFUL), runTime, AnalysisJobStates.SUCCESSFUL, host))
          case Left(x: ResultFailed) =>
            logger.error(s"Job $job failed ${x.message}")
            Left(x)
        }
      case Failure(ex) =>
        val sw = new StringWriter
        ex.printStackTrace(new PrintWriter(sw))
        val emsg = s"Unhandled exception in job type ${job.jobTypeId.id} id:${pbJob.jobId} in ${pbJob.path} " + sw.toString
        //val emsg = ex.getMessage
        writer.writeStderr(emsg)
        Left(ResultFailed(pbJob.jobId, job.jobTypeId.toString, s"Job $job FAILED $emsg ${ex.getStackTrace}", computeTimeDeltaFromNow(startedAt), AnalysisJobStates.FAILED, host))
    }
  }
}

/**
 * Import DataStore files after the run is completed.
 *
 * Note, this will only import non-chunked datastore files. Chunked files are interpreted as intermediate files.
 *
 * @param dsActor
 */
class SimpleAndImportJobRunner(dsActor: ActorRef) extends JobRunner with timeUtils{

  implicit val timeout = Timeout(30.seconds)

  private def importDataStoreFile(ds: DataStoreFile, jobUUID: UUID)(implicit ec: ExecutionContext): Future[String] = {
    if (ds.isChunked) {
      Future.successful(s"skipping import of intermediate chunked file $ds")
    } else {
      (dsActor ? ImportDataStoreFile(ds, jobUUID)).mapTo[MessageResponse].map(_.message)
    }
  }

  private def importDataStore(dataStore: PacBioDataStore, jobUUID:UUID)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    // Filter out non-chunked files. The are presumed to be intermediate files
    val nonChunked = dataStore.files.filter(!_.isChunked)
    Future.sequence(nonChunked.map(x => importDataStoreFile(x, jobUUID)))
  }

  private def importAbleFile(x: ImportAble, jobUUID: UUID)(implicit ec: ExecutionContext ): Future[Seq[String]] = {
    x match {
      case x: DataStoreFile => importDataStoreFile(x, jobUUID).map(List(_))
      case x: PacBioDataStore => importDataStore(x, jobUUID)
    }
  }

  def runJob(job: CoreJobModel, pbJob: JobResourceBase, writer: JobResultWriter)(implicit ec: ExecutionContext): Either[ResultFailed, ResultSuccess] = {

    val smsg = s"Running job-type ${job.jobTypeId.id} ${job.toString} in ${pbJob.path.toString}"
    logger.info(smsg)

    val host = InetAddress.getLocalHost.getHostName
    val startedAt = JodaDateTime.now()

    writer.writeLineStdout(smsg)
    // Block until the job completes
    val jobResult = Try {job.run(pbJob, writer)}
    writer.writeLineStdout(s"Completed running job ${job.jobTypeId.id} Result -> $jobResult")

    val runTime = computeTimeDelta(JodaDateTime.now(), startedAt)
    def toM(state: AnalysisJobStates.JobStates) = s"Job ${pbJob.jobId} completed with state $state in $runTime sec."

    // FIXME. This needs to be cleaned up.
    jobResult match {
      case Success(result) =>
        result match {
          case Right(x: ResultSuccess) =>
            writer.writeLineStdout(toM(AnalysisJobStates.SUCCESSFUL))
            Right(x) // pass through
          case Right(x:ImportAble) =>
            // Only Mark as Successful if all the Files are imported Successfully
            writer.writeLineStdout(s"attempting to import $x")
            val fx = importAbleFile(x, pbJob.jobId)

            // Block until the db import is completed
            Try {
              Await.result(fx, timeout.duration)
            } match {
              case Success(msgs) =>
                logger.info(s"Successfully imported datastore files. $x")
                writer.writeLineStdout("Successfully inserted datastore files:")
                writer.writeLineStdout("  " + msgs.mkString("\n  "))
                Right(ResultSuccess(pbJob.jobId, job.jobTypeId.toString, toM(AnalysisJobStates.SUCCESSFUL), runTime, AnalysisJobStates.SUCCESSFUL, host))
              case Failure(ex) =>
                writer.writeLineStderr(s"Failed to insert datastore files: ${ex.getMessage}")
                Left(ResultFailed(pbJob.jobId, job.jobTypeId.toString, s"Job $job FAILED to import datafiles ${ex.getMessage}", computeTimeDeltaFromNow(startedAt), AnalysisJobStates.FAILED, host))
            }
          case Right(x) =>
            writer.writeLineStdout(s"Successfully completed job ${pbJob.jobId}")
            Right(ResultSuccess(pbJob.jobId, job.jobTypeId.toString, toM(AnalysisJobStates.SUCCESSFUL), runTime, AnalysisJobStates.SUCCESSFUL, host))
          case Left(x) => Left(x)
        }
      case Failure(ex) =>
        val sw = new StringWriter
        ex.printStackTrace(new PrintWriter(sw))
        val emsg = sw.toString
        //val emsg = ex.getMessage
        writer.writeLineStderr(emsg)
        Left(ResultFailed(pbJob.jobId, job.jobTypeId.toString, s"Job $job FAILED $emsg ${ex.getStackTrace}", computeTimeDeltaFromNow(startedAt), AnalysisJobStates.FAILED, host))
    }
  }

}
