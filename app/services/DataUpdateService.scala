/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.ValidatedNec
import config.AppConfig
import logging.Logging
import models.Registration
import repositories.{RegistrationBackUpRepository, RegistrationRepository}
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait DataUpdateService

class DataUpdateServiceImpl @Inject()(
                                       registrationRepository: RegistrationRepository,
                                       registrationBackUpRepository: RegistrationBackUpRepository,
                                       appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) extends DataUpdateService with Logging {

  type ValidationResult[A] = ValidatedNec[ValidationError, A]

  val runUpdateDateOfFirstSale: Future[Seq[Boolean]] = updateDateOfFirstSale()

  def updateDateOfFirstSale(): Future[Seq[Boolean]] = {

    // Call Get method on RegistrationRepo
//    val registrations = registrationRepository.get(appConfig.dbRecordLimit)

    // Call InsertMany method on RegistrationBackUp
//    registrationBackUpRepository.insertMany(registrations)

    registrationRepository.get(appConfig.dbRecordLimit).flatMap {
      registrations =>
        logger.info(s"${registrations.size} registrations pulled from db")

        val registrationsWithoutDateOfFirstSale: Seq[Registration] = registrations.filter(_.dateOfFirstSale.isEmpty)

        logger.info(s"${registrationsWithoutDateOfFirstSale.size} registrations without dateOfFirstSale")

        Future.sequence(registrationsWithoutDateOfFirstSale.map {
          registration =>
            registrationRepository.updateDateOfFirstSale(registration).map {
              case true =>
                logger.info(s"Successfully updated dateOfFirstSale for VRN: ${obfuscateVrn(registration.vrn)}")
                true
              case false =>
                logger.info(s"Failed to update dateOfFirstSale for VRN: ${obfuscateVrn(registration.vrn)}")
                false
            }
        })
    }
  }

  private def obfuscateVrn(vrn: Vrn): String = vrn.vrn.take(5) + "****"
}
