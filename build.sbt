import sbtassembly.AssemblyKeys.{assembly, assemblyJarName, assemblyMergeStrategy}
import sbtassembly.{MergeStrategy, PathList}
import uk.gov.hmrc.DefaultBuildSettings.targetJvm

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)

name := "releaser"

scalaVersion := "2.11.11"
crossScalaVersions := Seq("2.11.11")
targetJvm := "jvm-1.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.5.16",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "commons-io" % "commons-io" % "2.4",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "org.apache.commons" % "commons-compress" % "1.10",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

resolvers += Resolver.typesafeRepo("releases")

headers := EEHeaderSettings()
organizationHomepage := Some(url("https://www.equalexperts.com"))
organization := "com.equalexperts"

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.typesafeRepo("releases")
)

assemblyJarName in assembly := "releaser.jar"

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
  case PathList("play", "core", "server", xs@_*) => MergeStrategy.first
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

artifact in(Compile, assembly) := {
  val art = (artifact in(Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)
