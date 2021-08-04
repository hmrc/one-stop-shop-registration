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

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import cats.implicits._
import config.AppConfig
import logging.Logging
import models._
import repositories.RegistrationRepository
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject
import scala.concurrent.ExecutionContext

trait DataQueryService
class DataQueryServiceImpl @Inject()(
                                  repository: RegistrationRepository,
                                  appConfig: AppConfig
                                )(implicit ec: ExecutionContext) extends DataQueryService with Logging {

  type ValidationResult[A] = ValidatedNec[ValidationError, A]

  val checkFieldLengths: Unit = {

    val count = appConfig.dbRecordLimit

    logger.info(s"About to check up to $count records for field length issues")

    repository.get(count).map {
      registrations =>

        logger.info(s"Checking ${registrations.size} registrations for field length issues")

        registrations.foreach {
          registration =>
            checkRegistration(registration) match {
              case Valid(_) =>
                logger.info(s"VRN ${obfuscateVrn(registration.vrn)} has no field length issues")
              case Invalid(e) =>
                val errorMessages = e.toChain.toList.map(_.errorMessage).mkString("\n")
                logger.warn(s"VRN ${obfuscateVrn(registration.vrn)} has the following field length issues:\n$errorMessages")
            }
        }
    }.recover {
      case e: Exception =>
        logger.warn("Error trying to get registrations", e)
        ()
    }
  }

  private def obfuscateVrn(vrn: Vrn): String = vrn.vrn.take(5) + "****"

  private def checkRegistration(registration: Registration): ValidationResult[Unit] = {

    logger.info(s"Checking data for VRN ${obfuscateVrn(registration.vrn)}")

    (
      checkContactName(registration.contactDetails),
      checkAddress(registration.vatDetails.address),
      checkEuRegistrations(registration.euRegistrations.toList)
    ).mapN(
      (_, _, _) => ().validNec
    )
  }

  private def checkEuRegistrations(registrations: List[EuTaxRegistration]): ValidationResult[List[Unit]] =
    registrations
      .map(checkEuRegistration)
      .sequence

  private def checkEuRegistration(registration: EuTaxRegistration): ValidationResult[Unit] =
    registration match {
      case v: EuVatRegistration =>
        checkLength("EuVatRegistration.vatNumber", v.vatNumber, 50)

      case fe: RegistrationWithFixedEstablishment =>
        (
          checkLength("RegistrationWithFixedAddress.taxIdentifier", fe.taxIdentifier.value, 20),
          checkFixedEstablishment(fe.fixedEstablishment)
        ).mapN(
          (_, _) => ().validNec
        )

      case _: RegistrationWithoutFixedEstablishment =>
        ().validNec
    }

  private def checkFixedEstablishment(fe: FixedEstablishment): ValidationResult[Unit] =
    (
      checkLength("FixedEstablishment.tradingName", fe.tradingName, 100),
      checkInternationalAddress(fe.address)
    ).mapN(
      (_, _) => ().validNec
    )

  private def checkContactName(contactDetails: ContactDetails): ValidationResult[Unit] =
    checkLength("ContactDetails.fullName", contactDetails.fullName, 100)

  private def checkAddress(address: Address): ValidationResult[Unit] =
    address match {
      case uk: UkAddress           => checkUkAddress(uk)
      case i: InternationalAddress => checkInternationalAddress(i)
      case _                       => ().validNec
    }

  private def checkInternationalAddress(address: InternationalAddress): ValidationResult[Unit] =
    (
      checkLength("InternationalAddress.line1", address.line1, 35),
      address.line2.map(checkLength("InternationalAddress.line2", _, 35)).getOrElse(().validNec),
      checkLength("InternationalAddress.townOrCity", address.townOrCity, 35),
      address.stateOrRegion.map(checkLength("InternationalAddress.stateOrRegion", _, 35)).getOrElse(().validNec),
      address.postCode.map(checkLength("InternationalAddress.postCode", _, 50)).getOrElse(().validNec)
    ).mapN((_, _, _, _, _) => ())


  private def checkUkAddress(address: UkAddress): ValidationResult[Unit] =
    (
      checkLength("UkAddress.line1", address.line1, 35),
      address.line2.map(checkLength("UkAddress.line2", _, 35)).getOrElse(().validNec),
      checkLength("UkAddress.townOrCity", address.townOrCity, 35),
      address.county.map(checkLength("UkAddress.county", _, 35)).getOrElse(().validNec),
      checkLength("address.postCode", address.postCode, 10)
    ).mapN((_, _, _, _, _) => ())

  private def checkLength(key: String, value: String, limit: Int): ValidationResult[Unit] =
    if (value.length <= limit) {
      ().validNec
    } else {
      FieldLengthError(key, value.length, limit).invalidNec
    }
}
