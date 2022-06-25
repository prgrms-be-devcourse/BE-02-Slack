package com.prgrms.be02slack.common.exception;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {

  @ExceptionHandler(value = {
      ConstraintViolationException.class,
      MissingPathVariableException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleConstraintViolationException(Exception e) {
  }
}
