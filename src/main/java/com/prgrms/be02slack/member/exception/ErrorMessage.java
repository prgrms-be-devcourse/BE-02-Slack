package com.prgrms.be02slack.member.exception;

public enum ErrorMessage {
  CHANNEL_NOT_ACCESS("The member do not have access to the channel.");

  ErrorMessage(String msg) {
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }

  private final String msg;
}
