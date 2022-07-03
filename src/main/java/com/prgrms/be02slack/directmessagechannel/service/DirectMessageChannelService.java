package com.prgrms.be02slack.directmessagechannel.service;

import java.util.List;

import com.prgrms.be02slack.directmessagechannel.controller.dto.DirectMessageChannelResponse;
import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.member.entity.Member;

import com.prgrms.be02slack.member.entity.Member;

public interface DirectMessageChannelService {

  String create(String workspaceId, String receiverEmail, Member sender);

  List<DirectMessageChannelResponse> getChannels(Member member);
}
