/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import config.AppConfig
import uk.gov.hmrc.crypto.{PlainText, Scrambled, Sha512Crypto}

import javax.inject.{Inject, Singleton}

@Singleton
class HashingUtil @Inject()(appConfig: AppConfig) {

  lazy val crypto = new Sha512Crypto(appConfig.exclusionsHashingKey)

  def hashValue(value: String): String = {
    crypto.hash(PlainText(value)).value
  }

  def verifyValue(value: String, hashedValue: String): Boolean = {
    crypto.verify(PlainText(value), Scrambled(hashedValue))
  }

}
