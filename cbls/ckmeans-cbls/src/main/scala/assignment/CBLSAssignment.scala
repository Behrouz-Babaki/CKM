package assignment

import oscar.cbls.core.computation.CBLSSetVar
import oscar.cbls.core.objective.Objective
import oscar.cbls.modeling.CBLSModel
import oscar.cbls.core.computation.CBLSIntVar

class CBLSAssignmentSolver(n: Int, k: Int, weights: Array[Int],
                           minClusterSize: Int, maxClusterSize: Int,
                           minClusterWeight: Int, maxClusterWeight: Int,
                           verbosity: Int = 0) extends CBLSModel {

  def solve(distances: Array[Array[Int]], previousAssignments: Array[Int]): assignmentResult = {

    val assignmentVariables: Array[CBLSIntVar] = Array.ofDim[CBLSIntVar](n)

    if (previousAssignments == null) {
      val closestClusters = distances.map(l => l.zipWithIndex.min._2)
      for (i <- 0 until n)
        assignmentVariables(i) = CBLSIntVar(closestClusters(i), 0 until k, "")
    } else {
      for (i <- 0 until n)
        assignmentVariables(i) = CBLSIntVar(previousAssignments(i), 0 until k, "")
    }

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

    val hasImproved = {
      if (previousAssignments == null)
        true
      else
        (sumOfDistances.value < distances.zipWithIndex.map(l => l._1(previousAssignments(l._2))).reduce(_ + _))
    }

    assignmentResult(assignments, violation, hasImproved)
  }

}