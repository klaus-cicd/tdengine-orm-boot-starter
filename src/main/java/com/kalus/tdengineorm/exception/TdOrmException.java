package com.kalus.tdengineorm.exception;

import com.klaus.fd.exception.AbstractException;
import com.klaus.fd.exception.ExceptionCode;

/**
 * @author Klaus
 */
public class TdOrmException extends AbstractException {
    public TdOrmException(Integer code, String message) {
        super(code, message);
    }

    public TdOrmException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

}
