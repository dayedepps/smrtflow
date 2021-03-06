// Necessary for sqlite to not have class loading JNI problem
fork := true

Revolver.settings

mainClass in (Compile, run) := Some("com.pacbio.secondary.smrtlink.app.SmrtLinkSmrtServer")

//assemblyMergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
//  case PathList("application.conf") => MergeStrategy.first
//  case p if p.endsWith("eclipse.inf") => MergeStrategy.first
//  case PathList("org", "slf4j", xs @ _*) => MergeStrategy.first
//  case PathList("org", "eclipse", xs @ _*) => MergeStrategy.first
//  case "logback.xml" => MergeStrategy.first
//  case x => old(x)
//}
//}

packSettings

packMain := Map(
  "smrt-server-link" -> "com.pacbio.secondary.smrtlink.app.SmrtLinkSmrtServer",
  "smrt-server-events" -> "com.pacbio.secondary.smrtlink.app.SmrtEventServerApp"
)


packGenerateWindowsBatFile := false