package com.prgrms.be02slack.common.exception;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.prgrms.be02slack.channel.exception.NameDuplicateException;

@ControllerAdvice
public class ExceptionController {

  @ExceptionHandler(value = {
      ConstraintViolationException.class,
      MissingPathVariableException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleConstraintViolationException(Exception e) {
  }

  @ExceptionHandler(value = NameDuplicateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public void handleNameDuplicateException(Exception e) {
  }
}
