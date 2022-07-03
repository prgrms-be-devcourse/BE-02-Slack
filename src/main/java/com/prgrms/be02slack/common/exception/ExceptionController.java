package com.prgrms.be02slack.common.exception;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.prgrms.be02slack.channel.exception.NameDuplicateException;

@ControllerAdvice
public class ExceptionController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Order(1)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    logger.warn("MethodArgumentNotValidException : ", e);
  }

  @Order(2)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    logger.warn("MethodArgumentTypeMismatchException : ", e);
  }

  @Order(3)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    logger.warn("HttpMessageNotReadableException : ", e);
  }

  @Order(4)
  @ExceptionHandler(value = {
      ConstraintViolationException.class,
      MissingPathVariableException.class,
      IllegalArgumentException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleConstraintViolationException(Exception e) {
    logger.warn("PathVariable is blank : ", e);
  }

  @Order(5)
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleNotFoundException(NotFoundException e) {
    logger.warn("NotFoundException : ", e);
  }

  @Order(6)
  @ExceptionHandler(value = NameDuplicateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public void handleNameDuplicateException(NameDuplicateException e) {
    logger.warn("NameDuplicateException :", e);
  }

  @Order(7)
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void handleRuntimeException(RuntimeException e) {
    logger.error("RuntimeException : ", e);
  }

  @Order(8)
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void handleException(Exception e) {
    logger.error("Exception :", e);
  }
}
