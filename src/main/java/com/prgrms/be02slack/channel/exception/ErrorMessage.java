package com.prgrms.be02slack.channel.exception;

public enum ErrorMessage {
  ALREADY_SUBSCRIBER("The invitee is already a subscriber"),
  NOT_WORKSPACE_MEMBER("The invitee is a person who does not exist in the workspace"),
  ;

  ErrorMessage(String msg) {
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }

  private final String msg;
}
