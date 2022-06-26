package com.prgrms.be02slack.channel.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.service.ChannelService;

@Validated
@RestController
@RequestMapping("api/v1/workspaces/{workspaceId}/channels")
public class ChannelApiController {
  private final ChannelService channelService;

  public ChannelApiController(ChannelService channelService) {
    this.channelService = channelService;
  }

  @PostMapping
  public String create(
      @PathVariable @NotBlank String workspaceId,
      @Valid @RequestBody ChannelSaveRequest channelSaveRequest) {
    return channelService.create(workspaceId, channelSaveRequest);
  }
}
