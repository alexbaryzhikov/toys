package tutorial

sealed trait IntSet {
  def incl(x: Int): IntSet

  def contains(x: Int): Boolean
}

case class NonEmpty(elem: Int, left: IntSet, right: IntSet) extends IntSet {
  def contains(x: Int): Boolean =
    if (x < elem) left contains x
    else if (x > elem) right contains x
    else true

  def incl(x: Int): IntSet =
    if (x < elem) NonEmpty(elem, left incl x, right)
    else if (x > elem) NonEmpty(elem, left, right incl x)
    else this
}

object Empty extends IntSet {
  override def incl(x: Int): IntSet = NonEmpty(x, Empty, Empty)

  override def contains(x: Int): Boolean = false
}
