package com.prgrms.be02slack.directmessagechannel.controller.dto;

public class DirectMessageChannelResponse {

  private final String memberName;
  private final String encodedDirectMessageChannelId;

  public DirectMessageChannelResponse(String memberName, String encodedDirectMessageChannelId) {
    this.memberName = memberName;
    this.encodedDirectMessageChannelId = encodedDirectMessageChannelId;
  }
}
