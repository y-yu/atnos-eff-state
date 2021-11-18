package state

import org.atnos.eff.*

object StateInterpreter extends StateInterpreter

trait StateInterpreter { self =>
  implicit class RunState[R, A](val eff: Eff[R, A]) {
    def runState[U, S](state: S)(implicit
      m1: Member.Aux[State[S, *], R, U]
    ): Eff[U, (A, S)] =
      self.runState(state)(eff)
  }

  def runState[R, A, U, S](state: S)(eff: Eff[R, A])(implicit
    m1: Member.Aux[State[S, *], R, U]
  ): Eff[U, (A, S)] = {
    def interpretContinuation[X](
      state: S,
      c: Continuation[R, X, A]
    ): Continuation[U, X, (A, S)] =
      Continuation.lift { (x: X) =>
        runState(state)(c(x)).addLast(interpretLast(c.onNone))
      }

    def interpretContinuationWithLast[X](
      state: S,
      c: Continuation[R, X, A],
      last: Last[R]
    ): Continuation[U, X, (A, S)] =
      Continuation.lift { (x: X) =>
        runState(state)(c(x)).addLast(interpretLast(last))
      }

    def interpretLastEff(last: Eff[R, Unit]): Eff[U, Unit] =
      last match {
        case Pure(_, last1) =>
          interpretLast(last1).value.map(_.value).getOrElse(Eff.pure(()))

        case Impure(NoEffect(a), c, last1) =>
          interpretLastEff(c(a).addLast(last1))

        case Impure(u: Union[_, _], c, last1) =>
          m1.project(u) match {
            case Right(_) =>
              Eff.pure(())
            case Left(other) =>
              Impure(
                other,
                Continuation.lift(
                  (x: u.X) => interpretLastEff(c(x)),
                  interpretLast(c.onNone)
                ),
                interpretLast(last1)
              )
          }

        case ap @ ImpureAp(_, _, _) =>
          interpretLastEff(ap.toMonadic)
      }

    def interpretLast(last: Last[R]): Last[U] =
      last.value match {
        case None => Last.none[U]
        case Some(l) => Last.eff(interpretLastEff(l.value))
      }

    eff match {
      case Pure(a, last) =>
        Eff.pure((a, state)).addLast(interpretLast(last))

      case Impure(NoEffect(a), c, last) =>
        Impure(
          NoEffect(a),
          interpretContinuation(state, c),
          interpretLast(last)
        )

      case Impure(u: Union[_, _], c, last) =>
        m1.project(u) match {
          case Right(tu) =>
            tu match {
              case State.Get() =>
                Eff.impure(state, interpretContinuationWithLast(state, c, last))

              case State.Set(value) =>
                Eff.impure((), interpretContinuationWithLast(value, c, last))
            }

          case Left(other) =>
            Impure(other, interpretContinuation(state, c), interpretLast(last))
        }

      case ap @ ImpureAp(_, _, _) =>
        runState(state)(ap.toMonadic)
    }
  }
}
