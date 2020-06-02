package com.jheng.bay.aspect

import com.fasterxml.jackson.databind.JsonMappingException
import com.jheng.bay.util.JsonUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class ActionLogAdvice {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Around("execution(* com.jheng.bay.controller..*.*(..))")
    @Throws(Throwable::class)
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {

        val args = try {
            JsonUtil.stringify(joinPoint.args)
        } catch (e: JsonMappingException) {
            "arguments cannot be serialize to json"
        }

        log.info("[ARGS] ${joinPoint.toShortString()} [args]$args")
        return joinPoint.proceed()
    }
}
