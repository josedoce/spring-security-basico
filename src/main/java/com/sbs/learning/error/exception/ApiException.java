package com.sbs.learning.error.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends Exception {

    private String message;
    private HttpStatus httpStatus;
  
    private static final long serialVersionUID = 1L;

    public ApiException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage(){
         return  this.message;
    }
    
    public void setHttpStatus(HttpStatus httpStatus){
        this.httpStatus = httpStatus;
    }
    
    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }
}
