package com.jheng.bay.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.jheng.bay.base.query.ModelQueryArgumentResolver
import com.jheng.bay.util.JacksonUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

//@Configuration
class WebConfig : WebMvcConfigurer {

    /**
     * use customized objectMapper
     */
    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val objectMapper = builder.build<ObjectMapper>()
        return JacksonUtil.configure(objectMapper)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .exposedHeaders("Content-Disposition")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(ModelQueryArgumentResolver())
    }
}
