package pl.edu.icm.coansys.citations.reducers

import collection.JavaConversions._
import org.testng.annotations.Test
import org.apache.hadoop.mrunit.mapreduce.{MapReduceDriver, ReduceDriver}
import pl.edu.icm.coansys.citations.data._
import org.apache.hadoop.io.{Text, BytesWritable}
import org.apache.hadoop.mapreduce.Mapper

/**
 * @author Mateusz Fedoryszak (m.fedoryszak@icm.edu.pl)
 */
class AuthorJoinerTest {
  val cit0 = MatchableEntity.fromParameters(id = "cit0", rawText = "Alojzy Krasinski").data.toByteArray
  val cit1 = MatchableEntity.fromParameters(id = "cit1", rawText = "Jan Kowalski").data.toByteArray
  val cit2 = MatchableEntity.fromParameters(id = "cit2", rawText = "Maciej Nowak").data.toByteArray
  val doc0 = MatchableEntity.fromParameters(id = "doc0", author = "Alojzy Krasinski").data.toByteArray
  val doc1 = MatchableEntity.fromParameters(id = "doc1", author = "Jan Kowalski").data.toByteArray
  val doc2 = MatchableEntity.fromParameters(id = "doc2", author = "Maciej Nowak").data.toByteArray

  val cits = List(cit0, cit1, cit2)
  val docs = List(doc0, doc1, doc2)

  @Test(groups = Array("fast"))
  def basicTest() {
    val bytesList =
      docs.map(new MarkedBytesWritable(_, marked = true)) ++ cits.map(new MarkedBytesWritable(_))


    val driver = ReduceDriver.newReduceDriver(new AuthorJoiner)
    driver.addInput(new MarkedText("key"), bytesList)
//    driver.addInput()
//    driver.addInput(new Text("cit0"), new BytesWritable(cit0))
//    driver.addInput(new Text("cit1"), new BytesWritable(cit1))
//    driver.addInput(new Text("cit2"), new BytesWritable(cit2))
//    driver.addInput(new Text("cit0"), new BytesWritable(cit0))
//    driver.addInput(new Text("cit2"), new BytesWritable(cit2))
//    driver.addInput(new Text("cit0"), new BytesWritable(cit0))
//    driver.addOutput(new Text("cit1"), new BytesWritable(cit1))
    for {
      doc <- docs
      cit <- cits
    } driver.addOutput(new BytesWritable(cit), new BytesWritable(doc))


    driver.runTest(false)
  }

  @Test(groups = Array("fast"))
  def biggerTest() {
    val bigDocs = for {
      doc <- docs
      i <- 0 until 20
    } yield new MarkedBytesWritable(doc, marked = true)
    val bytesList =
      bigDocs ++ cits.map(new MarkedBytesWritable(_))


    val driver = ReduceDriver.newReduceDriver(new AuthorJoiner)
    driver.addInput(new MarkedText("key"), bytesList)
    //    driver.addInput()
    //    driver.addInput(new Text("cit0"), new BytesWritable(cit0))
    //    driver.addInput(new Text("cit1"), new BytesWritable(cit1))
    //    driver.addInput(new Text("cit2"), new BytesWritable(cit2))
    //    driver.addInput(new Text("cit0"), new BytesWritable(cit0))
    //    driver.addInput(new Text("cit2"), new BytesWritable(cit2))
    //    driver.addInput(new Text("cit0"), new BytesWritable(cit0))
    //    driver.addOutput(new Text("cit1"), new BytesWritable(cit1))
    for {
      (doc, cit) <- docs zip cits
      i <- 0 until 20
    } driver.addOutput(new BytesWritable(cit), new BytesWritable(doc))


    driver.runTest(false)
  }
}
