/**
 * NOTE: This class is auto generated by the akka-scala (beta) swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen
 * For any issue or feedback, please open a ticket via https://github.com/swagger-api/swagger-codegen/issues/new
 */

package org.wso2.carbon.apimgt.rest.api.store.models

import org.joda.time.DateTime


case class Tier (
  name: String,
  description: Option[String],
  tierLevel: Option[TierEnums.TierLevel],
  /* Custom attributes added to the tier policy  */
  attributes: Option[Map[String, String]],
  /* Maximum number of requests which can be sent within a provided unit time  */
  requestCount: Long,
  unitTime: Long,
  /* This attribute declares whether this tier is available under commercial or free  */
  tierPlan: TierEnums.TierPlan,
  /* If this attribute is set to false, you are capabale of sending requests even if the request count exceeded within a unit time  */
  stopOnQuotaReach: Boolean)

object TierEnums {

  type TierLevel = TierLevel.Value
  type TierPlan = TierPlan.Value
  
  object TierLevel extends Enumeration {
    val Api = Value("api")
    val Application = Value("application")
  }

  object TierPlan extends Enumeration {
    val FREE = Value("FREE")
    val COMMERCIAL = Value("COMMERCIAL")
  }

  
}
