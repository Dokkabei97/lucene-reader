import org.apache.spark.sql.SparkSession
import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

/** Simple Spark job that reads a Lucene index in parallel. */
object SparkReader {
  def main(args: Array[String]): Unit = {
    val dataPath = if (args.nonEmpty) args(0) else "path"

    val spark = SparkSession.builder().appName("LuceneSparkReader").getOrCreate()
    try {
      val docs = spark.sparkContext
        .parallelize(0 until countDocs(dataPath), spark.sparkContext.defaultParallelism)
        .mapPartitions { it =>
          val index = FSDirectory.open(Paths.get(dataPath))
          val reader: IndexReader = DirectoryReader.open(index)
          val res = it.map { i =>
            val doc = reader.document(i)
            doc.getBinaryValue("_source").utf8ToString()
          }.toList
          reader.close()
          index.close()
          res.iterator
        }

      docs.collect().foreach(println)
    } finally {
      spark.stop()
    }
  }

  private def countDocs(path: String): Int = {
    val index = FSDirectory.open(Paths.get(path))
    val reader = DirectoryReader.open(index)
    val count = reader.maxDoc()
    reader.close()
    index.close()
    count
  }
}

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
