package com.prgrms.be02slack.message.controller.dto;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

public class MessageWebsocketRequest {

  @Length(max = 12000)
  @NotBlank
  private String content;

  public MessageWebsocketRequest() {/*no-op*/}

  public MessageWebsocketRequest(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }
}
