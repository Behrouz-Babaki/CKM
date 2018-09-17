package assignment

import scala.util.Random

object TestAssign extends App {
  val n = 100
  val k = 5
  val weights = Array.fill[Int](n)((Random.nextDouble * 100).toInt)
  val minClusterSize = 0
  val maxClusterSize = Math.ceil(n.toDouble / k).toInt

//  val minClusterWeight = 0
  val maxClusterWeight = Math.ceil(weights.reduce(_ + _) / k.toDouble).toInt
  val minClusterWeight = maxClusterWeight

  val distances = Array.fill[Int](n, k)((Random.nextDouble * 100).toInt)
  val solver = new CBLSAssignmentSolver(n, k, weights, minClusterSize, maxClusterSize,
    minClusterWeight, maxClusterWeight)
  val result = solver.solve(distances, null)
  println(result.assignments.mkString(","))
  println("violations: ", result.violations)

  if (result.violations > 0) {
    val cpSolver = new CPAssignmentSolver(n, k, weights,
      minClusterSize, maxClusterSize,
      minClusterWeight, maxClusterWeight,
      distances, null)

    val cpResult = cpSolver.result
    if (result.hasImproved)
      println(result.assignments.mkString(","))
    else
      print("UNSAT")
  }
}

