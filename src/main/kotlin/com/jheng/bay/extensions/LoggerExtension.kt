package com.jheng.bay.extensions

import org.slf4j.Logger


// do not move this to extension package cuz we may add additional functionality in the future
// reference: https://ruslan.ibragimov.by/2019/08/26/slf4j-fluent-api-kotlin/
internal inline fun Logger.trace(msg: () -> Any) {
    if (isTraceEnabled) {
        trace(msg().toString())
    }
}

internal inline fun Logger.trace(msg: () -> Any, t: Throwable) {
    if (isTraceEnabled) {
        trace(msg().toString(), t)
    }
}

internal inline fun Logger.debug(msg: () -> Any) {
    if (isDebugEnabled) {
        debug(msg().toString())
    }
}

internal inline fun Logger.debug(msg: () -> Any, t: Throwable) {
    if (isDebugEnabled) {
        trace(msg().toString(), t)
    }
}
