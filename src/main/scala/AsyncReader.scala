import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration.Duration
import java.util.concurrent.Executors

object AsyncReader extends App {
  val dataPath = if (args.length > 0) args(0) else "path"
  val index = FSDirectory.open(Paths.get(dataPath))
  val reader: IndexReader = DirectoryReader.open(index)

  val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
  given ExecutionContext = ExecutionContext.fromExecutorService(threadPool)

  val futures = (0 until reader.maxDoc()).map { i =>
    Future {
      val doc = reader.document(i)
      val source = doc.getBinaryValue("_source").utf8ToString()
      println(source)
    }
  }

  Await.result(Future.sequence(futures), Duration.Inf)

  reader.close()
  index.close()
  threadPool.shutdown()
}
