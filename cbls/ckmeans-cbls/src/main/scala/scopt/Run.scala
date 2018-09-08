package scopt

import java.io.File

case class Config(
  typeOfProblem:    String  = "",
  tdbFile:          File    = new File("."),
  bddFile:          File    = new File("."),
  minsup:           Double  = 0.0,
  minexp:           Double  = 0.0,
  maxcard:          Int     = 0,
  collectTraces:    Boolean = false,
  verbose:          Boolean = false,
  traceFile:        File    = new File("."),
  branching:        String  = "top-zero",
  typeOfPropagator: String  = "improved")

object Run extends App {
  try {
    val choice = args(0)
    val arguments = args.drop(1)
    choice match {
      case "KM"  => cluster.KMeans.main(arguments)
      case "NQ"  => example.NQueensEasy.main(arguments)
      case "WL"  => example.WarehouseLocation.main(arguments)
      case "CSV" => cluster.CSVreader.main(arguments)
      case _     => printUsage
    }
  } catch {
    case _: java.lang.ArrayIndexOutOfBoundsException => printUsage
  }

  def printUsage(): Unit = { println("Usage: Run <KM/NQ/WL/CSV> ARGUMENTS") }
}
