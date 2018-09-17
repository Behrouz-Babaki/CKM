package assignment

import cluster.CKMeans

case class assignmentResult( assignments: Array[Int], violations: Int, hasImproved: Boolean)

class OscarAssignmentSolver(
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
    previousAssignments: Array[Int]): assignmentResult = {
    val distances: Array[Array[Int]] = Array.ofDim[Int](n, k)
    for (i <- 0 until n)
      for (j <- 0 until k)
        distances(i)(j) = (CKMeans.l2Distance(points(i), centroids(j)) * distanceScalingFactor).toInt

    val CblsSolver = new CBLSAssignmentSolver(n, k, weightsInt,
      minClusterSize, maxClusterSize, minWeightInt, maxWeightInt, verbosity)
    val result = CblsSolver.solve(distances, previousAssignments)
    
    result
  }
}