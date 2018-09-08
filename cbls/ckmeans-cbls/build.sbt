import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.5",
      version      := "0.1"
    )),
    name := "CKmeans-CLBS",
    javaOptions += "-Xmx8G",
    resolvers += "Oscar Snapshots" at "http://artifactory.info.ucl.ac.be/artifactory/libs-snapshot-local/",
    resolvers += Resolver.sonatypeRepo("public"),
    libraryDependencies += "oscar" %% "oscar-cbls" % "4.1.0-SNAPSHOT" withSources(),
    libraryDependencies += "oscar" %% "oscar-util" % "4.1.0-SNAPSHOT" withSources(),
    libraryDependencies += scalaTest % Test,
    mainClass in (Compile, packageBin) := Some("example.WarehouseLocation"),
    mainClass in (Compile, run) := Some("example.WarehouseLocation"),
    mainClass in assembly := Some("example.WarehouseLocation"),
    assemblyMergeStrategy in assembly := {
      case x if x.endsWith(".class") => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }    
  )
