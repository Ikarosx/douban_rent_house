package cn.ikarosx.common.exception;


import cn.ikarosx.common.contants.ErrorCode;

/**
 * @author 许培宇
 * @date 2022/6/20 15:18
 */
public class NorthException extends RuntimeException{
    private ErrorCode errorCode;
    private String detailMessage;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }

    public static NorthException build(ErrorCode errorCode) {
        NorthException northException = new NorthException();
        northException.setErrorCode(errorCode);
        return northException;
    }

    public static NorthException build(ErrorCode errorCode, String detailMessage) {
        NorthException northException = new NorthException();
        northException.setErrorCode(errorCode);
        northException.setDetailMessage(detailMessage);
        return northException;
    }
}
