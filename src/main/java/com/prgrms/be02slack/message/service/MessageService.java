package com.prgrms.be02slack.message.service;

import com.prgrms.be02slack.message.entity.Message;

public interface MessageService {

  Message sendMessage(String encodedChannelId, String content);
}
