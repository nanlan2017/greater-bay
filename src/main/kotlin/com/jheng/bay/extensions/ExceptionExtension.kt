package com.jheng.bay.extensions

import java.io.PrintWriter
import java.io.StringWriter

internal fun Throwable.stacktraceString(): String {
    val sw = StringWriter()
    this.printStackTrace(PrintWriter(sw))
    return sw.toString()
}
