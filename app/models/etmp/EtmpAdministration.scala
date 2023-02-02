package models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpAdministration(messageType: String = "OSSSubscriptionCreate", regimeID: String = "OSS")

object EtmpAdministration {
  implicit val format: OFormat[EtmpAdministration] = Json.format[EtmpAdministration]
}