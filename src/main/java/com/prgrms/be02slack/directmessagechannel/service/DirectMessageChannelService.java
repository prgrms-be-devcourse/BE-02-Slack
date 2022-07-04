package com.prgrms.be02slack.directmessagechannel.service;

import com.prgrms.be02slack.member.entity.Member;

public interface DirectMessageChannelService {

  String create(String workspaceId, String receiverEmail, Member sender);
}
