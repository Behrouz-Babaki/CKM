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

    val CblsSolver = new CblsAssignmentSolver(n, k, weightsInt,
      minClusterSize, maxClusterSize, minWeightInt, maxWeightInt, verbosity)

    var currentAssignments: Array[Int] = null
    val cblsAssignments = CblsSolver.solve(distancesInt, previousAssignments)
    if (cblsAssignments != null)
      println("CBLS found solution!")
    if (improvedQuality(cblsAssignments)) {
      currentAssignments = cblsAssignments
      println("CBLS SUCCEEDED")
    }
    else {
      println("CBLS FAILED")
      val mipSolver = new MipAssignmentSolver(n, k, weights,
        minClusterSize, maxClusterSize, minClusterWeight, maxClusterWeight)
      val mipAssignments = mipSolver.solve(distances, previousAssignments)
      if (improvedQuality(mipAssignments))
        currentAssignments = mipAssignments
    }
    
    currentAssignments
  }
}