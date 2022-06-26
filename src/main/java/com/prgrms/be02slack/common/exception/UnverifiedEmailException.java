package com.prgrms.be02slack.common.exception;

public class UnverifiedEmailException extends RuntimeException {
  public UnverifiedEmailException() {
  }

  public UnverifiedEmailException(String message) {
    super(message);
  }
}
