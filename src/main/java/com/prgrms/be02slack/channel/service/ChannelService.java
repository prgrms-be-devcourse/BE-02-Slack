package com.prgrms.be02slack.channel.service;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;

public interface ChannelService {
  String create(String workspaceId, ChannelSaveRequest channelSaveRequest);
}
