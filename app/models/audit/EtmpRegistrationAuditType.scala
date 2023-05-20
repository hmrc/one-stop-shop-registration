package models.audit

import models.{Enumerable, WithName}

sealed trait EtmpRegistrationAuditType {
  val auditType: String
  val transactionName: String
}

object EtmpRegistrationAuditType extends Enumerable.Implicits {
  case object CreateRegistration extends WithName("CreateRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "EtmpRegistration"
    override val transactionName: String = "etmp-registration"
  }
  case object AmendRegistration extends WithName("AmendRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "RegistrationAmended"
    override val transactionName: String = "registration-amended"
  }
}