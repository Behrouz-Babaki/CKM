package scopt

object Run extends App {
  val tString = "--method simple --data-file ../../data/players.data --k 3 --weight-file ../../data/players.weights --min-weight -10 --max-weight 1000"
  val tArgs = tString.split("\\s+")

  try {
    val choice = args(0)
    val arguments = args.drop(1)
    choice match {
      case "KM"   => cluster.KMeans.main(arguments)
      case "CL"   => cluster.Runner.main(arguments)
      case "test" => cluster.Runner.main(tArgs)
      case "sandbox" => sandbox.Assign.main(arguments)
      case _      => printUsage
    }
  } catch {
    case _: java.lang.ArrayIndexOutOfBoundsException => printUsage
  }

  def printUsage(): Unit = { println("Usage: Run <KM/NQ/WL/CSV> ARGUMENTS") }
}
