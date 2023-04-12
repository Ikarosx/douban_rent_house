package cn.ikarosx.config;

import cn.ikarosx.common.contants.ErrorCode;
import cn.ikarosx.common.exception.NorthException;
import cn.ikarosx.common.exception.ResultMsg;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 许培宇
 * @since 2022-11-16 15:08:14
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Pattern tableDoesNotExistedPattern = Pattern.compile(".*\\s\\'(.*)\\'\\sdoes");
    private final Pattern dataTooLongPattern = Pattern.compile(".*\\'(.*)\\'");
    private final Pattern dataTooLongSqlPattern = Pattern.compile(".*SQL.\\s(.*)", Pattern.MULTILINE);
    private final Pattern columnDoesNotExistedPattern = Pattern.compile("column\\s\\'(.*)\\'\\sin", Pattern.MULTILINE);

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultMsg<Void>  methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) {
        return handlerException(exception.getBindingResult());
    }

    @ExceptionHandler(value = BindException.class)
    public ResultMsg<Void> bindExceptionHandler(BindException exception) {
        return handlerException(exception.getBindingResult());
    }


    @ExceptionHandler(value = NorthException.class)
    public ResultMsg<Void> northException(NorthException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.error("发生业务异常-code:{},message:{}", errorCode.getCode(), errorCode.getMessage(), exception);
        return ResultMsg.error(errorCode);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResultMsg<Void> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return ResultMsg.error(ErrorCode.UN_SUPPORT_METHOD);
    }

    @ExceptionHandler(value = Exception.class)
    public ResultMsg<Void> northException(Exception exception) {
        log.error("发生未知错误", exception);
        return ResultMsg.error(ErrorCode.INTERNAL_ERROR);
    }

    /**
     * 数据约束异常
     * @param exception
     * @return
     */
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResultMsg<Void> dataIntegrityViolationException(DataIntegrityViolationException exception) {
        if (exception.getCause() instanceof MysqlDataTruncation) {
            MysqlDataTruncation mysqlDataTruncation = (MysqlDataTruncation) exception.getCause();
            switch (mysqlDataTruncation.getSQLState()) {
                case "22001":
                    // 数据库字段长度不够

                    // Data too long for column 'name' at row 1
                    String detailMessage = mysqlDataTruncation.getMessage();
                    Matcher columnMatcher = dataTooLongPattern.matcher(detailMessage);
                    String column = "";
                    if (columnMatcher.find()) {
                        column = columnMatcher.group(1);
                    }
                    // SQL: INSERT INTO demo  ( id, name )  VALUES  ( ?, ? )
                    String message = exception.getMessage();
                    Matcher sqlMatcher = dataTooLongSqlPattern.matcher(message);
                    String sql = "";
                    if (sqlMatcher.find()) {
                        sql = sqlMatcher.group(1);
                    }
                    log.error("数据库字段长度不够,字段:{},SQL:{}", column, sql);
                    return ResultMsg.error(ErrorCode.SQL_DATA_TOO_LONG_ERROR, "字段:" + column + ", SQL:" + sql);
                default:
                    break;
            }
        }
        log.error("出现数据库约束异常", exception);
        return ResultMsg.error(ErrorCode.SQL_DATA_INTEGRITY_VIOLATION_ERROR);
    }


    /**
     * MySQL语法错误
     * @param exception
     * @return
     */
    @ExceptionHandler(value = BadSqlGrammarException.class)
    public ResultMsg<Void> sqlSyntaxErrorException(BadSqlGrammarException exception) {
        SQLException sqlException = exception.getSQLException();
        String sqlState = sqlException.getSQLState();
        log.error("mysql发生错误", exception);
        String message = sqlException.getMessage();
        switch (sqlState) {
            case  "42S02":
                // Table 'schoolmain.test222' doesn't exist
                Matcher matcher = tableDoesNotExistedPattern.matcher(message);
                String table = "";
                if (matcher.find()) {
                    table = matcher.group(1);
                }
                return ResultMsg.error(ErrorCode.TABLE_NOT_EXISTED, "表名：" + table);
            case  "42S22":
                // Unknown column 'aaa' in 'field list'
                Matcher columnMatcher = columnDoesNotExistedPattern.matcher(message);
                String column = "";
                if (columnMatcher.find()) {
                    column = columnMatcher.group(1);
                }
                return ResultMsg.error(ErrorCode.COLUMN_NOT_EXISTED, "字段：" + column);
            case  "42000":
                return ResultMsg.error(ErrorCode.SQL_SYNTAX_ERROR);
            default:
                return ResultMsg.error(ErrorCode.SQL_ERROR);
        }
    }


    private ResultMsg<Void> handlerException(BindingResult result) {
        List<FieldError> errors = result.getFieldErrors();
        StringBuffer msg = new StringBuffer();
        errors.forEach(fieldError -> {
            msg.append(fieldError.getField()).append(fieldError.getDefaultMessage()).append(";");
        });
        return ResultMsg.error(ErrorCode.PARAM_VALIDATE_ERROR, msg.toString());
    }
}
