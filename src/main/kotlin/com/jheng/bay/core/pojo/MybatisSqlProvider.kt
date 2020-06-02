package com.jheng.bay.core.pojo

import org.apache.ibatis.builder.annotation.ProviderContext
import kotlin.reflect.full.memberFunctions

interface MybatisSqlProvider {
    companion object {
        /**
         * to make this work,
         * the provider must contains a method have the same name to context.mapperMethod.name
         */
        @JvmStatic
        fun provideSql(
                context: ProviderContext,
                provider: MybatisSqlProvider
        ): String {
            val method = provider::class.memberFunctions
                    .find {
                        it.name == context.mapperMethod.name
                    }
                    ?: throw IllegalArgumentException("no ${context.mapperMethod.name} method was found in ${provider::class.simpleName}")
            return method.call(provider) as String
        }
    }
}
