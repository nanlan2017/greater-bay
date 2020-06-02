package com.jheng.bay.core

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
object AppContextHolder : ApplicationContextAware {
    @Volatile
    private var field: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        field = applicationContext
    }

    val context: ApplicationContext
        get() = AppContextHolder.field ?: throw RuntimeException("application context has not read yet")
}
