package assignment

import oscar.cp._
import oscar.algo.Inconsistency

class CPAssignmentSolver(n: Int, k: Int, weights: Array[Int],
                         minClusterSize: Int, maxClusterSize: Int,
                         minClusterWeight: Int, maxClusterWeight: Int,
                         distances: Array[Array[Int]], previousAssignments: Array[Int],
                         verbosity: Int = 0) extends CPModel {

  var result: assignmentResult = null

  val assignmentVariables = Array.fill(n)(CPIntVar(0 until k))
  
  val clusterWeightVariables = Array.fill(k)(CPIntVar(minClusterWeight to maxClusterWeight))
  add (binPacking(assignmentVariables, weights, clusterWeightVariables))
  
  for (i <- 0 until k) {
    add(atMost(maxClusterSize, assignmentVariables, i))
    add(atLeast(minClusterSize, assignmentVariables, i))
  }

  if (previousAssignments != null) {
    val ub = distances.zipWithIndex.map(l => l._1(previousAssignments(l._2))).reduce(_ + _)
    add(minAssignment(assignmentVariables, distances, CPIntVar(ub-1)))
  }

  val assignments = Array.ofDim[Int](n)
  search {
    binaryFirstFail(assignmentVariables)
  } onSolution {
    println("solution found")
    for (i <- 0 until n)
      assignments(i) = assignmentVariables(i).value
  }

  val stats = start(nSols = 1)
  val hasImproved = (stats.nSols > 0)
  
  result = assignmentResult(assignments, 0, hasImproved)
}