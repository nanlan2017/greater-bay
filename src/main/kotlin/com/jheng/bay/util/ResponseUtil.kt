package com.jheng.bay.util

import com.jheng.bay.core.pojo.BusinessException
import com.jheng.bay.core.pojo.ResponseData
import com.jheng.bay.core.pojo.ResponseStatus
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * this class is not that useful as we added the ResponseData class
 * see ResponseData and ResponseJsonAdvice
 */
object ResponseUtil {

    fun returnSuccess(): String {
        return returnJson()
    }

    fun returnServerException(e: Throwable? = null): String {
        return returnException(
                status = ResponseStatus.SERVER_EXCEPTION,
                message = "server is busy",
                e = e
        )
    }

    fun returnBusinessException(e: BusinessException? = null): String {
        return returnException(
                status = ResponseStatus.BUSINESS_EXCEPTION,
                message = e?.message ?: "",
                e = e
        )
    }

    private fun returnException(
            status: ResponseStatus,
            message: String,
            e: Throwable? = null
    ): String {
        val res = ResponseData.instance(
                status = status,
                message = message,
                exception = e
        )
        return JsonUtil.stringify(res)
    }

    fun returnJson(
            data: Any? = null,
            status: ResponseStatus = ResponseStatus.OK,
            message: String = "success"
    ): String {
        val res = ResponseData.instance(
                status = status,
                message = message,
                data = data
        )
        return JsonUtil.stringify(res)
    }

    fun downLoadExcel(
            fileName: String,
            outputStream: ByteArrayOutputStream
    ): ResponseEntity<*> {
        var fileName = fileName
        val resource = ByteArrayResource(outputStream.toByteArray())
        fileName += ".xls"
        // 2.1、设置报文头
        val headers = HttpHeaders()
        headers.accessControlExposeHeaders = mutableListOf("Content-Disposition");
        headers.contentDisposition = ContentDisposition.parse("attachment; filename=" + String(fileName.toByteArray(charset("GBK")), Charset.forName("ISO-8859-1")))
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.valueOf("application/vnd.ms-excel"))
                .body(resource)
    }
}
