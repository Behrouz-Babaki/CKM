package cluster

import java.io.File
import assignment._

case class Config(
  dataFile:      File   = new File("."),
  weightFile:    File   = new File("."),
  k:             Int    = -1,
  minWeight:     Double = Double.NegativeInfinity,
  maxWeight:     Double = Double.PositiveInfinity,
  numRepeats:    Int    = 1,
  maxIterations: Int    = 999,
  epsilon:       Double = 1e-3,
  minSize:       Int    = -1,
  maxSize:       Int    = Int.MaxValue,
  outputFile:    File   = new File("."),
  verbosity:     Int    = 0)

object Runner extends App {

  val parser = argsParser()

  parser.parse(args, Config()) match {
    case Some(config) =>
      System.err.println("Start CKMeans on " + config.dataFile.getAbsolutePath +
        " and " + config.weightFile.getAbsolutePath)

      // read the data and weights
      val points = CsvIo.read(config.dataFile)
      val weights = CsvIo.flatten(CsvIo.read(config.weightFile))
      
      val minSize = {if (config.minSize >= 0) config.minSize else 0}
      val maxSize = {if (config.minSize < Int.MaxValue) config.maxSize else points.length}
      val minWeight = {if (config.minWeight != Double.NegativeInfinity) config.minWeight else weights.filter(_ < 0).reduce(_ + _)}
      val maxWeight = {if (config.maxWeight != Double.PositiveInfinity) config.maxWeight else weights.filter(_ > 0).reduce(_ + _)}

      // run K-means
      val startTime = System.currentTimeMillis
      println("running CKmeans")
      println(config.verbosity)
      CKMeans.cluster(points, weights, config.k,
        minWeight = minWeight,
        maxWeight = maxWeight,
        minSize = minSize,
        maxSize = maxSize,
        epsilon = config.epsilon,
        outFile = config.outputFile,
        repeats = config.numRepeats,
        verbosity = config.verbosity)
      val elapsed = System.currentTimeMillis - startTime

      System.out.println("Clustering took " + elapsed.toDouble / 1000 + " seconds");
      System.out.println();

      // get output
      val centroids = CKMeans.centroids

      // print output
      for (i <- 0 until config.k)
        System.out.println("(" + centroids(i)(0) + ", " + centroids(i)(1) + ")");
      System.out.println();

      println("UNSAT?: " + CKMeans.unsatisfiable)

    case None =>
  }

  def argsParser(): scopt.OptionParser[Config] = {
    new scopt.OptionParser[Config]("CKMeans") {
      head("CKMeans", "0.2")

      opt[File]("data-file") required () valueName ("<file>") action { (x, c) => c.copy(dataFile = x)
      } validate { x =>
        if (x.exists()) success else failure("<DATA File> does not exist")
      } text ("the input DATA file")

      opt[Int]("k") required () valueName ("<#clusters>") action { (x, c) =>
        c.copy(k = x)
      } validate { x =>
        if (x > 0) success else failure("Value <k> must be > 0")
      } text ("number of clusters")

      opt[File]("weight-file") required () valueName ("<file>") action { (x, c) => c.copy(weightFile = x)
      } validate { x =>
        if (x.exists()) success else failure("<WEIGHT File> does not exist")
      } text ("the input WEIGHT file")

      opt[Double]("min-weight") required () valueName ("<w>") action { (x, c) =>
        c.copy(minWeight = x)
      } text ("minimum weight in each cluster")

      opt[Double]("max-weight") required () valueName ("<w>") action { (x, c) =>
        c.copy(maxWeight = x)
      } text ("maximum weight in each cluster")

      opt[Int]("min-size") optional () valueName ("<#instances>") action { (x, c) =>
        c.copy(minSize = x)
      } validate { x =>
        if (x >= 0) success else failure("value <min-size> must be >= 0")
      } text ("minimum number of instances in each cluster")

      opt[Int]("max-size") optional () valueName ("<#instances>") action { (x, c) =>
        c.copy(maxSize = x)
      } validate { x =>
        if (x >= 0) success else failure("value <max-size> must be >= 0")
      } text ("maximum number of instances in each cluster")

      opt[Int]("num-repeats") optional () valueName ("<n>") action { (x, c) =>
        c.copy(numRepeats = x)
      } text ("run the algorithm <n> times and pick the best solution")

      opt[Int]("max-iterations") optional () action { (x, c) =>
        c.copy(maxIterations = x)
      } text ("maximum number of iterations of the main loop")

      opt[Double]("tolerance") optional () action { (x, c) =>
        c.copy(epsilon = x)
      } text ("the (scaled) distance between the centers in two consecutive iterations that indicates convergence")

      opt[File]("output-file") optional () valueName ("<OUTFILE>") action { (x, c) =>
        c.copy(outputFile = x)
      } validate { x =>
        if (x.exists() || x.createNewFile()) success else failure("<OUTFILE> can not be created")
      } text ("store the result in OUTFILE")

      opt[Int]("verbosity") optional () valueName ("<VERBOSITY>") action { (x, c) =>
        c.copy(verbosity = x)
      } text ("set versbosity level to <VERBOSITY>")

      help("help") text ("Usage of CKMeans")

      override def showUsageOnError = true
    }
  }

}