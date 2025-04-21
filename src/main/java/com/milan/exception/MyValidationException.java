package com.milan.exception;

import java.util.Map;

public class MyValidationException extends RuntimeException {

    private Map<String,Object> errors;

    public MyValidationException(Map<String,Object> errors) {
        super();
        this.errors = errors;
    }

    public Map<String,Object> getErrors() {
        return errors;
    }

}
