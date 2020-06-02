package com.jheng.bay.core.pojo

import ROOT_PACKAGE_NAME

class ExceptionHint private constructor(
        val exception_class: String,
        val message: String,
        val suspicious_stacktrace: List<StackTraceElement>
) {
    companion object {
        private const val PROXY_FILE_START_SYMBOL: String = "<"

        fun from(throwable: Throwable): ExceptionHint {
            return ExceptionHint(
                    throwable::class.qualifiedName ?: "",
                    throwable.message ?: "",
                    throwable.stackTrace.filter {
                        it.className.startsWith(ROOT_PACKAGE_NAME)
                                && !(it.fileName?.startsWith(PROXY_FILE_START_SYMBOL)
                                ?: true)
                    }
            )
        }
    }
}
