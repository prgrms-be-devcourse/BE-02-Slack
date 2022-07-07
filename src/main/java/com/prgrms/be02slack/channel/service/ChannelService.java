package com.prgrms.be02slack.channel.service;

import java.util.List;

import javax.mail.MessagingException;

import com.prgrms.be02slack.channel.controller.dto.ChannelResponse;
import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.controller.dto.InviteRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Member;

public interface ChannelService {
  String create(
      Member member,
      String workspaceId,
      ChannelSaveRequest channelSaveRequest);

  void invite(String workspaceId,
      String channelId,
      InviteRequest inviteRequest) throws
      MessagingException;

  AuthResponse participate(String workspaceId,
      String channelId,
      String token);

  Channel findByKey(String key);

  List<ChannelResponse> findAllByMember(Member member);

  void leave(String channelId, Member member);

  void inviteMember(Member sender, String encodedWorkspaceId, InviteRequest inviteRequest) throws
      MessagingException;
}

