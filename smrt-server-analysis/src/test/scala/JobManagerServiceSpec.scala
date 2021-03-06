import akka.actor.{ActorRefFactory, ActorSystem}
import com.pacbio.common.actors._
import com.pacbio.common.auth._
import com.pacbio.common.dependency.{ConfigProvider, SetBindings, Singleton}
import com.pacbio.common.models.UserRecord
import com.pacbio.common.services.ServiceComposer
import com.pacbio.common.services.utils.StatusGeneratorProvider
import com.pacbio.common.time.FakeClockProvider
import com.pacbio.secondary.analysis.configloaders.{EngineCoreConfigLoader, PbsmrtpipeConfigLoader}
import com.pacbio.secondary.analysis.jobtypes.SimpleDevJobOptions
import com.pacbio.secondary.smrtlink.JobServiceConstants
import com.pacbio.secondary.smrtlink.actors._
import com.pacbio.secondary.smrtlink.app.SmrtLinkConfigProvider
import com.pacbio.secondary.smrtlink.services.{JobManagerServiceProvider, JobRunnerProvider}
import com.pacbio.secondary.smrtlink.testkit.TestUtils
import com.pacbio.secondary.smrtlink.tools.SetupMockData
import com.pacbio.secondary.smrtserver.models.SecondaryAnalysisJsonProtocols
import com.pacbio.secondary.smrtserver.services.jobtypes.SimpleServiceJobTypeProvider
import com.typesafe.config.Config
import org.specs2.mutable.Specification
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

import scala.concurrent.duration.FiniteDuration
import slick.driver.PostgresDriver.api._


class JobManagerServiceSpec extends Specification
with Specs2RouteTest
with SetupMockData
with JobServiceConstants with TestUtils{

  sequential

  import SecondaryAnalysisJsonProtocols._

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(5, "sec"))

  val INVALID_JWT = "invalid.jwt"

  object TestProviders extends
  ServiceComposer with
  JobManagerServiceProvider with
  SimpleServiceJobTypeProvider with
  StatusGeneratorProvider with
  EventManagerActorProvider with
  JobsDaoProvider with
  JobsDaoActorProvider with
  TestDalProvider with
  SmrtLinkConfigProvider with
  JobRunnerProvider with
  PbsmrtpipeConfigLoader with
  EngineCoreConfigLoader with
  AuthenticatorImplProvider with
  JwtUtilsProvider with
  InMemoryLogDaoProvider with
  ActorSystemProvider with
  ConfigProvider with
  FakeClockProvider with
  SetBindings {

    override final val jwtUtils: Singleton[JwtUtils] = Singleton(() => new JwtUtils {
      override def parse(jwt: String): Option[UserRecord] = if (jwt == INVALID_JWT) None else Some(UserRecord(jwt))
    })

    override val config: Singleton[Config] = Singleton(testConfig)
    override val actorSystem: Singleton[ActorSystem] = Singleton(system)
    override val actorRefFactory: Singleton[ActorRefFactory] = actorSystem
    override val baseServiceId: Singleton[String] = Singleton("test-service")
    override val buildPackage: Singleton[Package] = Singleton(getClass.getPackage)
  }

  override val dao: JobsDao = TestProviders.jobsDao()
  override val db: Database = dao.db
  val totalRoutes = TestProviders.jobManagerService().prefixedRoutes
  TestProviders.eventManagerActor()

  step(setupDb(TestProviders.dbConfig))

  "Smoke test for 'simple' job type" should {
    "Simple job should run" in {
      val record = SimpleDevJobOptions(7, 13)
      Post(s"/$ROOT_SERVICE_PREFIX/job-manager/jobs/simple", record) ~> totalRoutes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get Simple types" in {
      Get(s"/$ROOT_SERVICE_PREFIX/job-manager/jobs/simple") ~> totalRoutes ~> check {
        status.isSuccess must beTrue
      }
    }
  }
}
