package com.prgrms.be02slack.channel.exception;

public class NameDuplicateException extends RuntimeException {
  public NameDuplicateException() {
  }

  public NameDuplicateException(String message) {
    super(message);
  }
}
