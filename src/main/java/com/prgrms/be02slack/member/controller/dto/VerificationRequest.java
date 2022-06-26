package com.prgrms.be02slack.member.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class VerificationRequest {

  @Email
  @NotBlank
  private final String email;

  @NotBlank
  private final String verificationCode;

  public VerificationRequest(String email, String verificationCode) {
    this.email = email;
    this.verificationCode = verificationCode;
  }

  public String getEmail() {
    return email;
  }

  public String getVerificationCode() {
    return verificationCode;
  }
}
