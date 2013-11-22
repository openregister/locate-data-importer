package uk.gov.gds.model

import org.specs2.mutable.Specification
import uk.gov.gds.model.CodeLists._
import org.joda.time.DateTime
import scala.Some

class AddressBuilderTests extends Specification {

  "The address builder" should {
    "include the postcode from the BLPU" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.postcode must beEqualTo("postcode")
    }

    "include not create an address if no LPI" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List.empty[LPI], List(classification), List(organisation))) must beEqualTo(None)
    }

    "include not create an address if no eligible BLPU (logical status not approved)" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blpuProvisonal, List(lpi), List(classification), List(organisation))) must beEqualTo(None)
    }

    "include not create an address if no eligible LPI (end date present)" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpiWithEndDate), List(classification), List(organisation))) must beEqualTo(None)
    }

    "include not create an address if no eligible LPI (logical status not approved)" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpiProvisional), List(classification), List(organisation))) must beEqualTo(None)
    }

    "should concatenate all the SAO and PAO fields into one line each " in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.postcode must beEqualTo("postcode")
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.line1 must beEqualTo("sao text sao start number sao start suffix sao end number sao end suffix")
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.line2 must beEqualTo("pao text pao start number pao start suffix pao end number pao end suffix")
    }

    "if there is more than one LPI for a BLPU use the one that has no end date" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi, lpiWithEndDate), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.postcode must beEqualTo("postcode")
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi, lpiWithEndDate), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.line1 must beEqualTo("sao text sao start number sao start suffix sao end number sao end suffix")
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi, lpiWithEndDate), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.line2 must beEqualTo("pao text pao start number pao start suffix pao end number pao end suffix")
    }

    "if there is more than one LPI for a BLPU and both have no end date use the approved one" in {
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi, lpiProvisional), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.postcode must beEqualTo("postcode")
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi, lpiProvisional), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.line1 must beEqualTo("sao text sao start number sao start suffix sao end number sao end suffix")
      AddressBuilder.geographicAddressToSimpleAddress(AddressBaseWrapper(blup, List(lpi, lpiProvisional), List(classification), List(organisation)), streetMap, streetDescriptorMap).get.line2 must beEqualTo("pao text pao start number pao start suffix pao end number pao end suffix")
    }
  }


  private lazy val startDate = new DateTime().minusDays(100)
  private lazy val endDate = new DateTime().minusDays(1)
  private lazy val lastUpdatedDate = new DateTime().minusDays(50)

  private lazy val blup = BLPU(
    "uprn",
    Some(BlpuStateCode.inUse),
    Some(LogicalStatusCode.approved),
    1.1,
    2.2,
    1234,
    startDate,
    None,
    lastUpdatedDate,
    "postcode")

  private lazy val blpuProvisonal = BLPU(
    "uprn",
    Some(BlpuStateCode.inUse),
    Some(LogicalStatusCode.provisional),
    1.1,
    2.2,
    1234,
    startDate,
    None,
    lastUpdatedDate,
    "postcode")

  private lazy val lpi = LPI(
    "uprn",
    "usrn",
    Some(LogicalStatusCode.approved),
    startDate,
    None,
    lastUpdatedDate,
    Some("pao start number"),
    Some("pao start suffix"),
    Some("pao end number"),
    Some("pao end suffix"),
    Some("pao text"),
    Some("sao start number"),
    Some("sao start suffix"),
    Some("sao end number"),
    Some("sao end suffix"),
    Some("sao text"),
    Some("area name"),
    Some(true)
  )

  private lazy val lpiProvisional = LPI(
    "uprn",
    "usrn",
    Some(LogicalStatusCode.provisional),
    startDate,
    None,
    lastUpdatedDate,
    Some("pao start number"),
    Some("pao start suffix"),
    Some("pao end number"),
    Some("pao end suffix"),
    Some("pao text"),
    Some("sao start number"),
    Some("sao start suffix"),
    Some("sao end number"),
    Some("sao end suffix"),
    Some("sao text"),
    Some("area name"),
    Some(true)
  )

  private lazy val lpiWithEndDate = LPI(
    "uprn",
    "usrn",
    Some(LogicalStatusCode.approved),
    startDate,
    Some(endDate),
    lastUpdatedDate,
    Some("pao start number - end"),
    Some("pao start suffix - end"),
    Some("pao end number - end"),
    Some("pao end suffix - end"),
    Some("pao text - end"),
    Some("sao start number - end"),
    Some("sao start suffix - end"),
    Some("sao end number - end"),
    Some("sao end suffix - end"),
    Some("sao text - end"),
    Some("area name - end"),
    Some(true)
  )

  private lazy val classification = Classification("uprn", "code", startDate, None, lastUpdatedDate)
  private lazy val organisation = Organisation("uprn", "organisation", startDate, None, lastUpdatedDate)
  private lazy val street = Street("usrn", Some(StreetRecordTypeCode.numberedStreet), Some(StreetStateCode.open), Some(StreetSurfaceCode.mixed), Some(StreetClassificationCode.footpath), startDate, None, endDate)
  private lazy val streetDescriptor = StreetDescriptor("usrn", "street name", "locality", "town", "area")
  private lazy val streetMap = Map("usrn" -> List(street))
  private lazy val streetDescriptorMap = Map("usrn" -> streetDescriptor)
}
