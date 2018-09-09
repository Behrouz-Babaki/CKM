package scopt

object Run extends App {
  try {
    val choice = args(0)
    val arguments = args.drop(1)
    choice match {
      case "KM"  => cluster.KMeans.main(arguments)
      case "NQ"  => example.NQueensEasy.main(arguments)
      case "WL"  => example.WarehouseLocation.main(arguments)
      case "CL" => cluster.Runner.main(arguments)
      case _     => printUsage
    }
  } catch {
    case _: java.lang.ArrayIndexOutOfBoundsException => printUsage
  }

  def printUsage(): Unit = { println("Usage: Run <KM/NQ/WL/CSV> ARGUMENTS") }
}
