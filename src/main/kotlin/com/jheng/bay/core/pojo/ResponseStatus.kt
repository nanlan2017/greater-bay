package com.jheng.bay.core.pojo

enum class ResponseStatus(
        val code: String,
        val description: String
) {
    OK("0000", "正常"),
    BUSINESS_EXCEPTION("0201", "业务逻辑错误"),
    SERVER_EXCEPTION("0202", "服务器异常"),
    SESSION_EXPIRE("0203", "Session失效"),
    ;
}
