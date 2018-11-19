package net.engining.pg.support.db.id.generator;

import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;

/**
 * 生成ID异常.
 */
public class IdGenException extends ErrorMessageException {

    private static final long serialVersionUID = 1L;

    public IdGenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public IdGenException(ErrorCode errorCode, String message, Throwable t) {
        super(errorCode, message, t);
    }
}
