package utils

import base.BaseSpec
import config.AppConfig
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach

class HashingUtilSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    Mockito.reset(mockConfig)
    super.beforeEach()
  }

  "HashingUtil must" - {

    "hash a value that can be used in config" in {

      when(mockConfig.exclusionsHashingKey) thenReturn "mERA6vGFqQLsa4TuKmnqQTDLBQ43N8Lzbhj5auPJtHGyteuU8KCkYXFZH67sVoPa"

      val hashingUtil = new HashingUtil(mockConfig)

      val originalValue = "123456789"

      val hashedValue = hashingUtil.hashValue(originalValue)

      hashingUtil.verifyValue(originalValue, hashedValue) mustBe true
    }

    // Use this for producing the hashing VRNs

    "hash a lot of values" in {

      when(mockConfig.exclusionsHashingKey) thenReturn "mERA6vGFqQLsa4TuKmnqQTDLBQ43N8Lzbhj5auPJtHGyteuU8KCkYXFZH67sVoPa"

      val hashingUtil = new HashingUtil(mockConfig)

      val valuesToBeHashed = Seq(
        "123456789",
        "100005123",
        "600000011",
        "600000012",
        "600000013",
        "600000014",
        "600000015",
        "100000003",
        "110079461",
        "222221122",
        "888999957",
        "888999947",
        "888999946"
      )

      valuesToBeHashed.zipWithIndex.foreach { case (valueToBeHashed, index) =>
        val hashedValue = hashingUtil.hashValue(valueToBeHashed)
        println(s"Value $valueToBeHashed has index ${index + 1}")
        println(s"""features.exclusions.excluded-traders.${index + 1}.vrn: "$hashedValue"""")
      }

      true mustBe true // this is just here to make this test pass
    }

  }

}
