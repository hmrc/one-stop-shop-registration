/*
 * Copyright 2023 HM Revenue & Customs
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

package crypto

import models.domain.{EncryptedVatCustomerInfo, VatCustomerInfo}
import models.{DesAddress, EncryptedDesAddress, EncryptedSavedUserAnswers, SavedUserAnswers}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject


class SavedUserAnswersEncryptor @Inject()(
                                           crypto: SecureGCMCipher
                                         ) {

  def encryptData(data: JsValue, vrn: Vrn, key: String): EncryptedValue = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)

    e(data.toString)
  }

  def decryptData(data: EncryptedValue, vrn: Vrn, key: String): JsValue = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    Json.parse(d(data))

  }

  def encryptAnswers(answers: SavedUserAnswers, vrn: Vrn, key: String): EncryptedSavedUserAnswers = {
    EncryptedSavedUserAnswers(
      vrn = vrn,
      data = encryptData(answers.data, vrn, key),
      vatInfo = answers.vatInfo.map {
        info =>
          EncryptedVatCustomerInfo(
            encryptDesAddress(info.address, vrn, key),
            info.registrationDate,
            info.partOfVatGroup.map(partOf => crypto.encrypt(partOf.toString, vrn.vrn, key)),
            info.organisationName.map(name => crypto.encrypt(name, vrn.vrn, key)),
            info.singleMarketIndicator
          )
      },
      lastUpdated = answers.lastUpdated
    )
  }

  def decryptAnswers(encryptedAnswers: EncryptedSavedUserAnswers, vrn: Vrn, key: String): SavedUserAnswers = {
    SavedUserAnswers(
      vrn = vrn,
      data = decryptData(encryptedAnswers.data, vrn, key),
      vatInfo =
        encryptedAnswers.vatInfo.map{
          info =>
            VatCustomerInfo(
              decryptDesAddress(info.address, vrn, key),
              info.registrationDate,
              info.partOfVatGroup.map(field => crypto.decrypt(field, vrn.vrn, key).toBoolean),
              info.organisationName.map(name => crypto.decrypt(name, vrn.vrn, key)),
              info.singleMarketIndicator
            )
        },

      lastUpdated = encryptedAnswers.lastUpdated
    )
  }

  private def encryptDesAddress(address: DesAddress, vrn: Vrn, key: String): EncryptedDesAddress = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import address._

    EncryptedDesAddress(e(line1), line2 map e, line3 map e, line4 map e, line5 map e, postCode map e, e(countryCode))
  }

  private def decryptDesAddress(address: EncryptedDesAddress, vrn: Vrn, key: String): DesAddress = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import address._

    DesAddress(d(line1), line2 map d, line3 map d, line4 map d, line5 map d, postCode map d, d(countryCode))
  }
}

