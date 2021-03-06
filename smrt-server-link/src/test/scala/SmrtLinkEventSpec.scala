import org.specs2.mutable.Specification

import java.util.UUID
import org.joda.time.{DateTime => JodaDateTime}

import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._
import akka.actor.ActorRefFactory
import spray.json._
import com.pacbio.common.actors.ActorRefFactoryProvider
import com.pacbio.common.auth._
import com.pacbio.common.dependency.{SetBindings, Singleton}
import com.pacbio.common.models._
import com.pacbio.common.services.{PacBioServiceErrors, ServiceComposer}
import com.pacbio.common.time.FakeClockProvider
import com.pacbio.secondary.analysis.configloaders.{EngineCoreConfigLoader, PbsmrtpipeConfigLoader}
import com.pacbio.secondary.smrtlink.{JobServiceConstants, SmrtLinkConstants}
import com.pacbio.secondary.smrtlink.actors._
import com.pacbio.secondary.smrtlink.app.SmrtLinkConfigProvider
import com.pacbio.secondary.smrtlink.services._
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.testkit.TestUtils
import com.pacbio.secondary.smrtlink.tools.SetupMockData
import slick.driver.PostgresDriver.api.Database


class SmrtLinkEventSpec extends Specification
    with Specs2RouteTest
    with SetupMockData
    with PacBioServiceErrors
    with JobServiceConstants
    with SmrtLinkConstants
    with TestUtils{

  import SmrtLinkJsonProtocols._

  sequential

  val INVALID_JWT = "invalid.jwt"

  object TestProviders extends
      ServiceComposer with
      ProjectServiceProvider with
      SmrtLinkConfigProvider with
      PbsmrtpipeConfigLoader with
      EngineCoreConfigLoader with
      JobRunnerProvider with
      EulaServiceProvider with
      DataSetServiceProvider with
      JobsDaoActorProvider with
      EventManagerActorProvider with
      SmrtLinkEventServiceProvider with
      JobsDaoProvider with
      TestDalProvider with
      AuthenticatorImplProvider with
      JwtUtilsProvider with
      FakeClockProvider with
      SetBindings with
      ActorRefFactoryProvider {

    // Provide a fake JwtUtils that uses the login as the JWT, and validates every JWT except for invalidJwt.
    override final val jwtUtils: Singleton[JwtUtils] = Singleton(() => new JwtUtils {
      override def parse(jwt: String): Option[UserRecord] = if (jwt == INVALID_JWT) None else Some(UserRecord(jwt))
    })

    override val actorRefFactory: Singleton[ActorRefFactory] = Singleton(system)
  }

  override val dao: JobsDao = TestProviders.jobsDao()
  override val db: Database = dao.db
  val totalRoutes = TestProviders.routes()

  val exampleEvent = SmrtLinkEvent("test_event", 1, UUID.randomUUID(), JodaDateTime.now(), JsObject.empty)

    step(setupDb(TestProviders.dbConfig))

  "SmrtLink Event endpoint test" should {
    "Create an Event" in {
      Post("/smrt-link/events", exampleEvent) ~> totalRoutes ~> check {
        status.isSuccess must beTrue

        val smrtLinkSystemEvent = responseAs[SmrtLinkSystemEvent]
        smrtLinkSystemEvent.uuid must beEqualTo(exampleEvent.uuid)
      }
    }
  }

}
