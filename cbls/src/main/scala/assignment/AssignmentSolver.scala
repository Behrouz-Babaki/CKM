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

    if (verbosity > 0)
      println("assignment solver called")

    val distances: Array[Array[Double]] = Array.ofDim[Double](n, k)
    val distancesInt: Array[Array[Int]] = Array.ofDim[Int](n, k)
    for (i <- 0 until n)
      for (j <- 0 until k) {
        distances(i)(j) = CKMeans.l2Distance(points(i), centroids(j))
        distancesInt(i)(j) = (distances(i)(j) * distanceScalingFactor).toInt
      }

    val CblsSolver = new CblsAssignmentSolver(n, k, weightsInt,
      minClusterSize, maxClusterSize, minWeightInt, maxWeightInt, verbosity)

    if (verbosity > 0)
      println("calling CBLS solver")
    val cResult = CblsSolver.solve(distancesInt, previousAssignments)

    if (cResult.violations == 0 && cResult.hasImproved)
      cResult.assignments
    else {
      if (verbosity > 0) {
        println("CBLS failed. Calling Gurobi")
        Console.flush
      }

      val mipSolver = new MipAssignmentSolver(n, k, weights.map(_.toDouble),
        minClusterSize, maxClusterSize,
        minClusterWeight, maxClusterWeight)
      mipSolver.solve(distances, previousAssignments)

      mipSolver.assignments
    }
  }
}