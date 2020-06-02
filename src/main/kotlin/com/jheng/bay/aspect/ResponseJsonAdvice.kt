package com.jheng.bay.aspect

import com.jheng.bay.core.pojo.ResponseData
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


/**
 * this class save us from writing ResponseUtil.returnJson(data) everywhere.
 * also, if a controller method have no return value, it is equal to ResponseUtil.returnSuccess
 */
@RestControllerAdvice
class ResponseJsonAdvice : ResponseBodyAdvice<Any> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return AbstractJackson2HttpMessageConverter::class.java.isAssignableFrom(converterType)
    }

    override fun beforeBodyWrite(
            body: Any?,
            returnType: MethodParameter,
            selectedContentType: MediaType,
            selectedConverterType: Class<out HttpMessageConverter<*>>,
            request: ServerHttpRequest,
            response: ServerHttpResponse
    ): Any? {
        if (body is String) return body
        val status = (response as? ServletServerHttpResponse)?.servletResponse?.status
                ?: return body
        return if (status in 200..299) {
            ResponseData.success(data = body)
        } else {
            body
        }
    }
}
