package models

import generators.Generators
import models.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class EuTaxRegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "must serialise and deserialise from / to an EU VAT Registration" in {

    val euVatNumberGen = arbitrary[Int].map(_.toString)

    forAll(arbitrary[Country], euVatNumberGen) {
      case (country, vatNumber) =>

        val euVatRegistration = EuVatRegistration(country, vatNumber)

        val json = Json.toJson(euVatRegistration)
        json.validate[EuTaxRegistration] mustEqual JsSuccess(euVatRegistration)
    }
  }

  "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

    forAll(arbitrary[Country], arbitrary[FixedEstablishment], arbitrary[EuTaxIdentifier]) {
      case (country, fixedEstablishment, taxRef) =>

        val euRegistration = RegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

        val json = Json.toJson(euRegistration)
        json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
    }
  }

  "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

    forAll(arbitrary[Country]) {
      country =>
        val euRegistration = RegistrationWithoutFixedEstablishment(country)

        val json = Json.toJson(euRegistration)
        json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
    }
  }
}
