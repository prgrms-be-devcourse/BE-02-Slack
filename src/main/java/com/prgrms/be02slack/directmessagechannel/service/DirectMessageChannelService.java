package com.prgrms.be02slack.directmessagechannel.service;


public interface DirectMessageChannelService {

  String create(String workspaceId, String receiverEmail);
}
