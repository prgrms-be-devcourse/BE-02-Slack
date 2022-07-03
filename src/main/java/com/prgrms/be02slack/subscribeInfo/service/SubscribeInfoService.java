package com.prgrms.be02slack.subscribeInfo.service;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;

public interface SubscribeInfoService {

  void subscribe(Channel channel, Member member);

  void unsubscribe(Channel channel, Member member);

  boolean isExistsByChannelAndMemberEmail(Channel channel, String email);

  boolean isExistsByChannelAndMemberName(Channel channel, String name);
}
