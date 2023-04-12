package cn.ikarosx.common.exception;

import cn.ikarosx.common.contants.ErrorCode;

/**
 * @author 许培宇
 * @since 2022-11-16 15:22:30
 */
public class ResultMsg<T> {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private String detailMessage;
    private T data;

    public static <T> ResultMsg<T> ok(T data) {
        ResultMsg<T> r = new ResultMsg<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static ResultMsg<Void> ok() {
        return new ResultMsg<Void>();
    }


    public static ResultMsg<Void> error(ErrorCode errorCode) {
        ResultMsg<Void> r = new ResultMsg<>();
        r.code = errorCode.getCode();
        r.message = errorCode.getMessage();
        return r;
    }

    public static ResultMsg<Void> error(ErrorCode errorCode, String detailMsg) {
        ResultMsg<Void> r = new ResultMsg<>();
        r.code = errorCode.getCode();
        r.message = errorCode.getMessage();
        r.detailMessage = detailMsg;
        return r;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public T getData() {
        return data;
    }
}
