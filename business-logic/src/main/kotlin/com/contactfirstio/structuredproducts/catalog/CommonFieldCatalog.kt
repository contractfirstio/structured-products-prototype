package com.contactfirstio.structuredproducts.catalog

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldRequirement

object CommonFieldCatalog {

  const val CATEGORY_COUNTERPARTY_AND_TENOR = "Counterparty & Tenor"
  const val CATEGORY_MONETARY_AND_SIZE = "Monetary & Size"
  const val CATEGORY_COUPON_STRUCTURE = "Coupon Structure"
  const val CATEGORY_BARRIERS_AND_LEVELS = "Barriers & Levels"
  const val CATEGORY_MARKET_AND_TECHNICAL = "Market & Technical"

  val fields: List<CatalogFieldDefinition> =
      listOf(
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "counterparty_by_rating",
                  displayName = "CounterParty (By Rating)",
                  category = CATEGORY_COUNTERPARTY_AND_TENOR,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL, enumOptions = listOf("AAA", "AA", "A", "BBB", "BB", "B", "Unrated"),
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "settlement_period",
                  displayName = "Settlement Period",
                  category = CATEGORY_COUNTERPARTY_AND_TENOR,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL, enumOptions = listOf("T+0", "T+1", "T+2"),
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "tenor",
                  displayName = "Tenor",
                  category = CATEGORY_COUNTERPARTY_AND_TENOR,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL, enumOptions = listOf("1 Week", "1 Month", "3 Months", "6 Months", "1 Year"),
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "issuer",
                  displayName = "Issuer",
                  category = CATEGORY_COUNTERPARTY_AND_TENOR,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "counterparty",
                  displayName = "Counterparty",
                  category = CATEGORY_COUNTERPARTY_AND_TENOR,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "currency",
                  displayName = "Currency",
                  category = CATEGORY_MONETARY_AND_SIZE,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL, enumOptions = listOf("USD", "HKD", "GBP", "EUR", "JPY"),
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "size",
                  displayName = "Size",
                  category = CATEGORY_MONETARY_AND_SIZE,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "ib_price_pct",
                  displayName = "IB Price(%)",
                  category = CATEGORY_MONETARY_AND_SIZE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "fixed_coupon_pa_pct",
                  displayName = "Fixed Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "bonus_coupon_pa_pct",
                  displayName = "Bonus Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "variable_coupon_pa_pct",
                  displayName = "Variable Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "min_coupon_pa_pct",
                  displayName = "Min. Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "max_coupon_pa_pct",
                  displayName = "Max. Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "head_start_coupon_pa_pct",
                  displayName = "Head-Start Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "target_coupon_pa_pct",
                  displayName = "Target Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "no_of_fixed_coupon_period",
                  displayName = "No. of Fixed Coupon Period",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "cpn_pa_pct",
                  displayName = "Cpn p.a. (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "step_up_coupon_pa_pct",
                  displayName = "Step-up Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "lock_in_coupon_pa_pct",
                  displayName = "Lock-in Coupon (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "floating_rate_cpn_pa_ref_index_plus_pct",
                  displayName = "Floating rate Cpn p.a. (Ref. Index + %)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "fixed_rate_cpn_pa_pct",
                  displayName = "Fixed rate Cpn (p.a.) (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "cms_rate_pct",
                  displayName = "CMS rate (%)",
                  category = CATEGORY_COUPON_STRUCTURE,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "put_strike_pct",
                  displayName = "Put Strike (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "callable_level_pct",
                  displayName = "Callable Level (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "callable_after",
                  displayName = "Callable after",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "putable_after",
                  displayName = "Putable after",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "stepdown_by_pct",
                  displayName = "Stepdown by (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "knock_out_barrier_continuous_monitoring_pct",
                  displayName = "Knock-Out Barrier (Continuous monitoring %)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "knock_out_barrier_daily_close_monitoring_pct",
                  displayName = "Knock-Out Barrier (Daily close monitoring %)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "knock_out_barrier_maturity_monitoring_pct",
                  displayName = "Knock-Out Barrier (Maturity monitoring %)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "capital_return_level_pct",
                  displayName = "Capital Return Level (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "coupon_barrier",
                  displayName = "Coupon Barrier",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "lower_barrier_pct",
                  displayName = "Lower Barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "upper_barrier_pct",
                  displayName = "Upper Barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "outer_barrier_pct",
                  displayName = "Outer Barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "kick_in_barrier_continuous_monitoring_pct",
                  displayName = "Kick-In Barrier (Continuous monitoring %)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "kick_in_barrier_daily_close_monitoring_pct",
                  displayName = "Kick-In Barrier (Daily close monitoring %)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "kick_in_barrier_maturity_monitoring_pct",
                  displayName = "Kick-In Barrier (Maturity monitoring %)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "lock_in_barrier_pct",
                  displayName = "Lock-in Barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "callable_barrier_pct",
                  displayName = "Callable Barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "outperformance_barrier_pct",
                  displayName = "Outperformance barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "underperformance_barrier_pct",
                  displayName = "Underperformance barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "inner_barrier_pct",
                  displayName = "Inner Barrier (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "floor_pct",
                  displayName = "Floor (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "call_strike_pct",
                  displayName = "Call Strike (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "upper_strike_pct",
                  displayName = "Upper Strike (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "lower_strike_pct",
                  displayName = "Lower Strike (%)",
                  category = CATEGORY_BARRIERS_AND_LEVELS,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "underlyings",
                  displayName = "Underlyings",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "underlying",
                  displayName = "Underlying",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "bullish_underlying_basket",
                  displayName = "Bullish Underlying basket",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "bearish_underlying_basket",
                  displayName = "Bearish Underlying basket",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "called_date",
                  displayName = "Called Date",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "cap_pct",
                  displayName = "Cap (%)",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "upside_participation_pct",
                  displayName = "Upside Participation (%)",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "downside_participation_pct",
                  displayName = "Downside Participation (%)",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.DOUBLE,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "observation_frequency",
                  displayName = "Observation Frequency",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL, enumOptions = listOf("Monthly", "Quarterly", "Semi-Annually", "Annually"),
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "no_of_callable_periods",
                  displayName = "No. of Callable periods",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "ref_index",
                  displayName = "Ref. Index",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "gearing_ratio",
                  displayName = "Gearing Ratio",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "no_of_guarantee_periods",
                  displayName = "No. of Guarantee Periods",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "settlement_frequency",
                  displayName = "Settlement Frequency",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL, enumOptions = listOf("Monthly", "Quarterly", "Semi-Annually", "Annually"),
              ),
          ),
          CommonFieldGroup.fields(
              CommonFieldGroupSpec(
                  baseKey = "badger",
                  displayName = "Badger",
                  category = CATEGORY_MARKET_AND_TECHNICAL,
                  dataType = FieldDataType.STRING,
                  requirement = FieldRequirement.OPTIONAL,
              ),
          ),
      ).flatten()

  val categories: List<String> =
      listOf(
          CATEGORY_COUNTERPARTY_AND_TENOR,
          CATEGORY_MONETARY_AND_SIZE,
          CATEGORY_COUPON_STRUCTURE,
          CATEGORY_BARRIERS_AND_LEVELS,
          CATEGORY_MARKET_AND_TECHNICAL,
      )
}
