package com.prgrms.be02slack.common.exception;

public enum ErrorMessage {
  INVALID_TOKEN("Invalid token");

  ErrorMessage(String msg) {
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }

  private final String msg;
}
