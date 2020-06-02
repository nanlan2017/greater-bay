package com.jheng.bay.config

import org.springframework.context.annotation.Configuration

//import org.springframework.boot.web.servlet.FilterRegistrationBean
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.util.AntPathMatcher
//import org.springframework.web.cors.CorsUtils
//import sun.net.httpserver.AuthFilter
//import javax.servlet.http.HttpServletRequest
//
@Configuration
class AuthConfig {
//
//    val publicRequestMatcher = object : RequestMatcher {
//        private val matcher = AntPathMatcher()
//
//        override fun matches(request: HttpServletRequest): Boolean {
//            // we allow cors pre flight request
//            // let cors filter do its work
//            if (CorsUtils.isPreFlightRequest(request)) {
//                return true
//            }
//
//            val path = getPath(request)
//            val patterns = listOf(
//                    "/**/*.html",
//                    "/**/*.css",
//                    "/**/*.js",
//                    "/compliance_portal/auth/**",
//                    "/actuator/**"
//            )
//            return patterns.any {
//                matcher.match(it, path)
//            }
//        }
//
//        private fun getPath(request: HttpServletRequest): String {
//            val servletPath = request.servletPath
//            val pathInfo = request.pathInfo ?: ""
//            return if (servletPath.isNotBlank())
//                servletPath + pathInfo
//            else pathInfo
//        }
//    }

//    @Bean
//    fun authFilter(): FilterRegistrationBean<AuthFilter> {
//        val filter = AuthFilter(publicRequestMatcher = publicRequestMatcher)
//        val registration = FilterRegistrationBean<AuthFilter>(filter)
//        registration.order = 1000
//        return registration
//    }
}
