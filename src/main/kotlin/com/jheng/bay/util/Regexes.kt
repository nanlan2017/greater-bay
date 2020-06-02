package com.jheng.bay.util

object Regexes {
    val NUMBER = """\d+(\.\d+)?""".toRegex()

    val UTC_Time = """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(.\d{2})?Z""".toRegex()
}