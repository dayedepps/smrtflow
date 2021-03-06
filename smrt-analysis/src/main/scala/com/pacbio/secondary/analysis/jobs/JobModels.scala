package com.pacbio.secondary.analysis.jobs

import java.nio.file.{Path, Paths}
import java.util.UUID
import java.net.URL

import org.joda.time.{DateTime => JodaDateTime}
import spray.json._

object AnalysisJobStates {

  trait JobStates {
    val stateId: Int
  }

  trait Completed

  case object CREATED extends JobStates {
    val stateId = 1
  }

  case object SUBMITTED extends JobStates {
    val stateId = 2
  }

  case object RUNNING extends JobStates {
    val stateId = 3
  }

  case object TERMINATED extends JobStates with Completed {
    val stateId = 4
  }

  case object SUCCESSFUL extends JobStates with Completed {
    val stateId = 5
  }

  case object FAILED extends JobStates with Completed {
    val stateId = 6
  }

  case object UNKNOWN extends JobStates {
    val stateId = 7
  }

  // sugar
  val VALID_STATES = Seq(CREATED, SUBMITTED, RUNNING, TERMINATED, SUCCESSFUL, FAILED, UNKNOWN)

  val COMPLETED_STATES = Seq(TERMINATED, SUCCESSFUL, FAILED)

  def isCompleted(state: JobStates): Boolean = COMPLETED_STATES contains state

  def intToState(i: Int): Option[JobStates] = VALID_STATES.map(x => (x.stateId, x)).toMap.get(i)

  // This is NOT case sensitive
  def toState(s: String): Option[JobStates] = VALID_STATES.map(x => (x.toString.toLowerCase, x)).toMap.get(s.toLowerCase)

}

// mirrors pbcommand.models.common.TaskOptionTypes
object OptionTypes {
  sealed trait OptionType {
    val optionTypeId: String
  }

  case object STR extends OptionType { val optionTypeId = "string" }
  case object INT extends OptionType { val optionTypeId = "integer" }
  // XXX this is a little sloppy, as internally we use Double
  case object FLOAT extends OptionType { val optionTypeId = "float" }
  case object BOOL extends OptionType { val optionTypeId = "boolean" }
  case object CHOICE extends OptionType { val optionTypeId = "choice_string" }
  case object CHOICE_INT extends OptionType { val optionTypeId = "choice_integer" }
  case object CHOICE_FLOAT extends OptionType { val optionTypeId = "choice_float" }

}

object JobModels {

  // Constants used across all Job types

  object JobConstants {

    // Default Output job files
    val JOB_STDERR = "pbscala-job.stderr"
    val JOB_STDOUT = "pbscala-job.stdout"

    // This is the DataStore File "master" log. The fundamental log file for the
    // job should be stored here and added to the datastore for downstream consumers
    val DATASTORE_FILE_MASTER_LOG_ID = "pbsmrtpipe::master.log"

    // Event that means the Job state has been changed
    val EVENT_TYPE_JOB_STATUS = "smrtlink_job_status"
    // Event that means the Job Task has changed state
    val EVENT_TYPE_JOB_TASK_STATUS = "smrtlink_job_task_status"

  }

  object JobTypeIds {
    val CONVERT_FASTA_BARCODES = JobTypeId("convert-fasta-barcodes")
    val CONVERT_FASTA_REFERENCE = JobTypeId("convert-fasta-reference")
    val CONVERT_RS_MOVIE = JobTypeId("convert-rs-movie")
    val DELETE_DATASETS = JobTypeId("delete-datasets")
    val DELETE_JOB = JobTypeId("delete-job")
    val EXPORT_DATASETS = JobTypeId("export-datasets")
    val IMPORT_DATASET = JobTypeId("import-dataset")
    val IMPORT_DATASTORE = JobTypeId("import-datastore")
    val MERGE_DATASETS = JobTypeId("merge-datasets")
    val MOCK_PBSMRTPIPE = JobTypeId("mock-pbsmrtpipe")
    val PBSMRTPIPE = JobTypeId("pbsmrtpipe")
    val PBSMRTPIPE_DIRECT = JobTypeId("pbsmrtpipe-direct")
    val SIMPLE = JobTypeId("simple")
  }

  // Uses the pbsmrtpipe Task Id format (e.g., "pbsmrtpipe.tasks.my_task")
  // the 'id' is the short name
  case class JobTypeId(id: String) {
    def fullName = s"pbscala.job_types.$id"
  }

  trait JobResourceBase {
    val jobId: UUID
    val path: Path
    val state: AnalysisJobStates.JobStates
  }

  // This is a terrible name
  case class JobResource(
      jobId: UUID,
      path: Path,
      state: AnalysisJobStates.JobStates) extends JobResourceBase

  trait JobResult {
    val uuid: UUID
    val jobType: String
    val message: String
    val runTimeSec: Int
    val state: AnalysisJobStates.JobStates
    val host: String
  }

  // This needs to be fixed.
  case class ResultSuccess(uuid: UUID, jobType: String, message: String, runTimeSec: Int, state: AnalysisJobStates.JobStates, host: String) extends JobResult

  case class ResultFailed(uuid: UUID, jobType: String, message: String, runTimeSec: Int, state: AnalysisJobStates.JobStates, host: String) extends JobResult

  case class NoAvailableWorkError(message: String)

  // New Job Models
  case class RunnableJob(job: CoreJob, state: AnalysisJobStates.JobStates)

  // There's a bit of awkwardness here due to the PrimaryKey of the job having a Int and UUID
  case class RunnableJobWithId(id: Int, job: CoreJob, state: AnalysisJobStates.JobStates)

  // This should only have the "completed" jobOptions states
  case class JobCompletedResult(uuid: UUID, state: AnalysisJobStates.JobStates)


  /**
    * General Job Event data model.
    *
    * This can communicate finer granularity of
    *
    * @param eventId     Globally Unique id for the event.
    * @param jobId       Job Id
    * @param state       state of computational unit (either EngineJob or JobTask)
    * @param message     status message of event
    * @param createdAt   When the message was created
    * @param eventTypeId event type id
    */
  case class JobEvent(
      eventId: UUID,
      jobId: Int,
      state: AnalysisJobStates.JobStates,
      message: String,
      createdAt: JodaDateTime,
      eventTypeId: String = JobConstants.EVENT_TYPE_JOB_STATUS)


  /**
    * Core "Engine" Job data model
    *
    * Note, the jobTypeId should map to a unique schema for the jsonSettings provided.
    * For example, for jobTypeId, "pbsmrtpipe", the jsonSettings JSON must have a pipelineId key
    * that is a String.
    *
    *
    * @param id id of the Job (unique relative to the SL System)
    * @param uuid Globally unique job identifier
    * @param name Display name of task
    * @param comment User comment
    * @param createdAt when the Job was created
    * @param updatedAt when the job was last updated
    * @param state current state of the job
    * @param jobTypeId job type id
    * @param path path to job output directory
    * @param jsonSettings JSON format of the job options (this structure will be consistent with the job type id)
    * @param createdBy user that created the Job
    * @param smrtlinkVersion SL System version
    * @param smrtlinkToolsVersion SL tools version
    * @param isActive if the job is active. Not Active jobs will not be displayed by default
    * @param errorMessage error message if the job is an Error state.
    */
  case class EngineJob(
      id: Int,
      uuid: UUID,
      name: String,
      comment: String,
      createdAt: JodaDateTime,
      updatedAt: JodaDateTime,
      state: AnalysisJobStates.JobStates,
      jobTypeId: String,
      path: String,
      jsonSettings: String,
      createdBy: Option[String],
      smrtlinkVersion: Option[String],
      smrtlinkToolsVersion: Option[String],
      isActive: Boolean = true,
      errorMessage: Option[String] = None) {

      def isComplete: Boolean = AnalysisJobStates.isCompleted(this.state)
      def isSuccessful: Boolean = this.state == AnalysisJobStates.SUCCESSFUL
      def isRunning: Boolean = this.state == AnalysisJobStates.RUNNING

      def apply(
          id: Int,
          uuid: UUID,
          name: String,
          comment: String,
          createdAt: JodaDateTime,
          updatedAt: JodaDateTime,
          stateId: Int,
          jobTypeId: String,
          path: String,
          jsonSettings: String,
          createdBy: Option[String],
          smrtlinkVersion: Option[String],
          smrtlinkToolsVersion: Option[String],
          isActive: Boolean = true,
          errorMessage: Option[String] = None) = {

          // This might not be the best idea.
          val state = AnalysisJobStates.intToState(stateId) getOrElse AnalysisJobStates.UNKNOWN

          EngineJob(id, uuid, name, comment, createdAt, updatedAt, state, jobTypeId, path, jsonSettings, createdBy, smrtlinkVersion, smrtlinkToolsVersion, isActive, errorMessage)
      }
  }

  /**
    * A Single Engine Job has many sub-computational units of work. These are a JobTask.
    *
    * @param uuid         Globally unique id of the Task
    * @param jobId        Job Id
    * @param taskId       task id (unique in the context of a single job)
    * @param taskTypeId   Tool Contract Id
    * @param name         Display name of task
    * @param state        state of the Task
    * @param createdAt    when the task was created (Note, this is not necessarily when the task started to run
    * @param updatedAt    last time the task state was updated
    * @param errorMessage error message (if state in error state)
    */
  case class JobTask(uuid: UUID,
                     jobId: Int,
                     taskId: String,
                     taskTypeId: String,
                     name: String,
                     state: String,
                     createdAt: JodaDateTime,
                     updatedAt: JodaDateTime,
                     errorMessage: Option[String])

  /**
    * Update the state and status message of Task of a Job. If the task is in
    * the error state, the detailed error message will be propagated.
    *
    * @param jobId        Job id the task belongs to
    * @param uuid         Globally unique Task identifier
    * @param state        state of the Task (this needs to be consistent with the allowed analysis job states)
    * @param message      status Message
    * @param errorMessage Detailed Error Message of Job Task
    */
  case class UpdateJobTask(jobId: Int,
                           uuid: UUID,
                           state: String,
                           message: String,
                           errorMessage: Option[String])

  // This is too pbsmtpipe-centric. This should be generalized or defined a base trait
  case class AnalysisJobResources(
      path: Path,
      tasksDir: Path,
      workflowDir: Path,
      logDir: Path,
      htmlDir: Path,
      datastoreJson: Path,
      entryPointsJson: Path,
      jobReportJson: Path)

  trait ImportAble

  /**
   * Core DataStore File
   *
   * @param uniqueId    UUID of the DataStore file
   * @param sourceId    General string to provide context of it's origin
   * @param fileTypeId  FileType Id (FIXME, this should be a FileTypes.FileType)
   * @param fileSize    Size of the file
   * @param createdAt   created at time of the file
   * @param modifiedAt  modified timestamp of the file
   * @param path        Absolute path to the file
   * @param isChunked   Was the file an intermediate "chunked" file that was generated.
   * @param name        Display Name of the File
   * @param description Description of the File
   */
  case class DataStoreFile(
      uniqueId: UUID,
      sourceId: String,
      fileTypeId: String,
      fileSize: Long,
      createdAt: JodaDateTime,
      modifiedAt: JodaDateTime,
      path: String,
      isChunked: Boolean = false,
      name: String,
      description: String) extends ImportAble {

    def fileExists: Boolean = Paths.get(path).toFile.exists
  }

  // Container for file created from a Job
  case class DataStoreJobFile(
      jobId: UUID,
      dataStoreFile: DataStoreFile)



  case class PacBioDataStore(createdAt: JodaDateTime, updatedAt: JodaDateTime, version: String, files: Seq[DataStoreFile]) extends ImportAble

  // Should think about making this a Path
  case class BoundEntryPoint(entryId: String, path: String)

  // Used in pipeline Templates. Name is the display name
  case class EntryPoint(entryId: String, fileTypeId: String, name: String)

  trait PacBioBaseOption {
    type In
    val id: String
    val value: In
    val optionTypeId: String
  }

  trait PipelineBaseOption extends PacBioBaseOption {
    val name: String
    val description: String

    def asServiceOption: ServiceTaskOptionBase
  }

  trait PipelineChoiceOption extends PipelineBaseOption {
    val choices: Seq[In]
    def validate(v: In): In = {
      if (choices.toSet contains v) v
      else throw new UnsupportedOperationException(s"Value $v is not an allowed choice")
    }
    def applyValue(v: In): PipelineChoiceOption
  }

  trait PipelineIntOptionBase extends PipelineBaseOption {
    type In = Int
  }

  trait PipelineDoubleOptionBase extends PipelineBaseOption {
    type In = Double
  }

  trait PipelineStrOptionBase extends PipelineBaseOption {
    type In = String
  }

  trait PipelineBooleanOptionBase extends PipelineBaseOption {
    type In = Boolean
  }

  case class PipelineStrOption(id: String, name: String, value: String, description: String) extends PipelineStrOptionBase {
    val optionTypeId = OptionTypes.STR.optionTypeId
    def asServiceOption = ServiceTaskStrOption(id, value, optionTypeId)
  }


  case class PipelineIntOption(id: String, name: String, value: Int, description: String) extends PipelineIntOptionBase {
    val optionTypeId = OptionTypes.INT.optionTypeId
    def asServiceOption = ServiceTaskIntOption(id, value, optionTypeId)
  }

  case class PipelineDoubleOption(id: String, name: String, value: Double, description: String) extends PipelineDoubleOptionBase {
    val optionTypeId = OptionTypes.FLOAT.optionTypeId
    def asServiceOption = ServiceTaskDoubleOption(id, value, optionTypeId)
  }

  case class PipelineBooleanOption(id: String, name: String, value: Boolean, description: String) extends PipelineBooleanOptionBase {
    val optionTypeId = OptionTypes.BOOL.optionTypeId
    def asServiceOption = ServiceTaskBooleanOption(id, value, optionTypeId)
  }

  case class PipelineChoiceStrOption(id: String, name: String, value: String, description: String, choices: Seq[String]) extends PipelineStrOptionBase with PipelineChoiceOption {
    val optionTypeId = OptionTypes.CHOICE.optionTypeId
    val _ = validate(value)

    def applyValue(v: String): PipelineChoiceStrOption = copy(value=validate(v))
    def asServiceOption = ServiceTaskStrOption(id, value, optionTypeId)
  }

  case class PipelineChoiceIntOption(id: String, name: String, value: Int, description: String, choices: Seq[Int]) extends PipelineIntOptionBase with PipelineChoiceOption {
    val optionTypeId = OptionTypes.CHOICE_INT.optionTypeId
    val _ = validate(value)

    def applyValue(v: Int): PipelineChoiceIntOption = copy(value=validate(v))
    def asServiceOption = ServiceTaskIntOption(id, value, optionTypeId)
  }

  case class PipelineChoiceDoubleOption(id: String, name: String, value: Double, description: String, choices: Seq[Double]) extends PipelineDoubleOptionBase with PipelineChoiceOption {
    val optionTypeId = OptionTypes.CHOICE_FLOAT.optionTypeId
    val _ = validate(value)

    def applyValue(v: Double): PipelineChoiceDoubleOption = copy(value=validate(v))
    def asServiceOption = ServiceTaskDoubleOption(id, value, optionTypeId)
  }

  trait ServiceTaskOptionBase extends PacBioBaseOption {
  }

  trait ServiceTaskStrOptionBase extends ServiceTaskOptionBase{ type In = String }
  trait ServiceTaskIntOptionBase extends ServiceTaskOptionBase{ type In = Int }
  trait ServiceTaskBooleanOptionBase extends ServiceTaskOptionBase{ type In = Boolean }
  trait ServiceTaskDoubleOptionBase extends ServiceTaskOptionBase{ type In = Double }

  case class ServiceTaskStrOption(id: String, value: String, optionTypeId: String = OptionTypes.STR.optionTypeId) extends ServiceTaskStrOptionBase
  case class ServiceTaskIntOption(id: String, value: Int, optionTypeId: String = OptionTypes.INT.optionTypeId) extends ServiceTaskIntOptionBase
  case class ServiceTaskBooleanOption(id: String, value: Boolean, optionTypeId: String = OptionTypes.BOOL.optionTypeId) extends ServiceTaskBooleanOptionBase
  case class ServiceTaskDoubleOption(id: String, value: Double, optionTypeId: String = OptionTypes.FLOAT.optionTypeId) extends ServiceTaskDoubleOptionBase

  // Raw (aka) Direct Options. Minimal options used to call pbsmrtpipe
  case class PbsmrtpipeDirectJobOptions(
      pipelineId: String,
      entryPoints: Seq[BoundEntryPoint],
      taskOptions: Seq[ServiceTaskOptionBase],
      workflowOptions: Seq[ServiceTaskOptionBase])

  // pbsmrtpipe/smrtflow Pipelines
  case class PipelineTemplate(
      id: String,
      name: String,
      description: String,
      version: String,
      options: Seq[PipelineBaseOption],
      taskOptions: Seq[PipelineBaseOption],
      entryPoints: Seq[EntryPoint],
      tags: Seq[String],
      presets: Seq[PipelineTemplatePreset])

  // templateId refers to the PipelineTemplate Id
  case class PipelineTemplatePreset(
      presetId: String,
      pipelineId: String,
      options: Seq[ServiceTaskOptionBase],
      taskOptions: Seq[ServiceTaskOptionBase])


  // View Rules Models
  case class PipelineOptionViewRule(id: String, hidden: Boolean, advanced: Boolean)

  case class PipelineTemplateViewRule(
      id: String,
      name: String,
      description: String,
      taskOptions: Seq[PipelineOptionViewRule])

  // FIXME(mkocher)(2016-8-18) All of these View rules should probable be migrated to a central location
  case class DataStoreFileViewRule(sourceId: String, fileTypeId: String, isHidden: Boolean, name: Option[String], description: Option[String])
  case class PipelineDataStoreViewRules(pipelineId: String, rules: Seq[DataStoreFileViewRule], smrtlinkVersion: String)

}
