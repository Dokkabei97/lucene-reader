import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future, blocking, Await}
import scala.concurrent.duration.Duration
import java.util.concurrent.Executors

/** Reads documents from a Lucene index using a thread pool. */
object AsyncReader:

  /**
   * Reads all documents from the index at the given path. The returned
   * [[scala.concurrent.Future]] completes when all documents have been loaded
   * and the underlying index resources are closed.
   */
  def readDocuments(path: String)(using ec: ExecutionContext): Future[Seq[String]] =
    val index = FSDirectory.open(Paths.get(path))
    val reader = DirectoryReader.open(index)

    val docsF = Future.traverse(0 until reader.maxDoc()) { i =>
      Future(blocking {
        val doc = reader.document(i)
        doc.getBinaryValue("_source").utf8ToString()
      })
    }

    docsF.andThen { case _ =>
      reader.close()
      index.close()
    }

  @main def run(path: String): Unit =
    val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
    given ExecutionContext = ExecutionContext.fromExecutorService(threadPool)

    val resultF = readDocuments(path)
    resultF.onComplete(_ => threadPool.shutdown())
    // for CLI usage we wait, but callers may handle the Future themselves
    Await.result(resultF.map(_.foreach(println)), Duration.Inf)
