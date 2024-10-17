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

package crypto

import config.AppConfig
import models.{EncryptedSavedUserAnswers, LegacyEncryptedSavedUserAnswers, NewEncryptedSavedUserAnswers, SavedUserAnswers}
import play.api.libs.json.Json
import services.crypto.EncryptionService
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject


class SavedUserAnswersEncryptor @Inject()(
                                           appConfig: AppConfig,
                                           encryptionService: EncryptionService,
                                           secureGCMCipher: AesGCMCrypto
                                         ) {

  protected val key: String = appConfig.encryptionKey

  def encryptAnswers(answers: SavedUserAnswers, vrn: Vrn): EncryptedSavedUserAnswers = {
    def encryptAnswerValue(answerValue: String): String = encryptionService.encryptField(answerValue)

    NewEncryptedSavedUserAnswers(
      vrn = vrn,
      data = encryptAnswerValue(answers.data.toString),
      lastUpdated = answers.lastUpdated
    )
  }

  def decryptAnswers(encryptedAnswers: NewEncryptedSavedUserAnswers, vrn: Vrn): SavedUserAnswers = {
    def decryptValue(encryptedValue: String): String = encryptionService.decryptField(encryptedValue)
    SavedUserAnswers(
      vrn = vrn,
      data = Json.parse(decryptValue(encryptedAnswers.data)),
      lastUpdated = encryptedAnswers.lastUpdated
    )
  }

  def decryptLegacyAnswers(encryptedAnswers: LegacyEncryptedSavedUserAnswers, vrn: Vrn): SavedUserAnswers = {
    def decryptValue(encryptedValue: EncryptedValue): String = secureGCMCipher.decrypt(encryptedValue, vrn.vrn, key)

    SavedUserAnswers(
      vrn = vrn,
      data = Json.parse(decryptValue(encryptedAnswers.data)),
      lastUpdated = encryptedAnswers.lastUpdated
    )
  }

}

