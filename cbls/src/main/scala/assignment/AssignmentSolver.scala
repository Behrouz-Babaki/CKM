package assignment

import cluster.CKMeans

class AssignmentSolver(
  points: Array[Array[Double]],
  n:      Int, k: Int,
  weights:               Array[Double],
  minClusterSize:        Int,
  maxClusterSize:        Int,
  minClusterWeight:      Double,
  maxClusterWeight:      Double,
  weightScalingFactor:   Int           = 1000,
  distanceScalingFactor: Int           = 1000,
  verbosity:             Int           = 0) {

  def verbprint(i: Int, j: Any) = { if (this.verbosity >= i) println(j) }

  val weightsInt: Array[Int] = weights.map(i => (i * weightScalingFactor).toInt)
  val minWeightInt: Int = (minClusterWeight * weightScalingFactor).toInt
  val maxWeightInt: Int = (maxClusterWeight * weightScalingFactor).toInt

  def assignmentStep(
    centroids:           Array[Array[Double]],
    previousAssignments: Array[Int]): Array[Int] = {

    val distances: Array[Array[Double]] = Array.ofDim[Double](n, k)
    val distancesInt: Array[Array[Int]] = Array.ofDim[Int](n, k)
    for (i <- 0 until n)
      for (j <- 0 until k) {
        distances(i)(j) = CKMeans.l2Distance(points(i), centroids(j))
        distancesInt(i)(j) = (distances(i)(j) * distanceScalingFactor).toInt
      }

    def computeQuality(a: Array[Int]): Double = {
      if (a == null) Double.PositiveInfinity
      else (0 until n).map(i => distances(i)(a(i))).sum
    }

    val previousQuality = computeQuality(previousAssignments)
    def improvedQuality(a: Array[Int]): Boolean = {
      (a != null) && (previousAssignments == null || computeQuality(a) < previousQuality)
    }

    verbprint(1, "solving the assignment problem using local search")
    val CblsSolver = new CblsAssignmentSolver(n, k, weightsInt,
      minClusterSize, maxClusterSize, minWeightInt, maxWeightInt, verbosity)

    var currentAssignments: Array[Int] = null
    val cblsAssignments = CblsSolver.solve(distancesInt, previousAssignments)

    if (verbosity >= 1) {
      if (cblsAssignments != null)
        println("local search found a solution")
      else
        println("local search did NOT find any solution")
    }

    if (improvedQuality(cblsAssignments)) {
      currentAssignments = cblsAssignments
      verbprint(1, "using the solution found by local search")
    } else {
      if (currentAssignments != null)
        verbprint(1, "the solution found by local search does not improve the clustering quality")
      verbprint(1, "solving the assignment problem using the mip solver")
      val mipSolver = new MipAssignmentSolver(n, k, weights,
        minClusterSize, maxClusterSize, minClusterWeight, maxClusterWeight)
      val mipAssignments = mipSolver.solve(distances, previousAssignments)
      if (improvedQuality(mipAssignments)) {
        currentAssignments = mipAssignments
        verbprint(1, "using a solution found by the mip solver")
      }
    }

    currentAssignments
  }
}