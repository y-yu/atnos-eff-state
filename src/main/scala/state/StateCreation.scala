package state

import org.atnos.eff.Eff

object StateCreation extends StateCreation

trait StateCreation extends StateTypes {
  def get[S, R: _state[S, *]]: Eff[R, S] =
    Eff.send[State[S, *], R, S](State.Get())

  def set[S, R: _state[S, *]](value: S): Eff[R, Unit] =
    Eff.send[State[S, *], R, Unit](State.Set(value))
}
