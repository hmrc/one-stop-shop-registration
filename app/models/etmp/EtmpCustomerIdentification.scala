package models.etmp

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

case class EtmpCustomerIdentification(vrn: Vrn)

object EtmpCustomerIdentification {
  implicit val format: OFormat[EtmpCustomerIdentification] = Json.format[EtmpCustomerIdentification]
}