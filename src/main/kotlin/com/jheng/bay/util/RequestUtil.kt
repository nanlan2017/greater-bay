package com.jheng.bay.util

import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object RequestUtil {
    fun getHeaderParam(key: String?): Int {
        val defaultVal = 1
        return try {
            val str = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
                    .request.getHeader(key)
            if (StringUtils.isEmpty(str)) defaultVal else Integer.parseInt(str)
        } catch (ignore: Exception) {
            defaultVal
        }
    }
}
