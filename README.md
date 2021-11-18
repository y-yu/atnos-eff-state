State without mutable using atnos-eff
=====================================
[![CI](https://github.com/y-yu/atnos-eff-state/actions/workflows/ci.yml/badge.svg)](https://github.com/y-yu/atnos-eff-state/actions/workflows/ci.yml)

PoC of `State` without mutable(e.g. `var` variables) using [atnos-eff](https://github.com/atnos-org/eff). Its monadic and applicative lows are tested by [scalaprops](https://github.com/scalaprops/scalaprops).

## Example

See also: [`Main.scala`](https://github.com/y-yu/atnos-eff-state/blob/master/src/main/scala/Main.scala).

```scala
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
```

Output:
```
s1: 5
s2: 1
result(s1 + num): Right((10,1))
```