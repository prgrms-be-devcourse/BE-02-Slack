package com.prgrms.be02slack.subscribeInfo.service;

import java.util.List;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;

public interface SubscribeInfoService {

  void subscribe(Channel channel, Member member);

  void unsubscribe(Channel channel, Member member);

  boolean isExistsByChannelAndMemberEmail(Channel channel, String email);

  boolean isExistsByChannelAndMemberName(Channel channel, String name);

  List<SubscribeInfo> findAllByMember(Member member);

  boolean isExistsByMemberAndChannelId(Member member, Long channelId);

  List<SubscribeInfo> findAllByChannelId(Long channelId);
}
