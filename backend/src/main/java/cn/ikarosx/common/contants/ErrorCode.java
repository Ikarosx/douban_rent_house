package cn.ikarosx.common.contants;

/**
 * @author 许培宇
 * @since 2022-11-16 15:10:35
 */
public enum ErrorCode {
    INTERNAL_ERROR(500,  "服务器内部错误"),
    // 1000 公用,
    PARAM_LIMIT(1001,  "参数不合理"),
    JSON_FORMAT_ERROR(1002,  "您发送的请求中，json格式不正确"),
    PARAM_VALIDATE_ERROR(1003,  "参数校验异常"),
    UN_SUPPORT_METHOD(1004,  "不支持该请求类型"),
    UN_SUPPORT_OPERATE(1005,  "暂不支持该操作"),
    ILLEGAL_PAGE_PARAM(1006,  "非法的分页参数"),
    ILLEGAL_METHOD(1006,  "不允许PUT和DELETE"),
    DATA_NOT_FOUND(1007,  "数据不存在"),
    // 1100 SQL
    TABLE_NOT_EXISTED(1101,  "数据库表不存在"),
    SQL_ERROR(1102,  "数据库异常"),
    SQL_SYNTAX_ERROR(1103,  "SQL代码错误"),
    SQL_DATA_INTEGRITY_VIOLATION_ERROR(1104,  "数据库约束异常"),
    SQL_DATA_TOO_LONG_ERROR(1105,  "数据库字段长度不够"),
    COLUMN_NOT_EXISTED(1106,  "字段不存在"),

   

    ;
    private final Integer code;

    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }


    public String getMessage() {
        return message;
    }

}
