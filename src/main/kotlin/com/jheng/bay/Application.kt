package com.jheng.bay

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean


@SpringBootApplication
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }

    @Bean
    fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner { _ ->
            println("Let's inspect the beans provided by Spring Boot:")

            val beanNames = ctx.beanDefinitionNames
            beanNames.sort()
            for (beanName in beanNames) {
                println(beanName)
            }
        }
    }
}


