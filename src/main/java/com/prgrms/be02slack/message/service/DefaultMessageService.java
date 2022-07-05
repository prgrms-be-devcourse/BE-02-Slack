package com.prgrms.be02slack.message.service;

import org.springframework.stereotype.Service;

import com.prgrms.be02slack.message.entity.Message;

@Service
public class DefaultMessageService implements MessageService {

  @Override
  public Message sendMessage(String senderEmail, String encodedChannelId, String content) {
    return null;
  }
}
