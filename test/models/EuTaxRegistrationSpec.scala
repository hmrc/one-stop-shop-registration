package models

import crypto.EncryptedValue
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class EuTaxRegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "EU Tax Registration" - {

    "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

      forAll(arbitrary[Country], arbitrary[TradeDetails], arbitrary[EuTaxIdentifier]) {
        case (country, fixedEstablishment, taxRef) =>

          val euRegistration = RegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

          val json = Json.toJson(euRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }

    "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

      forAll(arbitrary[Country]) {
        country =>
          val euRegistration = RegistrationWithoutTaxId(country)

          val json = Json.toJson(euRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }
  }

  "Encrypted EU Tax Registration" - {

    "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

      val gen = for {
        country        <- arbitrary[EncryptedCountry]
        name           <- arbitrary[EncryptedValue]
        line1          <- arbitrary[EncryptedValue]
        town           <- arbitrary[EncryptedValue]
        identifierType <- arbitrary[EncryptedValue]
        taxRef         <- arbitrary[EncryptedValue]
        addressCountry <- arbitrary[Country]
      } yield (
        country,
        EncryptedTradeDetails(name, EncryptedInternationalAddress(line1, None, town, None, None, addressCountry)),
        EncryptedEuTaxIdentifier(identifierType, taxRef)
      )

      forAll(gen) {
        case (country, fixedEstablishment, taxRef) =>

          val euRegistration = EncryptedRegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

          val json = Json.toJson(euRegistration)
          json.validate[EncryptedEuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }

    "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

      forAll(arbitrary[EncryptedCountry]) {
        country =>
          val euRegistration = EncryptedRegistrationWithoutTaxId(country)

          val json = Json.toJson(euRegistration)
          json.validate[EncryptedEuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }
  }
}