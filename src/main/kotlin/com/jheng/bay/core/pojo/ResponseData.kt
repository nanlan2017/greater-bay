package com.jheng.bay.core.pojo

import com.fasterxml.jackson.annotation.JsonIgnore

@Suppress("DataClassPrivateConstructor")
data class ResponseData private constructor(
        @JsonIgnore
        val status: ResponseStatus,
        val message: String,
        val data: Any? = null,
        /**
         * return a piece of information to help ourselves understand what's happening
         */
        val exception_hint: ExceptionHint? = null
) {

    companion object {
        private val successInstance = ResponseData(
                status = ResponseStatus.OK,
                message = "success"
        )

        fun success(data: Any? = null): ResponseData {
            if (data == null) return successInstance
            return successInstance.copy(data = data)
        }

        fun instance(
                status: ResponseStatus = ResponseStatus.OK,
                message: String = "success",
                data: Any? = null,
                exception: Throwable? = null
        ): ResponseData {
            return ResponseData(
                    status = status,
                    message = message,
                    data = data,
                    exception_hint = exception?.let { ExceptionHint.from(it) }
            )
        }
    }

    val code: String
        get() = status.code
}
