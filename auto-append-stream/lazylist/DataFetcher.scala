import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

case class Data(value: Int) // Define your data type

object DataFetcher {
  def fetchDataFromAPI(): Future[Data] = Future {
    // Simulate fetching data from the REST API
    Thread.sleep(1000) // Simulate network delay
    Data(scala.util.Random.nextInt(100)) // Generate random data
  }
}

object Main extends App {
  def fetchNextData(): LazyList[Data] = {
    val futureData = DataFetcher.fetchDataFromAPI()
    // Convert the Future to a LazyList
    LazyList.from {
      futureData.value match {
        case None => // The future hasn't completed yet, so recursively call fetchNextData
          Seq(fetchNextData()).toIterator
        case Some(Success(data)) => // The future completed successfully, so append the data
          Seq(data).toIterator ++ fetchNextData()
        case Some(Failure(ex)) => // The future completed with an exception, so log the error and recursively call fetchNextData
          println(s"Error fetching data: ${ex.getMessage}")
          Seq(fetchNextData()).toIterator
      }
    }
  }

  // Obtain a LazyList of data from the API
  val dataLazyList = fetchNextData()

  // Print the first 10 elements of the LazyList
  println("First 10 elements:")
  dataLazyList.take(10).foreach(println)

  // Sleep for a while to allow the API calls to complete
  Thread.sleep(15000)
}
