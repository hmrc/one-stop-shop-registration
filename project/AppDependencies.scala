import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.2.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.50.0",
    "uk.gov.hmrc"             %% "domain"                     % "5.11.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.2.0"             % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.50.0"            % "test, it",
    "org.scalatest"           %% "scalatest"                  % "3.2.5"             % "test, it",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % "test, it",
    "org.scalatestplus"       %% "mockito-3-4"                % "3.2.7.0"           % "test, it",
    "org.mockito"             %% "mockito-scala"              % "1.16.0"            % "test, it",
    "uk.gov.hmrc"             %% "hmrctest"                   % "3.9.0-play-26"     % "test, it"

  )
}
