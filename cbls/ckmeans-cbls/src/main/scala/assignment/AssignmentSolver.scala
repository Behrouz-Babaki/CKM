package assignment

abstract class AssignmentSolver {
  def assignmentStep(points: Array[Array[Double]], centroids: Array[Array[Double]]): Array[Int]
}

class SimpleAssignment() extends AssignmentSolver {

  def distance(x: Array[Double], y: Array[Double]): Double = {

    var dist: Double = 0
    var i = 0
    while (i < x.length) {
      dist += Math.abs((x(i) - y(i)) * (x(i) - y(i)));
      i += 1
    }

    dist
  }

  def assignmentStep(points: Array[Array[Double]], centroids: Array[Array[Double]]): Array[Int] = {
    val m: Int = points.length
    val k: Int = centroids.length
    val assignment = Array.ofDim[Int](m);

    var tempDist: Double = 0
    var minValue: Double = 0
    var minLocation: Int = 0

    var i: Int = 0
    var j: Int = 0
    i = 0
    while (i < m) {
      minLocation = 0
      minValue = Double.PositiveInfinity
      j = 0
      while (j < k) {
        tempDist = distance(points(i), centroids(j));
        if (tempDist < minValue) {
          minValue = tempDist;
          minLocation = j;
        }
        j += 1
      }

      assignment(i) = minLocation;
      i += 1
    }
    assignment
  }

}
