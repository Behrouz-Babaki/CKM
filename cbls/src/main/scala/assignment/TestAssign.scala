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

  for (i <- 0 until 10) {

    val distances = Array.fill[Int](n, k)((Random.nextDouble * 100).toInt)
    val solver = new CblsAssignmentSolver(n, k, weights, minClusterSize, maxClusterSize,
      minClusterWeight, maxClusterWeight)
    val result = solver.solve(distances, null)
    println(result.assignments.mkString(","))
    println("violations: ", result.violations)

    if (result.violations > 0) {
      val mipSolver = new MipAssignmentSolver(n, k, weights.map(_.toDouble),
        minClusterSize, maxClusterSize,
        minClusterWeight, maxClusterWeight);
      mipSolver.solve(distances.map(_.map(_.toDouble)), null)

      if (mipSolver.hasImproved)
        println(mipSolver.assignments.mkString(","))
      else
        print("UNSAT")
    }
  }
}

