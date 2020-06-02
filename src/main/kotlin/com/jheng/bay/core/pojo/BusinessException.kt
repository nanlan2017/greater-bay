package com.jheng.bay.core.pojo

class BusinessException(
        message: String? = null,
        cause: Throwable? = null
) : RuntimeException(message, cause)
