package com.prgrms.be02slack.channel.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChannelResponse {
  private final String id;
  private final String name;
  private final boolean isPrivate;

  public ChannelResponse(String id, String name, boolean isPrivate) {
    this.id = id;
    this.name = name;
    this.isPrivate = isPrivate;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @JsonProperty("isPrivate")
  public boolean isPrivate() {
    return isPrivate;
  }
}
