import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.4.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.50.0",
    "uk.gov.hmrc"             %% "domain"                     % "5.11.0-play-27",
    "org.typelevel"           %% "cats-core"                  % "2.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.2.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.50.0",
    "org.scalatest"           %% "scalatest"                  % "3.2.5",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.scalatestplus"       %% "mockito-3-4"                % "3.2.7.0",
    "org.mockito"             %% "mockito-scala"              % "1.16.0",
    "com.github.tomakehurst"  %  "wiremock-standalone"        % "2.25.0",
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.7.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.15.3"
  ).map(_ % "test, it")
}
