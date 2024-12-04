import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

lazy val akkaHttpVersion = "10.2.10"
lazy val akkaVersion    = "2.6.20"

fork := true

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.8"
    )),
    name := "scala-backend",
    libraryDependencies ++= Seq(
      "ch.megard" %% "akka-http-cors" % "1.2.0"
      "org.scala-lang" % "scala-library" % "2.13.8",
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.4.11",

      // Test dependencies
      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.17"        % Test
      ),

      // Specify the main class here
      Compile / mainClass := Some("com.example.Main"),  // Replace with your actual main class

      scalacOptions ++= Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-Xlint",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen"
      ),
      // Memory settings
      javaOptions ++= Seq(
        "-Xmx256m",
        "-Xms64m"
      ),

      // Force the server to bind to 0.0.0.0
      run / fork := true,
    
     // Production settings
      Universal / javaOptions ++= Seq(
        "-Dconfig.resource=production.conf",
        s"-Dhttp.port=${sys.env.getOrElse("PORT", "9000")}",
        "-Dhttp.address=0.0.0.0"
      )
    )

// Ensure clean state
onLoad in Global := (onLoad in Global).value andThen { state =>
  "clean" :: state
}
