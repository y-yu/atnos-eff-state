package state

import org.atnos.eff./=

sealed trait State[S, +A]

object State {
  case class Get[S]() extends State[S, S]

  case class Set[S](value: S) extends State[S, Unit]
}

trait StateTypes {
  type _state[S, R] = State[S, *] /= R
}
