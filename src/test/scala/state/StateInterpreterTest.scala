package state

import scalaprops.*
import org.atnos.eff.Eff
import org.atnos.eff.Fx
import scalaprops.Properties
import scalaprops.Scalaprops
import scalaz.Equal
import scalaz.Monad
import scalaz.Scalaz.intInstance
import scalaz.std.anyVal.*
import state.StateEffect.*

object StateInterpreterTest extends Scalaprops {
  type R = Fx.fx1[State[Int, *]]

  implicit def monadTask[A]: Monad[Eff[R, *]] =
    new Monad[Eff[R, *]] {
      def point[B](a: => B): Eff[R, B] = Eff.pure(a)
      def bind[B, C](a: Eff[R, B])(f: B => Eff[R, C]): Eff[R, C] = a.flatMap(f)
    }

  implicit def genState[A](implicit
    g1: Gen[A],
    g2: Gen[Int]
  ): Gen[Eff[R, A]] =
    Gen.frequency(
      1 -> g1.map(x => Eff.pure[R, A](x)),
      1 -> g1.map { x =>
        StateEffect
          .set[Int, R](g2.sample())
          .map(_ => x)
      },
      1 -> g1.map { x =>
        StateEffect
          .get[Int, R]
          .map(_ => x)
      }
    )

  implicit def equalState[A](implicit
    equal: Equal[A],
    gen: Gen[Int]
  ): Equal[Eff[R, A]] =
    equal.contramap { (eff: Eff[R, A]) =>
      Eff.run(eff.runState(gen.sample()))._1
    }

  val intStateMonadLawsTest = Properties.list(
    scalazlaws.monad.all[Eff[R, *]]
  )
}
