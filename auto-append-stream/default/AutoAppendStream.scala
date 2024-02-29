package default
class AutoAppendingStream(source: Iterator[Int], threshold: Int) {
  private def appendNext(current: Iterator[Int], acc: List[Int]): Stream[Int] = {
    if (acc.length < threshold && current.hasNext) {
      val nextElem = current.next()
      Stream.cons(nextElem, appendNext(current, acc :+ nextElem))
    } else {
      Stream.empty
    }
  }

  private val baseStream: Stream[Int] = appendNext(source, Nil)

  def stream: Stream[Int] = baseStream
}

object Main extends App {
  // Example source iterator
  val sourceIterator = Iterator.from(1)

  // Create an instance of AutoAppendingStream with a threshold of 10
  val streamGenerator = new AutoAppendingStream(sourceIterator, 10)

  // Obtain the lazy evaluated stream
  val stream = streamGenerator.stream

  // Print the first 15 elements of the stream
  stream.take(15).foreach(println)
}
