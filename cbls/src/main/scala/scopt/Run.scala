package scopt

object Run extends App {
  val tString = "--data-file ../data/players.data --k 3 --weight-file ../data/players.weights --min-weight -10 --max-weight 1000 --verbosity 1"
  val tArgs = tString.split("\\s+")
  val tSmall = "--data-file ../data/small.data --k 5 --weight-file ../data/small.weights --min-weight -5 --max-weight 5 --min-size 2 --verbosity 1".split("\\s+")
  val tMedium = "--data-file ../data/medium.data --k 15 --weight-file ../data/medium.weights --min-weight -5 --max-weight 5 --min-size 2 --verbosity 1".split("\\s+")
  val tLarge = "--data-file ../data/large.data --k 30 --weight-file ../data/large.weights --min-weight -5 --max-weight 5 --min-size 2 --verbosity 1".split("\\s+")

  try {

    val choice = args(0)
    val arguments = args.drop(1)
    choice match {
      case "test"   => cluster.Runner.main(tArgs)
      case "small"  => cluster.Runner.main(tSmall)
      case "medium" => cluster.Runner.main(tMedium)
      case "large"  => cluster.Runner.main(tLarge)
      case "assign" => assignment.TestAssign.main(arguments)
      case _        => printUsage
    }

  } catch {
    case _: java.lang.ArrayIndexOutOfBoundsException => printUsage
  }

  def printUsage(): Unit = { println("Usage: Run <test/assign> ARGUMENTS") }
}
