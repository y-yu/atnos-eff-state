import org.atnos.eff.Eff

import org.atnos.eff.EitherEffect
import org.atnos.eff.Fx
import state.State
import state.StateEffect.*

object Main {
  def main(args: Array[String]): Unit = {
    type R = Fx.fx2[State[Int, *], Either[Throwable, *]]

    val eff: Eff[R, Int] = for {
      s1 <- get[Int, R]
      _ = println(s"s1: $s1")
      num <- EitherEffect.fromEither[R, Throwable, Int](
        Right(5)
      )
      _ <- set[Int, R](1)
      s2 <- get[Int, R]
      _ = println(s"s2: $s2")
    } yield s1 + num

    val result = Eff.run(
      EitherEffect.runEither(
        eff.runState(5)
      )
    )
    println(s"result(s1 + num): $result")
  }
}
