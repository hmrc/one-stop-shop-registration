import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.20.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.59.0",
    "uk.gov.hmrc"             %% "domain"                     % "7.0.0-play-28",
    "org.typelevel"           %% "cats-core"                  % "2.7.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.20.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.59.0",
    "org.scalatest"           %% "scalatest"                  % "3.2.10",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.35.10", // Required to stay at this version - see https://github.com/scalatest/scalatest/issues/1736
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.scalatestplus"       %% "mockito-3-4"                % "3.2.10.0",
    "org.mockito"             %% "mockito-scala"              % "1.17.0",
    "com.github.tomakehurst"  %  "wiremock-standalone"        % "2.27.2",
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.15.4"
  ).map(_ % "test, it")
}
