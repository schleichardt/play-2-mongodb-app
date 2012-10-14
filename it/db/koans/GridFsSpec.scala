package info.schleichardt.ic2.db.koans

import com.mongodb.casbah
import casbah.gridfs.{GridFS, GridFSDBFile}
import java.util.UUID
import org.specs2.execute._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.Imports._
import play.api.Application
import info.schleichardt.ic2.db.DbTestTools._
import org.specs2.matcher.MatchResult
import java.io.ByteArrayOutputStream

class GridFsSpec extends Specification {
  val filename = "data.txt"
  val bucketName = "test-fs"
  val MimeText: String = "text/plain"
  val textFilecontents: String = "Foo\nBar"

  "with Casbah you" can {
    "store, retrieve files" in {
      //http://api.mongodb.org/scala/casbah/current/tutorial.html#gridfs-with-casbah

      implicit val app = FakeApplication()

      def retrieveFile(filename: String)(implicit app: Application): Option[GridFSDBFile] = salatPlugin.gridFS(bucketName).findOne(filename)

      def createFile() {
        val textAsBinary = textFilecontents.getBytes
        // FileWriteOp of GridFS.apply uses API of GridFSInputFile
        salatPlugin.gridFS(bucketName)(textAsBinary) {
          file =>
            file.contentType = MimeText
            file.filename = filename
        }
      }

      def assertFileNotExist() = {
        retrieveFile(filename) must beNone
      }

      def assertFileCreated() = {
        val fileOption: Option[GridFSDBFile] = retrieveFile(filename)
        fileOption must beSome
        fileOption.get.filename must beSome(filename)
        fileOption.get.contentType must beSome(MimeText)
        val outputStream = new ByteArrayOutputStream
        fileOption.get.writeTo(outputStream)
        new String(outputStream.toByteArray) === textFilecontents
      }

      def dropFile() {
        salatPlugin.gridFS(bucketName).remove(filename)
      }

      running(app) {
        dropFile()
        assertFileNotExist()
        createFile()
        assertFileCreated()
        dropFile()
        assertFileNotExist()
      }
    }
  }
}