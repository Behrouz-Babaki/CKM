package scopt

object Run extends App {
  val tString = "--data-file ../data/players.data --k 3 --weight-file ../data/players.weights --min-weight -10 --max-weight 1000 --verbosity 1"
  val tArgs = tString.split("\\s+")

  try {

    val choice = args(0)
    val arguments = args.drop(1)
    choice match {
      case "gurobi" => mip.TestGurobi.main(arguments)
      case "test"   => cluster.Runner.main(tArgs)
      case "assign" => assignment.TestAssign.main(arguments)
      case _        => printUsage
    }

  } catch {
    case _: java.lang.ArrayIndexOutOfBoundsException => printUsage
  }

  def printUsage(): Unit = { println("Usage: Run <test/assign> ARGUMENTS") }
}
