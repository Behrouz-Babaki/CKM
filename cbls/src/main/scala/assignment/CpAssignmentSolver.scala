package assignment

import oscar.cp._
import oscar.algo.Inconsistency

class CPAssignmentSolver(n: Int, k: Int, weights: Array[Int],
                         minClusterSize: Int, maxClusterSize: Int,
                         minClusterWeight: Int, maxClusterWeight: Int,
                         distances: Array[Array[Int]], previousAssignments: Array[Int],
                         verbosity: Int = 0) extends CPModel {

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

  var assignments: Array[Int] = null
  search {
    binaryFirstFail(assignmentVariables)
  } onSolution {
    println("solution found")
    assignments = Array.ofDim[Int](n)
    for (i <- 0 until n)
      assignments(i) = assignmentVariables(i).value
  }

  val stats = start(nSols = 1)
  val hasImproved = (stats.nSols > 0)
  
}