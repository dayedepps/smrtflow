import java.io.File
import java.nio.file.{Path, Paths}

import com.pacbio.secondary.analysis.jobs.JobModels.PacBioDataStore
import com.pacbio.secondary.analysis.jobs.SecondaryJobJsonProtocol
import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import scala.io.Source

import spray.json._

/**
 *
 * Created by mkocher on 7/15/15.
 */
class SanityDataStoreSpec extends Specification
with LazyLogging
with SecondaryJobJsonProtocol{

  sequential

  val rootDir = "/datastores"

  "DataStore loading from JSON" should {
    "pbsmrtpipe and pbscala created datastores" in {

      def loadDataStore(file: File): PacBioDataStore = {
        val contents = Source.fromFile(file).getLines.mkString
        val jsonAst = contents.parseJson
        jsonAst.convertTo[PacBioDataStore]
      }

      val root = getClass.getResource(rootDir)
      val p = Paths.get(root.toURI)
      val files = p.toFile.listFiles.filter(_.isFile).toList
      val datastores = files.map(x => loadDataStore(x))
      1 must beEqualTo(1)
    }
  }

}
