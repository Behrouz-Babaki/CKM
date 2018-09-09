package cluster

import java.io.File
import assignment._

case class Config(
  algorithm:     String  = "simple",
  dataFile:      File    = new File("."),
  weightFile:    File    = new File("."),
  k:             Int     = -1,
  minWeight:     Double  = Double.NegativeInfinity,
  maxWeight:     Double  = Double.PositiveInfinity,
  balanced:      Boolean = false,
  maxSizeDiff:   Int     = Int.MaxValue,
  alpha:         Double  = 0.0,
  numRepeats:    Int     = 1,
  maxIterations: Int     = 999,
  tolerance:     Double  = 1e-4,
  minSize:       Int     = -1,
  outputFile:    File    = new File("."))

object Runner extends App {

  val parser = argsParser()

  parser.parse(args, Config()) match {
    case Some(config) =>
      System.err.println("Start CKMeans on " + config.dataFile.getAbsolutePath +
        " and " + config.weightFile.getAbsolutePath)

      var solver: AssignmentSolver = config.algorithm match {
        case "simple" => new SimpleAssignment
        case "cbls" => new CBLSAssignment
      }
      
      // read the data and weights
      val points = CSVreader.read(config.dataFile)
      val weights = CSVreader.flatten(CSVreader.read(config.weightFile))
      
      // run K-means
      val startTime = System.currentTimeMillis
      val clustering: KMeans = new KMeans.Builder(config.k, points).iterations(50).pp(true).epsilon(.001).useEpsilon(true).solver(new SimpleAssignment()).build();
      val elapsed = System.currentTimeMillis - startTime

      System.out.println("Clustering took " + elapsed.toDouble / 1000 + " seconds");
      System.out.println();

      // get output
      val centroids = clustering.getCentroids();
      val WCSS = clustering.getWCSS();
      // int[] assignment = kmean.getAssignment();

      // print output
      for (i <- 0 until config.k)
        System.out.println("(" + centroids(i)(0) + ", " + centroids(i)(1) + ")");
      System.out.println();

      System.out.println("The within-cluster sum-of-squares (WCSS) = " + WCSS);
      System.out.println();

    case None =>
  }

  def argsParser(): scopt.OptionParser[Config] = {
    new scopt.OptionParser[Config]("CKMeans") {
      head("CKMeans", "0.2")

      opt[String]("method") required () valueName ("<algorithm>") action { (x, c) => c.copy(algorithm = x)
      } validate { x =>
        if (List("simple", "cbls") contains x) success else failure("unknown <algorithm>")
      } text ("the algorithm for solving the assignment subproblem")

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

      opt[Int]("max-size-difference") optional () valueName ("<difference>") action { (x, c) =>
        c.copy(maxSizeDiff = x)
      } validate { x =>
        if (x >= 0) success else failure("value <difference> must be >= 0")
      } text ("maximum difference between the size of clusters")

      opt[Double]("alpha") optional () valueName ("<a>") action { (x, c) =>
        c.copy(alpha = x)
      } text ("the degree of acceptable imbalance")

      opt[Int]("num-repeats") optional () valueName ("<n>") action { (x, c) =>
        c.copy(numRepeats = x)
      } text ("run the algorithm <n> times and pick the best solution")

      opt[Int]("max-iterations") optional () action { (x, c) =>
        c.copy(maxIterations = x)
      } text ("maximum number of iterations of the main loop")

      opt[Double]("tolerance") optional () action { (x, c) =>
        c.copy(tolerance = x)
      } text ("the (scaled) distance between the centers in two consecutive iterations that indicates convergence")

      opt[File]("output-file") optional () valueName ("<OUTFILE>") action { (x, c) =>
        c.copy(outputFile = x)
      } validate { x =>
        if (x.exists() || x.createNewFile()) success else failure("<OUTFILE> can not be created")
      } text ("store the result in OUTFILE")

      help("help") text ("Usage of CKMeans")

      override def showUsageOnError = true
    }
  }

}