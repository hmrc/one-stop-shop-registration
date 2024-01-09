/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import config.AppConfig
import connectors.EnrolmentsConnector
import logging.Logging
import models.enrolments.HistoricTraderForEnrolment
import play.api.http.Status.CREATED
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.{Clock, ZoneId}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait HistoricalRegistrationEnrolmentService

class HistoricalRegistrationEnrolmentServiceImpl @Inject()(
                                                            appConfig: AppConfig,
                                                            enrolmentsConnector: EnrolmentsConnector,
                                                            registrationService: RegistrationService,
                                                            clock: Clock
                                                          )
                                                          (implicit ec: ExecutionContext) extends HistoricalRegistrationEnrolmentService with Logging {

  val startTransfer: Future[Any] = sendEnrolmentForUsers()

  def sendEnrolmentForUsers(): Future[Boolean] = {
    if (appConfig.historicTradersForEnrolmentEnabled) {
      logger.info("Starting historic trader enrolment")
      val tradersToSubmit = appConfig.historicTradersForEnrolment
      logger.info(s"There are ${tradersToSubmit.size} historic traders to submit for enrolment")
      submitSequentially(tradersToSubmit).map {
        case Right(_) => true
        case Left(e) => throw new Exception(e.body)
      }
    } else {
      logger.info("Skipping historic trader enrolment as disabled")
      Future.successful(true)
    }
  }

  private def submitSequentially(remainingTraders: Seq[HistoricTraderForEnrolment]): Future[Either[HttpResponse, Unit]] = {
    remainingTraders match {
      case Nil => Future.successful(Right())
      case traderToEnrol +: Nil =>
        getRegAndTriggerEs8(traderToEnrol, Seq.empty)
      case traderToEnrol +: otherTraders =>
        getRegAndTriggerEs8(traderToEnrol, otherTraders)
    }
  }

  private def getRegAndTriggerEs8(traderToEnrol: HistoricTraderForEnrolment, otherTraders: Seq[HistoricTraderForEnrolment]) = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    registrationService.getWithoutAudit(traderToEnrol.vrn).flatMap {
      case Some(registration) =>
        enrolmentsConnector.es8(traderToEnrol.groupId, traderToEnrol.vrn, traderToEnrol.userId, registration.submissionReceived.getOrElse{
          val exception = new IllegalStateException(s"Registration for user ${traderToEnrol.vrn} did not have a submission received date")
          logger.error(exception.getMessage, exception)
          throw exception
        }.atZone(ZoneId.systemDefault).toLocalDate).flatMap { a =>
          a.status match {
            case CREATED =>
              logger.info(s"Successfully created enrolment for ${traderToEnrol.vrn}")
              if (otherTraders.nonEmpty) {
                submitSequentially(otherTraders)
              } else {
                logger.info("Completed submitting enrolment for existing users")
                Future.successful(Right(()))
              }
            case status =>
              logger.error(s"Received unexpected response for ${traderToEnrol}: $status ${a.body}")
              submitSequentially(otherTraders)
          }
        }
      case _ =>
        logger.error(s"Unable to find registration for ${traderToEnrol.vrn}")
        Future.failed(new Exception("User didn't exist in registration"))
    }
  }
}
