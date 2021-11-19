package state

import cats.Eq
import cats.implicits.*
import cats.laws.discipline.ApplicativeTests
import cats.laws.discipline.FunctorTests
import cats.laws.discipline.MonadTests
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.atnos.eff.Eff
import org.atnos.eff.Fx
import org.scalacheck.Prop
import org.scalacheck.Properties
import state.StateEffect.*

object StateLawsTest extends Properties("StateLaws") {
  type R = Fx.fx1[State[Int, *]]

  val default: Int = Arbitrary.arbitrary[Int].sample.getOrElse(1)

  import Eff.EffMonad

  implicit def genState[A: Arbitrary](implicit
    aInt: Arbitrary[Int]
  ): Arbitrary[Eff[R, A]] =
    Arbitrary(
      Gen.frequency(
        1 -> Arbitrary.arbitrary[A].map(Eff.pure[R, A]),
        1 -> Arbitrary.arbitrary[A].map { x =>
          for {
            s <- StateEffect.get[Int, R]
            newState = s + 1
            _ <- StateEffect.set(newState)
          } yield x
        }
      )
    )

  implicit def equalState[A](implicit
    eq: Eq[(A, Int)]
  ): Eq[Eff[R, A]] = {
    Eq.by { (eff: Eff[R, A]) =>
      Eff
        .run(
          eff.runState(default)
        )
    }
  }

  def checkAll(props: Seq[(String, Prop)]): Unit = {
    for ((name2, prop) <- props) yield {
      property(name + ":" + name2) = prop
    }
  }

  checkAll(MonadTests[Eff[R, *]].monad[Int, Int, Int].props)
  checkAll(ApplicativeTests[Eff[R, *]].applicative[Int, Int, Int].props)
  checkAll(FunctorTests[Eff[R, *]].functor[Int, Int, Int].props)
}
