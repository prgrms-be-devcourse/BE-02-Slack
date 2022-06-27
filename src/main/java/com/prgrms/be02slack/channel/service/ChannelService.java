package com.prgrms.be02slack.channel.service;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.common.dto.ApiResponse;

public interface ChannelService {
  String create(String workspaceId, ChannelSaveRequest channelSaveRequest);

  ApiResponse verifyName(String workspaceId, String name);
}
