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
import models.*
import play.api.libs.json.{Json, JsSuccess}
import services.crypto.EncryptionService
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject

class CachedRegistrationEncryptor @Inject()(
                                           appConfig: AppConfig,
                                           encryptionService: EncryptionService
                                         ) {

  protected val key: String = appConfig.encryptionKey

  def encryptRegistration(cachedRegistrationWrapper: CachedRegistrationWrapper, vrn: Vrn): EncryptedCachedRegistrationWrapper = {
    def encryptValue(value: String): String = encryptionService.encryptField(value)

    EncryptedCachedRegistrationWrapper(
      userId = cachedRegistrationWrapper.userId,
      vrn = vrn,
      data = cachedRegistrationWrapper.registration.map(reg => encryptValue(Json.toJson(reg).toString)),
      lastUpdated = cachedRegistrationWrapper.lastUpdated
    )
  }

  def decryptRegistration(encryptedCachedRegistration: EncryptedCachedRegistrationWrapper): CachedRegistrationWrapper = {
    def decryptValue(encryptedValue: String): String = encryptionService.decryptField(encryptedValue)
    CachedRegistrationWrapper(
      userId = encryptedCachedRegistration.userId,
      registration = encryptedCachedRegistration.data.map(encryptedValue => Json.parse(decryptValue(encryptedValue)).validate[Registration] match {
        case JsSuccess(value, _) => value
        case _ => throw new IllegalStateException("Unable to parse JSON of encrypted cached registration")
      }),
      lastUpdated = encryptedCachedRegistration.lastUpdated
    )
  }

}

