package com.prgrms.be02slack.workspace.controller.dto;

import javax.validation.constraints.Email;

public class MemberInviteRequest {

  @Email
  private String recipientEmail;

  protected MemberInviteRequest() { }

  public String getRecipientEmail() {
    return recipientEmail;
  }
}
