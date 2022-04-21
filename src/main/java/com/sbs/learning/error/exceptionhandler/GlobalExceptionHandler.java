package com.sbs.learning.error.exceptionhandler;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.sbs.learning.error.exception.ApiException;
import com.sbs.learning.model.http.response.ApiResponse;

import javax.servlet.http.HttpServletResponse;

import javax.validation.ConstraintViolationException;
import org.springframework.security.authentication.BadCredentialsException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return handleExceptionInternal(ex, "erro interno.", new HttpHeaders(), status, request);
    }

    //handle for @Valid objectDTO annotated with @RequestBody
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());

        //Get all errors
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(x -> x.getField() + "-" + x.getDefaultMessage())
            .collect(Collectors.toList());

        body.put("errors", errors);

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(WebRequest request, ApiException ex) throws IOException {
        ApiResponse apiResponse = new ApiResponse(ex.getHttpStatus().value(), ex.getMessage());

        return handleExceptionInternal(ex, apiResponse, new HttpHeaders(), ex.getHttpStatus(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public void constraintViolationException( HttpServletResponse response ) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentialsException( HttpServletResponse response, BadCredentialsException ex) throws IOException {  
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
    
}
