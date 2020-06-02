package com.jheng.bay.base.model

import java.time.Instant

interface SoftDeletable {
    var delete_time: Instant?
}