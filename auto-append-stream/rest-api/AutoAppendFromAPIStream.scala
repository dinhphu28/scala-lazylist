package rest.api
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.io.StdIn

class AutoAppendingStream(fetchData: => Future[Option[Int]], threshold: Int) {
  private def appendNext(current: Future[Option[Int]], acc: List[Int]): Stream[Int] = {
    if (acc.length < threshold) {
      Await.result(current, Duration.Inf) match {
        case Some(nextElem) =>
          Stream.cons(nextElem, appendNext(fetchData, acc :+ nextElem))
        case None =>
          Stream.empty
      }
    } else {
      Stream.empty
    }
  }

  private val baseStream: Stream[Int] = appendNext(fetchData, Nil)

  def stream: Stream[Int] = baseStream
}

object Main extends App {
  // Example REST API call (replace with your own)
  def fetchDataFromAPI: Future[Option[Int]] = Future {
    // Simulating a REST API call here
    Thread.sleep(1000) // Simulate latency
    Some(scala.util.Random.nextInt(100)) // Return some random data
  }

  // Create an instance of AutoAppendingStream with a threshold of 10
  val streamGenerator = new AutoAppendingStream(fetchDataFromAPI, 20)

  // Obtain the lazy evaluated stream
  val stream = streamGenerator.stream

  // // Print the first 15 elements of the stream
  // (1 to 15).foreach { _ =>
  //   stream.headOption match {
  //     case Some(elem) =>
  //       println(elem)
  //       stream.tail // Move to the next element
  //     case None => // Stream is empty
  //       println("Stream is empty")
  //   }
  // }

  stream.foreach(p => println(s"Value is: $p"))

  // Keep the application running to allow asynchronous API calls to complete
  StdIn.readLine()
}
