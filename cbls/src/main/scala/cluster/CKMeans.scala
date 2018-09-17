package cluster

import assignment.AssignmentSolver
import scala.util.Random
import java.io.File

object CKMeans extends App {

  var centroids = Array.ofDim[Double](0, 0)
  var assignment = Array.ofDim[Int](0)
  var points = Array.ofDim[Double](0, 0)
  var n = 0
  var m = 0
  var k = 0
  var WCSS = Double.PositiveInfinity
  var unsatisfiable = false
  var bestWCSS = Double.PositiveInfinity
  var bestCentroids = Array.ofDim[Double](k, m)
  var bestAssignment = Array.ofDim[Int](n)

  def cluster(
    _points:   Array[Array[Double]],
    weights:   Array[Double],
    _k:        Int,
    minWeight: Double               = Double.NegativeInfinity,
    maxWeight: Double               = Double.PositiveInfinity,
    minSize:   Int                  = Int.MinValue,
    maxSize:   Int                  = Int.MaxValue,
    epsilon:   Double               = 1e-3,
    outFile:   File                 = null,
    repeats:   Int                  = 1,
    verbosity: Int                  = 0): Unit = {

    points = _points
    n = points.length
    m = points(0).length
    k = _k

    val solver = new AssignmentSolver(points, n, k, weights,
      minSize, maxSize,
      minWeight, maxWeight,
      verbosity = verbosity)

    // run multiple times and then choose the best run
    for (_ <- 0 until repeats) {
      cluster

      if (WCSS < bestWCSS) {
        bestWCSS = WCSS;
        bestCentroids = centroids;
        bestAssignment = assignment;
      }
    }

    def cluster(): Unit = {      
      initialize

      if (!unsatisfiable) {
        var prevWCSS: Double = 0
        var done = false
        do {
          val result = solver.assignmentStep(centroids, assignment)
          if (result != null) {
            assignment = result
            prevWCSS = WCSS
            updateStep
            println(WCSS + " :: " + prevWCSS + " :: " + (1 - (WCSS / prevWCSS)) + " :: " + epsilon)
          } else {
            done = true
          }
        } while (!done && (epsilon < 1 - (WCSS / prevWCSS)));
      }

      if (outFile != null)
        printResults
    }

    def initialize(): Unit = {
      plusplus
      assignment = solver.assignmentStep(centroids, null)
      if (assignment == null)
        unsatisfiable = true
      else {
        updateStep
      }
      println(assignment.mkString(","))
    }

    def updateStep(): Unit = {
      for (i <- 0 until k)
        for (j <- 0 until m)
          centroids(i)(j) = 0;

      val clustSize = Array.ofDim[Int](k)

      for (i <- 0 until n) {
        clustSize(assignment(i)) += 1
        for (j <- 0 until m)
          centroids(assignment(i))(j) += points(i)(j);
      }

      for (i <- 0 until k)
        for (j <- 0 until m)
          centroids(i)(j) /= clustSize(i);

      WCSS = 0
      for (i <- 0 until m)
        WCSS += l2Distance(points(i), centroids(assignment(i)));
    }

  }

  def l2Distance(x: Array[Double], y: Array[Double]): Double = {
    if (x.length != y.length)
      throw new IllegalArgumentException("dimension error");
    var dist: Double = 0;
    for (i <- 0 until x.length)
      dist += Math.pow(x(i) - y(i), 2)
    dist
  }

  def plusplus(): Unit = {
    centroids = Array.ofDim[Double](k, m)
    val distToClosestCentroid = Array.ofDim[Double](n)
    val weightedDistribution = Array.ofDim[Double](n)

    val gen = new Random
    var choose: Int = 0;

    for (c <- 0 until k) {
      if (c == 0)
        choose = gen.nextInt(n)
      else {
        for (p <- 0 until k) {
          val tempDistance = l2Distance(points(p), centroids(c - 1)); 
          if (c == 1)
            distToClosestCentroid(p) = tempDistance
          else { // c != 1
            if (tempDistance < distToClosestCentroid(p))
              distToClosestCentroid(p) = tempDistance
          }
          if (p == 0)
            weightedDistribution(0) = distToClosestCentroid(0)
          else
            weightedDistribution(p) = weightedDistribution(p - 1) + distToClosestCentroid(p)
        }

        val rand = gen.nextDouble
        var j: Int = n
        while (choose == 0 && j > 0) {
          if (rand > weightedDistribution(j - 1) / weightedDistribution(n - 1)) {
            choose = j; 
          } else 
            choose = 0;
          j -= 1
        }
      }
      for (i <- 0 until m)
        centroids(c)(i) = points(choose)(i)
    }
  }

  def printResults(): Unit = {
    //TODO write results to an output file
  }

}