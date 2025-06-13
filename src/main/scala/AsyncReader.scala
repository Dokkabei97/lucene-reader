import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import java.util.concurrent.Executors

object AsyncReader extends App {
  val dataPath = if (args.length > 0) args(0) else "path"

  val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
  given ExecutionContext = ExecutionContext.fromExecutorService(threadPool)

  val readFuture = Future {
    val index = FSDirectory.open(Paths.get(dataPath))
    val reader: IndexReader = DirectoryReader.open(index)
    try {
      (0 until reader.maxDoc()).foreach { i =>
        val doc = reader.document(i)
        val source = doc.getBinaryValue("_source").utf8ToString()
        println(source)
      }
    } finally {
      reader.close()
      index.close()
    }
  }

  readFuture.onComplete {
    case _ => threadPool.shutdown()
  }

  Await.result(readFuture, Duration.Inf)
}
