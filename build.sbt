val projectName = "quipu"
val scala3Version = "3.0.2"
val mainPath = Some("quipu.Main")

name := projectName
Compile / mainClass := mainPath
//Run using `runMain quipu.Main "src\\\\main\\\\resources\\\\HelloWorld.quipu"`
//(yes, it didn't work without the extra backslashes (on Windows), I don't know why)

//Automatically run "sbt reload" when build.sbt is changed
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .settings(
    name := "quipu",
    description := "An esoteric programming language inspired by talking knots.",
    version := "0.1.0",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
    ),
    libraryDependencies ++= Seq("org.scala-lang" %% "scala3-library" % scala3Version)
  )
