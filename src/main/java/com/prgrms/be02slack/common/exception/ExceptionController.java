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

@ControllerAdvice
public class ExceptionController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @Order(1)
  public void handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    logger.warn("MethodArgumentNotValidException : ", e);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @Order(2)
  public void handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    logger.warn("MethodArgumentTypeMismatchException : ", e);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @Order(3)
  public void handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    logger.warn("HttpMessageNotReadableException : ", e);
  }

  @ExceptionHandler(value = {
      ConstraintViolationException.class,
      MissingPathVariableException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @Order(4)
  public void handleConstraintViolationException(Exception e) {
    logger.warn("PathVariable is blank : ", e);
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @Order(5)
  public void handleNotFoundException(NotFoundException e) {
    logger.warn("NotFoundException : ", e);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @Order(6)
  public void handleRuntimeException(RuntimeException e) {
    logger.error("RuntimeException : ", e);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @Order(7)
  public void handleException(Exception e) {
    logger.error("Exception :", e);
  }
}
