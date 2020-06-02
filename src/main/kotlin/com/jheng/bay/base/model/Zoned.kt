package com.jheng.bay.base.model

import java.time.ZoneId
import java.time.zone.ZoneRulesException

interface Zoned {
    // we may persist this field to db
    val zone_id_string: String?

    val zoneId: ZoneId?
        get() = zone_id_string?.let {
            try {
                ZoneId.of(it)
            } catch (e: ZoneRulesException) {
                null
            }
        }
}
