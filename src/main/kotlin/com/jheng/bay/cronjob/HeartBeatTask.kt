package com.jheng.bay.cronjob

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class HeartBeatTask {
    private val log = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    private lateinit var env: Environment

    @Scheduled(fixedRate = 60 * 1000)
    fun heartbeat() {
        log.info("[{}] heartbeat", env.getProperty("spring.profiles.active"))
    }
}
