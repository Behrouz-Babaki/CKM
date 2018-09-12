package sandbox

import scala.util.Random
import oscar.cbls.core.computation.CBLSIntVar
import oscar.cbls.modeling.CBLSModel
import oscar.cbls.core.computation.CBLSSetVar

object wcss extends CBLSModel with App {
  val n = 10
  val d = 3
  val k = 3

  val instances = Array.fill[Int](n, d)((Random.nextDouble * 100).toInt)
  val assignementsArray = Array.tabulate(n)(l => CBLSIntVar(0, 0 until k, ""))
  val clusterArray = Array.tabulate(k)(l => CBLSSetVar(s, 0 until n, 0 until n, ""))
  denseCluster(assignementsArray, clusterArray)
  
  
  println(assignementsArray.deep.mkString(","))
  println(clusterArray.deep.mkString(","))
}