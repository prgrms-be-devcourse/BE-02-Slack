package com.prgrms.be02slack.message.service;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.message.entity.Message;

public interface MessageService {

  Message sendMessage(Member member, String encodedChannelId, String content);
}
