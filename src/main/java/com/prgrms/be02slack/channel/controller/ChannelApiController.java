package com.prgrms.be02slack.channel.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.service.ChannelService;

@RestController
@RequestMapping("api/v1/channels")
public class ChannelApiController {
  private final ChannelService channelService;

  public ChannelApiController(ChannelService channelService) {
    this.channelService = channelService;
  }

  @PostMapping
  public String create(@Valid @RequestBody ChannelSaveRequest channelSaveRequest) {
    return channelService.create(channelSaveRequest);
  }
}
