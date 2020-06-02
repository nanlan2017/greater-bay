package com.jheng.bay.aspect

import com.jheng.bay.core.pojo.BusinessException
import com.jheng.bay.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.reflect.UndeclaredThrowableException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestControllerAdvice
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class GlobalExceptionAdvice {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var environment: Environment

    @ExceptionHandler(value = [Exception::class])
    @Throws(Exception::class)
    fun defaultErrorHandler(
            req: HttpServletRequest,
            response: HttpServletResponse,
            e: Exception
    ): ResponseEntity<String> {
        // set correct content type
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val body = handle(req, response, e)
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(body)
    }

    private fun handle(
            req: HttpServletRequest,
            response: HttpServletResponse,
            e: Exception
    ): String {
        val showErrorHint = environment.activeProfiles.toSet()
                .intersect(setOf("local", "dev", "debug"))
                .isNotEmpty()
        log.error("[exception]", e)
        if (e is BusinessException) {
            val error = if (showErrorHint) e else null
            return ResponseUtil.returnBusinessException(error)
        }
        if (e is UndeclaredThrowableException) {
            val error = if (showErrorHint) e else null
            return ResponseUtil.returnServerException(error?.undeclaredThrowable)
        }
        // It's better to let AuthExceptionFilter handle this exception
        // But I don't know how
//        if (e is AuthenticationException) {
//            response.status = HttpStatus.UNAUTHORIZED.value()
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
//        }
        return ResponseUtil.returnServerException(e)
    }
}
