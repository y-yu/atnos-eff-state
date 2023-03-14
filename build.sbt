import sbt._

import Keys._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._

val scala213 = "2.13.7"
val scala3 = "3.0.2"

val projectName = "atnos-eff-state"

lazy val root =
  project
    .in(file("."))
    .settings(
      scalaVersion := scala213,
      crossScalaVersions := Seq(scala213, scala3),
      scalacOptions ++= {
        if (isScala3.value) {
          Seq(
            "-Ykind-projector",
            "-source",
            "3.0-migration"
          )
        } else {
          Seq(
            "-Xlint:infer-any",
            "-Xsource:3"
          )
        }
      },
      scalacOptions ++= Seq(
        "-feature",
        "-language:existentials",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-unchecked",
        "-encoding",
        "UTF-8",
        "-deprecation"
      ),
      scalafmtOnCompile := true,
      organization := "com.github.y-yu",
      name := projectName,
      description := "State implementation using atnos-eff",
      homepage := Some(url("https://github.com/y-yu")),
      licenses := Seq("MIT" -> url(s"https://github.com/y-yu/$projectName/blob/master/LICENSE")),
      addCommandAlias("SetScala3", s"++ ${scala3}!"),
      libraryDependencies ++= Seq(
        "org.atnos" %% "eff" % "5.23.0",
        "com.lihaoyi" %% "pprint" % "0.6.6",
        "org.typelevel" %% "cats-laws" % "2.6.1" % Test,
        "org.scalacheck" %% "scalacheck" % "1.14.1" % Test
      ),
      libraryDependencies ++= {
        if (isScala3.value)
          Nil
        else
          Seq(compilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full))
      }
    )

def isScala3 = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
)
