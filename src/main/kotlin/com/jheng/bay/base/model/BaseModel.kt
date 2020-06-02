package com.jheng.bay.base.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

abstract class BaseModel {
    var id: Int = 0

    var create_time: Instant? = null
    var update_time: Instant? = null

    @get: JsonIgnore
    val is_new: Boolean
        get() = id == 0
}
