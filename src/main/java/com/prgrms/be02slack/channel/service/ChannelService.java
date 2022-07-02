package com.prgrms.be02slack.channel.service;

import javax.mail.MessagingException;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.controller.dto.InviteRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.common.dto.AuthResponse;

public interface ChannelService {
  String create(String workspaceId,
      ChannelSaveRequest channelSaveRequest);

  void invite(String workspaceId,
      String channelId,
      InviteRequest inviteRequest) throws
      MessagingException;

  AuthResponse participate(String workspaceId,
      String channelId,
      String token);

  Channel findByKey(String key);
}

