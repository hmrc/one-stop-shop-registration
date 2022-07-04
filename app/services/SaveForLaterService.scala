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

package services

import models.SavedUserAnswers
import models.requests.SaveForLaterRequest
import repositories.SaveForLaterRepository
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.Future


class SaveForLaterService @Inject()(
                                     repository: SaveForLaterRepository,
                                     clock: Clock
                                   ) {

  def saveAnswers(request: SaveForLaterRequest): Future[SavedUserAnswers] = {
    val answers = SavedUserAnswers(
      vrn = request.vrn,
      data = request.data,
      vatInfo = request.vatInfo,
      lastUpdated = Instant.now(clock)
    )
    repository.set(answers)
  }

  def get(vrn: Vrn) :  Future[Option[SavedUserAnswers]] =
    repository.get(vrn)

  def delete(vrn: Vrn): Future[Boolean] =
    repository.clear(vrn)

}
