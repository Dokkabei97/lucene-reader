import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

object Reader extends App {
  val dataPath = "path"
  val index = FSDirectory.open(Paths.get(dataPath))
  val reader: IndexReader = DirectoryReader.open(index)

  println(s"Number of documents in the index: ${reader.maxDoc()}")
  for (i <- 0 until reader.maxDoc()) {
    val doc = reader.document(i)
    val source = doc.getBinaryValue("_source").utf8ToString()
    println(source)
  }

  reader.close()
  index.close()
}
