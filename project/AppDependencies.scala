import play.core.PlayVersion
import sbt.*

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % "8.4.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % "1.7.0",
    "uk.gov.hmrc"             %% "domain-play-30"             % "9.0.0",
    "org.typelevel"           %% "cats-core"                  % "2.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % "8.4.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % "1.7.0",
    "org.scalatest"           %% "scalatest"                  % "3.2.15",
    "org.playframework"       %% "play-test"                  % PlayVersion.current,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.6",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.scalatestplus"       %% "mockito-4-6"                % "3.2.15.0",
    "org.mockito"             %% "mockito-scala"              % "1.17.30",
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.17.0"
  ).map(_ % "test, it")
}
