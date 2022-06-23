package com.prgrms.be02slack.channel.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.channel.service.ChannelService;

@RestController
@RequestMapping("api/v1/channels")
public class ChannelApiController {
  private final ChannelService channelService;

  public ChannelApiController(ChannelService channelService) {
    this.channelService = channelService;
  }
}
