package com.prgrms.be02slack.channel.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChannelSaveRequest {
  @NotBlank
  @Size(min = 1, max = 80)
  private String name;

  private String description;

  @JsonProperty("isPrivate")
  private boolean isPrivate;

  public ChannelSaveRequest(String name, String description, boolean isPrivate) {
    this.name = name;
    this.description = description;
    this.isPrivate = isPrivate;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isPrivate() {
    return isPrivate;
  }
}
