import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AutoAppendingStream(fetchData: => Future[Option[Int]], threshold: Int) {
  private def appendNext(current: Future[Option[Int]], acc: List[Int]): Future[Stream[Int]] = {
    if (acc.length < threshold) {
      current.flatMap {
        case Some(nextElem) =>
          Future.successful(Stream.cons(nextElem, appendNext(fetchData, acc :+ nextElem)))
        case None =>
          Future.successful(Stream.empty)
      }
    } else {
      Future.successful(Stream.empty)
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
  val streamGenerator = new AutoAppendingStream(fetchDataFromAPI, 10)

  // Obtain the lazy evaluated stream
  val stream = streamGenerator.stream

  // Print the first 15 elements of the stream
  (1 to 15).foreach { _ =>
    stream.headOption match {
      case Some(elem) =>
        println(elem)
        // Move to the next element asynchronously
        stream.tail // This will trigger the next API call asynchronously
      case None => // Stream is empty
        println("Stream is empty")
    }
    // Sleep briefly to allow the asynchronous API call to complete
    Thread.sleep(500) // Simulate processing time
  }

  // Keep the application running
  scala.io.StdIn.readLine()
}
