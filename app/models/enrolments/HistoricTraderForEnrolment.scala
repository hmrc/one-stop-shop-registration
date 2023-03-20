package models.enrolments

import play.api.libs.json.{Json, OFormat}

case class HistoricTraderForEnrolment(vrn: String, groupId: String, userId: String)

object HistoricTraderForEnrolment {

  implicit val format: OFormat[HistoricTraderForEnrolment] = Json.format[HistoricTraderForEnrolment]

}