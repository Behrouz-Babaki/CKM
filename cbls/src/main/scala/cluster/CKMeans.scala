package cluster

import assignment.AssignmentSolver
import scala.util.Random
import java.io.File
import oscar.util.OutFile

object CKMeans extends App {

  var centroids = Array.ofDim[Double](0, 0)
  var assignment = Array.ofDim[Int](0)
  var points = Array.ofDim[Double](0, 0)
  var n = 0
  var m = 0
  var k = 0
  var unsatisfiable = false
  var bestWCSS = Double.PositiveInfinity
  var bestCentroids = Array.ofDim[Double](k, m)
  var bestAssignment = Array.ofDim[Int](n)
  var verbosity: Int = 0
  
  def verbprint(i: Int, j: Any) = {if (this.verbosity >= i) println(j)}

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
    this.verbosity = verbosity

    val solver = new AssignmentSolver(points, n, k, weights,
      minSize, maxSize,
      minWeight, maxWeight,
      verbosity = verbosity)

    // run multiple times and then choose the best run
    for (_ <- 0 until repeats) {
      cluster

      val WCSS: Double = {
        if (unsatisfiable) Double.PositiveInfinity
        else (0 until n).map(i => l2Distance(points(i), centroids(assignment(i)))).sum
      }

      if (WCSS < bestWCSS) {
        bestWCSS = WCSS;
        bestCentroids = centroids;
        bestAssignment = assignment;
      }
    }

    def cluster(): Unit = {
      unsatisfiable = false
      initialize

      var done = unsatisfiable
      while (!done) {
        val currentAssignment = solver.assignmentStep(centroids, assignment)
        if (currentAssignment != null) {
          assignment = currentAssignment
          centroids = updateCentroids(assignment)
          verbprint(2, "cluster assignements:" + assignment.mkString(","))
        } else {
          verbprint(1, "no clustering found in this iteration. terminating.")
          done = true
        }
      }

      if (outFile != null)
        printResults(outFile)
    }

    def initialize(): Unit = {
      randomCentroids
      verbprint(1, "finding the first set of assignments")
      assignment = solver.assignmentStep(centroids, null)
      if (assignment == null) {
        unsatisfiable = true
        verbprint(1, "This problem is unsatisfiable!")
      }
      else
        centroids = updateCentroids(assignment)
    }

    def updateCentroids(a: Array[Int]): Array[Array[Double]] = {
      verbprint(2, "calculating the new centroids")
      val newCentroids = Array.ofDim[Double](k, m)

      for (i <- 0 until k)
        for (j <- 0 until m)
          newCentroids(i)(j) = 0;

      val clustSize = Array.ofDim[Int](k)

      for (i <- 0 until n) {
        clustSize(a(i)) += 1
        for (j <- 0 until m)
          newCentroids(a(i))(j) += points(i)(j);
      }

      for (i <- 0 until k)
        for (j <- 0 until m)
          newCentroids(i)(j) /= clustSize(i);

      newCentroids
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

  def randomCentroids(): Unit = {
    verbprint(2, "randomly selecting the first set of centroids")
    centroids = Array.ofDim[Double](k, m)
    val selectedPoints = Random.shuffle(points.toList).take(k)
    for (i <- 0 until k)
      for (j <- 0 until m)
        centroids(i)(j) = selectedPoints(i)(j)
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

  def printResults(outFile: File): Unit = {
    CsvIo.write(outFile.getAbsolutePath, assignment)
  }

}