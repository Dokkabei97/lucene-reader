import org.apache.spark.sql.SparkSession
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

/** Example Spark job that uses AsyncReader within each partition. */
object SparkReader:
  def main(args: Array[String]): Unit =
    val indexPath = if args.nonEmpty then args(0) else "path"
    val spark = SparkSession.builder()
      .appName("LuceneAsyncReader")
      .master("local[*]")
      .getOrCreate()

    // In a real job you would distribute the work across partitions.
    // Here we just run once on the driver for demonstration.
    given ExecutionContext = scala.concurrent.ExecutionContext.global
    val future = AsyncReader.readDocuments(indexPath)
    Await.result(future.map(_.foreach(println)), Duration.Inf)

    spark.stop()
