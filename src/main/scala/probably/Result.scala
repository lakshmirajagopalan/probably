package probably


trait Result[+T] {}
case class Ok[T](value:T)extends Result[T]
case object StructureNotFound extends Result[Nothing]
