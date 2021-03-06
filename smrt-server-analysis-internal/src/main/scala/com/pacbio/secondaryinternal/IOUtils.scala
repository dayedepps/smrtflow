package com.pacbio.secondaryinternal

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging

import scala.io.Source
import spray.json._
import com.pacbio.secondaryinternal.models.{ReseqConditions, ResolvedConditions, ServiceCondition}


object IOUtils extends LazyLogging{

  def parseConditionCsv(path: Path): Seq[ServiceCondition] =
    parseConditionCsv(Source.fromFile(path.toFile))


  def parseConditionCsv(sx: Source): Seq[ServiceCondition] = {
    sx.getLines.drop(1).toSeq.map(x => parseLine(x.split(",").map(_.trim):_*))
  }

  def parseLine(args: String*): ServiceCondition = {
    parseLine(args(0), args(1), args(2).toInt)
  }

  def parseLine(condId : String, host : String, jobId : Int): ServiceCondition = {
    val spHost = host split ":"
    // See: https://confluence.pacificbiosciences.com/display/SL/On-site+SMRT+Link+servers
    val port = 
      if (spHost.length == 2) spHost(1).toInt
      else
        spHost(0) match {
          case "smrtlink-release" => 9091
          case _ => 8081
        }
    ServiceCondition(condId, spHost(0), port, jobId)
  }

  private def writeString(sx: String, path: Path) = {
    val bw = new BufferedWriter(new FileWriter(path.toFile))
    bw.write(sx)
    bw.close()
    path
  }

  def writeResolvedConditions(resolvedConditions: ResolvedConditions, path: Path): ResolvedConditions = {
    import InternalAnalysisJsonProcotols._
    logger.debug(s"Writing resolved conditions to $path")
    writeString(resolvedConditions.toJson.prettyPrint.toString, path)
    resolvedConditions
  }

  def writeReseqConditions(reseqConditions: ReseqConditions, path: Path): ReseqConditions = {
    import InternalAnalysisJsonProcotols._
    logger.info(s"Writing reseq conditions to $path")
    writeString(reseqConditions.toJson.prettyPrint.toString, path)
    reseqConditions
  }

  def loadReseqConditions(path: Path): ReseqConditions = {
    import InternalAnalysisJsonProcotols._
    val sx = io.Source.fromFile(path.toFile).mkString
    sx.parseJson.convertTo[ReseqConditions]
  }
}