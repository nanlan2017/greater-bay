package com.jheng.bay.features

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BayController {
    @Autowired(required = false)
    private var buildProperties: BuildProperties? = null
    @Autowired(required = false)
    private var gitProperties: GitProperties? = null

    @GetMapping("/build_info")
    fun build_info(): BuildProperties? {
        return buildProperties
    }

    @GetMapping("/git_info")
    fun git_info(): GitProperties? {
        return gitProperties
    }
    @GetMapping
    fun index(): String {
        return "Hello, welcome to big bay area!"
    }
}