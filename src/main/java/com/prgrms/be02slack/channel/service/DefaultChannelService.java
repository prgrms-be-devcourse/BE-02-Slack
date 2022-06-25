package com.prgrms.be02slack.channel.service;

import org.springframework.stereotype.Service;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;

@Service
public class DefaultChannelService implements ChannelService {
  @Override
  public String create(ChannelSaveRequest channelSaveRequest) {
    return null;
  }
}
