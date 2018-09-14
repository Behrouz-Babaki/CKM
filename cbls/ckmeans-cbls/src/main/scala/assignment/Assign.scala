package assignment

import scala.util.Random

import oscar.cbls.core.computation.CBLSSetVar
import oscar.cbls.core.objective.Objective
import oscar.cbls.modeling.CBLSModel
import oscar.cbls.core.computation.CBLSIntVar

object TestAssign extends App {
  val n = 100
  val k = 5
  val weights = Array.fill[Int](n)((Random.nextDouble * 100).toInt)
  val minClusterSize = 0
  val maxClusterSize = Math.ceil(n.toDouble / k).toInt

  val minClusterWeight = 0
  val maxClusterWeight = Math.ceil(weights.reduce(_ + _) / k.toDouble).toInt.toInt

  val distances = Array.fill[Int](n, k)((Random.nextDouble * 100).toInt)
  val a = new Assign(n, k, weights, minClusterSize, maxClusterSize, minClusterWeight, maxClusterWeight)
  val (assignments, violation) = a.assign(distances)
  println(assignments.mkString(","))
  println(violation)
}

class Assign(n: Int, k: Int, weights: Array[Int],
             minClusterSize: Int, maxClusterSize: Int,
             minClusterWeight: Int, maxClusterWeight: Int) extends CBLSModel {

  def assign(distances: Array[Array[Int]]): (Array[Int], Int) = {

    val closestClusters = distances.map(l => l.zipWithIndex.min._2)
    val assignmentVariables: Array[CBLSIntVar] = Array.ofDim[CBLSIntVar](n)
    for (i <- 0 until n)
      assignmentVariables(i) = CBLSIntVar(closestClusters(i), 0 until k, "")

    val clusterSizes = Array.tabulate(k)(l => CBLSIntVar(0, 0 to n, " "))
    denseCount(assignmentVariables, clusterSizes)

    val clusterVariables = Array.tabulate(k)(l => CBLSSetVar(s, 0 until n, 0 until n, ""))
    denseCluster(assignmentVariables, clusterVariables)
    val clusterWeights = Array.tabulate(k)(l => sum(weights, clusterVariables(l)))

    for (i <- 0 until k) {
      c.add(clusterSizes(i) ge minClusterSize)
      c.add(clusterSizes(i) le maxClusterSize)
      c.add(clusterWeights(i) ge minClusterWeight)
      c.add(clusterWeights(i) le maxClusterWeight)
    }
    c.close()

    val maximumSumOfDistances = distances.map(l => l.zipWithIndex.max._1).reduce(_ + _)
    val penalty = (maximumSumOfDistances + 1) * c.violation

    val distanceToSelectedCenters = Array.tabulate(n)(l => constantIntElement(assignmentVariables(l), distances(l)))
    val sumOfDistances = sum(distanceToSelectedCenters)
    val obj = Objective(sum2(penalty, sumOfDistances))

    val neighborhood = (
      bestSlopeFirst(
        List(
          assignNeighborhood(assignmentVariables, "SwitchDataPoints"),
          swapsNeighborhood(assignmentVariables, "SwapDataPoints")), refresh = n / 5)
        onExhaustRestartAfter (randomizeNeighborhood(assignmentVariables, () => n / 5), 10, obj))

    s.close()

    neighborhood.doAllMoves(obj = obj)

    val assignments = Array.tabulate(n)(l => assignmentVariables(l).value)
    val violation = c.violation.value

    (assignments, violation)
  }

}