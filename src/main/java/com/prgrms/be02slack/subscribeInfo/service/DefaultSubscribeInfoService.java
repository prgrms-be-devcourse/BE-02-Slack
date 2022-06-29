package com.prgrms.be02slack.subscribeInfo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;
import com.prgrms.be02slack.subscribeInfo.repository.SubscribeInfoRepository;

@Service
@Transactional
public class DefaultSubscribeInfoService implements SubscribeInfoService {

  private final SubscribeInfoRepository subscribeInfoRepository;

  public DefaultSubscribeInfoService(SubscribeInfoRepository subscribeInfoRepository) {
    this.subscribeInfoRepository = subscribeInfoRepository;
  }

  @Override
  public void subscribe(Channel channel, Member member) {
    Assert.notNull(channel, "Channel must be provided");
    Assert.notNull(member, "Member must be provided");

    final var subscribeInfo = SubscribeInfo.subscribe(channel, member);
    subscribeInfoRepository.save(subscribeInfo);
  }
}
