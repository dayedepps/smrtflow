
package com.pacbio.simulator.scenarios

import java.net.URL
import java.nio.file.{Path, Paths}
import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigException}

import com.pacbio.secondary.smrtserver.client.AnalysisServiceAccessLayer
import com.pacbio.secondary.analysis.externaltools.{PacBioTestData,PbReports}
import com.pacbio.secondary.smrtlink.client.ClientUtils
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.analysis.reports.ReportModels.Report
import com.pacbio.secondary.analysis.constants.FileTypes
import com.pacbio.simulator.{Scenario, ScenarioLoader}
import com.pacbio.simulator.steps._

/**
 * Example config:
 *
 * {{{
 *   smrt-link-host = "smrtlink-bihourly"
 *   smrt-link-port = 8081
 * }}}
 */

// FIXME too much code duplication
object ProjectsScenarioLoader extends ScenarioLoader {
  override def load(config: Option[Config])(implicit system: ActorSystem): Scenario = {
    require(config.isDefined, "Path to config file must be specified for ProjectsScenario")
    require(PacBioTestData.isAvailable, "PacBioTestData must be configured for ProjectsScenario")
    val c: Config = config.get

    // Resolve overrides with String
    def getInt(key: String): Int =
      try {
        c.getInt(key)
      } catch {
        case e: ConfigException.WrongType => c.getString(key).trim.toInt
      }

    new ProjectsScenario(
      c.getString("smrt-link-host"),
      getInt("smrt-link-port"))
  }
}

class ProjectsScenario(host: String, port: Int)
  extends Scenario with VarSteps with ConditionalSteps with IOSteps with SmrtLinkSteps with SmrtAnalysisSteps with ClientUtils {

  override val name = "ProjectsScenario"

  override val smrtLinkClient = new AnalysisServiceAccessLayer(new URL("http", host, port, ""))

  val MSG_PROJ_ERR = "Project database should be initially have just one project"
  val MSG_DS_ERR = "DataSet database should be initially empty"
  val EXIT_SUCCESS: Var[Int] = Var(0)
  val EXIT_FAILURE: Var[Int] = Var(1)

  val jobStatus: Var[Int] = Var()
  val projId: Var[Int] = Var()
  val projects: Var[Seq[Project]] = Var()
  val project: Var[FullProject] = Var()
  val testdata = PacBioTestData()
  val ftSubreads = Var(FileTypes.DS_SUBREADS.fileTypeId)
  val subreads1 = Var(testdata.getFile("subreads-xml"))
  val subreadsUuid1 = Var(dsUuidFromPath(subreads1.get))
  val subreadSets: Var[Seq[SubreadServiceDataSet]] = Var()
  val jobId: Var[UUID] = Var()
  val dsId: Var[Int] = Var()

  val setupSteps = Seq(
    jobStatus := GetStatus,
    fail("Can't get SMRT server status") IF jobStatus !=? EXIT_SUCCESS
  )
  val projectTests = Seq(
    projects := GetProjects,
    fail(MSG_PROJ_ERR) IF projects.mapWith(_.size) !=? 1,
    projId := CreateProject(Var("test-project"), Var("A test project")),
    project := GetProject(projId),
    subreadSets := GetSubreadSets,
    fail(MSG_DS_ERR) IF subreadSets ? (_.nonEmpty),
    jobId := ImportDataSet(subreads1, ftSubreads),
    jobStatus := WaitForJob(jobId),
    fail("Import job failed") IF jobStatus !=? EXIT_SUCCESS,
    project := GetProject(Var(1)),
    fail("Expected one dataset in General Project") IF project.mapWith(_.datasets.size) !=? 1,
    subreadSets := GetSubreadSets,
    project := UpdateProject(projId, project.mapWith(_.asRequest.appendDataSet(subreadSets.mapWith(_(0).id).get))),
    fail("Expected one dataset in project") IF project.mapWith(_.datasets.size) !=? 1,
    project := GetProject(Var(1)),
    fail("Expected no datasets in General Project") IF project.mapWith(_.datasets.size) !=? 0
  )
  override val steps = setupSteps ++ projectTests
}
