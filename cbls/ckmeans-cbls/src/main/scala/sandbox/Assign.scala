package sandbox

import scala.util.Random
import oscar.cbls.modeling.CBLSModel
import oscar.cbls.core.objective.Objective
import oscar.cbls.core.objective.CascadingObjective
import oscar.cbls.core.computation.Store
import oscar.cbls.core.constraint.ConstraintSystem
import oscar.cbls.lib.invariant.numeric.Sum
import oscar.cbls.core.computation.CBLSSetVar

object Assign extends CBLSModel with App {
  val n = 100
  val k = 5
  val distances = Array.fill[Int](n, k)((Random.nextDouble * 100).toInt)
  val weights = Array.fill[Int](n)((Random.nextDouble * 100).toInt)

  //TODO better names
  //TODO better initialization

  // create the decision variables
  val assignementsArray = Array.tabulate(n)(l => CBLSIntVar(0, 0 until k, ""))

  val sizes = Array.tabulate(k)(l => CBLSIntVar(0, 0 to n, " "))
  denseCount(assignementsArray, sizes)

  val clusterArray = Array.tabulate(k)(l => CBLSSetVar(s, 0 until n, 0 until n, ""))
  denseCluster(assignementsArray, clusterArray)
  val clusterWeights = Array.tabulate(k)(l => sum(weights, clusterArray(l)))

  //TODO get these from the user
  val size_low = 0
  val size_hi = Math.ceil(n.toDouble / k).toInt
  
  val weight_low =0
  val weight_hi = Math.ceil(weights.reduce(_ + _) / k.toDouble).toInt.toInt

  for (i <- 0 until k) {
    c.add(sizes(i) ge size_low)
    c.add(sizes(i) le size_hi)
    c.add(clusterWeights(i) ge weight_low)
    c.add(clusterWeights(i) le weight_hi)
  }
  c.close()

  // compute the sum of distances
  val selectedDistances = Array.tabulate(n)(l => constantIntElement(assignementsArray(l), distances(l)))

  val maxSumDistances = distances.map(l => l.zipWithIndex.max._2).reduce(_ + _)
  val penalty = (maxSumDistances + 1) * c.violation

  val sumOfDistances = sum(selectedDistances)
  val obj = Objective(sum2(penalty, sumOfDistances))

  val neighborhood = (
    bestSlopeFirst(
      List(
        assignNeighborhood(assignementsArray, "SwitchDataPoints"),
        swapsNeighborhood(assignementsArray, "SwapDataPoints")), refresh = n / 5)
      onExhaustRestartAfter (randomizeNeighborhood(assignementsArray, () => n / 5), 2, obj))

  s.close()

  neighborhood.doAllMoves(obj = obj)
  println(obj.value)

  //TODO if there are violations, solve a constraint system and initialize with that

  println(sizes.mkString(","))
  println(weight_hi)
  println(clusterWeights.mkString(","))
  println(c.violation.value)

}