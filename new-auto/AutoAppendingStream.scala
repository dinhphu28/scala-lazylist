import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AutoAppendingStreamFuture(fetchData: => Future[Int], threshold: Int) {

  private val baseLazyList: LazyList[Future[Int]] = LazyList.continually(fetchData)

  def lazyList: LazyList[Future[Int]] = baseLazyList

  def processData(callback: Int => Unit): Unit = {
    val z: Unit = lazyList.take(threshold).foreach { future =>
      // future.onComplete {
      //   case scala.util.Success(value) => callback(value)
      //   case scala.util.Failure(exception) => println(s"Error fetching data: ${exception.getMessage}")
      // }
      future.flatMap(p => Future { callback(p) })
    }

    val t: LazyList[Future[Unit]] = lazyList.take(threshold).map { future =>
      future.flatMap(p => Future { callback(p) })
    }

    val y = Future.traverse(lazyList.take(threshold)) { future =>
      future.flatMap(p => Future { callback(p) })
    }

    val u: Future[LazyList[Unit]] = Future.sequence(lazyList.take(threshold).map { future =>
      future.flatMap(p => Future { callback(p) })
    })
  }
}
