package com.s3dropbox.lambda

import com.s3dropbox.lambda.ZipFileIterator.ZipFileEntry
import com.s3dropbox.lambda.DossierArtifactsSpec.EmptyByteArray
import org.mockito.Mockito._

import scala.util.Random.shuffle

class DossierArtifactsSpec extends UnitSpec {

  describe("when given a list of LaTeX and PDF files") {
    it("should pairs the LaTeX and PDF files") {
      val filePairs: List[(String, String)] =
        (0 to 3)
          .map((index: Int) => (s"file_$index.tex", s"file_$index.pdf"))
          .toList

      val fileList: List[String] =
        shuffle(
          filePairs.flatMap((filePair: (String, String)) => List(filePair._1, filePair._2))
        )

      val expected: List[DossierArtifact] =
        filePairs
          .map((filePair: (String, String)) => filePair match {
            case (texFilename: String, pdfFilename: String) =>
              DossierArtifact(
                ZipFileEntry(pdfFilename, EmptyByteArray),
                ZipFileEntry(texFilename, EmptyByteArray)
              )
          })

      assert(
        DossierArtifacts(mockZipFileIterator(fileList)).artifacts == expected
      )
    }
  }

  describe("when given a list containing an odd number of LaTeX files") {
    it("should throw an exception") {
      val fileList: List[String] =
        (0 to 3)
          .flatMap((index: Int) => List(s"file_$index.tex", s"file_$index.pdf"))
          .toList
          .tail
          .reverse

      assertThrows[IllegalStateException] {
        DossierArtifacts(mockZipFileIterator(fileList))
          .artifacts
      }
    }
  }

  describe("when given a list containing two PDFs of the same file") {
    it("should throw an exception") {
      assertThrows[IllegalStateException] {
        DossierArtifacts(mockZipFileIterator(List("file_0.pdf", "file_0.pdf")))
          .artifacts
      }
    }
  }

  describe("when given a list containing a PDF and a JPG of the same file basename") {
    it("should throw an exception") {
      assertThrows[IllegalStateException] {
        DossierArtifacts(mockZipFileIterator(List("file_0.pdf", "file_0.jpg")))
          .artifacts
      }
    }
  }

  describe("when given a list containing a PDF and a LaTeX file with different basenames") {
    it("should throw an exception") {
      assertThrows[IllegalStateException] {
        DossierArtifacts(mockZipFileIterator(List("file_0.pdf", "file_1.tex")))
          .artifacts
      }
    }
  }

  def mockZipFileIterator(fileList: List[String]): ZipFileIterator = {
    val mockIterator: ZipFileIterator = mock[ZipFileIterator]
    when(mockIterator.toList)
      .thenReturn(
        fileList.map(ZipFileEntry(_, EmptyByteArray))
      )
    mockIterator
  }
}

private object DossierArtifactsSpec {
  val EmptyByteArray: Array[Byte] = Array[Byte]()
}