import java.nio.file.{Path, Paths}

import com.pacbio.secondary.analysis.constants.FileTypes
import com.pacbio.secondary.analysis.jobs.JobModels.{DataStoreFileViewRule, PipelineDataStoreViewRules}
import com.pacbio.secondary.analysis.jobs.SecondaryJobJsonProtocol
import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import spray.json._

import scala.io.Source

/**
  * Created by mkocher on 8/18/16.
  */
class PipelineDataStoreViewRulesSpec extends Specification with SecondaryJobJsonProtocol with LazyLogging {


  val RESOURCE_DIR = "pipeline-datastore-view-rules"

  def getTestResource(name: String): Path = Paths.get(getClass.getResource(s"$RESOURCE_DIR/$name").toURI)

  "Test loading PipelineDataStoreViewRules" should {
    "Sanity test for loading Test file" in {
      val name = "pipeline_datastore_view_rules-dev-01.json"
      val p = getTestResource(name)
      val x = Source.fromFile(p.toFile).mkString
      println(x)
      val pipelineTemplateRules = x.parseJson.convertTo[PipelineDataStoreViewRules]
      pipelineTemplateRules.pipelineId must beEqualTo("pbsmrtpipe.pipelines.dev_01")
    }
    "Convert PipelineDataStoreViewRules to Json" in {
      val r1 = DataStoreFileViewRule("source-id", FileTypes.LOG.fileTypeId, isHidden = false, Option("File Name"), Option("File desc"))
      val r2 = DataStoreFileViewRule("source-id-2", FileTypes.FASTA.fileTypeId, isHidden = true, None, None)
      val p = PipelineDataStoreViewRules("pipeline-id", Seq(r1), "0.1.2-fe4516")
      val sx = p.toJson.prettyPrint
      println(sx)
      (sx.length > 0) must beTrue
    }
    "Load PipelineDataStoreViewRules from JSON string" in {

      val sx =
        """
          |{
          |  "smrtlinkVersion": "1.2.3",
          |  "pipelineId": "pbsmrtpipe.pipelines.dev_01",
          |  "rules": [
          |    {
          |      "fileTypeId": "PacBio.FileTypes.log",
          |      "sourceId": "pbsmrtpipe::pbsmrtpipe.log",
          |      "name": "Name",
          |      "description": "Description",
          |      "isHidden": false
          |    },
          |    {
          |      "fileTypeId": "PacBio.FileTypes.log",
          |      "sourceId": "pbsmrtpipe::master.log",
          |      "isHidden": true,
          |      "description": null,
          |      "name": null
          |    }
          |  ]
          |}
          |
        """.stripMargin

      val pt = sx.parseJson.convertTo[PipelineDataStoreViewRules]
      pt.pipelineId must beEqualTo("pbsmrtpipe.pipelines.dev_01")
    }
  }

}
