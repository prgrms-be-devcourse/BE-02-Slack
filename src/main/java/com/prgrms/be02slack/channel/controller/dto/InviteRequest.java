package com.prgrms.be02slack.channel.controller.dto;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class InviteRequest {
  @NotEmpty
  private Set<String> inviteeInfos = new HashSet<>();

  @NotBlank
  @Size(min = 1, max = 80)
  private String sender;

  public InviteRequest(Set<String> inviteeInfos, String sender) {
    this.inviteeInfos = inviteeInfos;
    this.sender = sender;
  }

  public Set<String> getInviteeInfos() {
    return inviteeInfos;
  }

  public String getSender() {
    return sender;
  }
}
