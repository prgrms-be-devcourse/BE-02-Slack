package com.prgrms.be02slack.subscribeInfo.service;

import static org.apache.logging.log4j.util.Strings.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;
import com.prgrms.be02slack.subscribeInfo.repository.SubscribeInfoRepository;

@Service
@Transactional
public class DefaultSubscribeInfoService implements SubscribeInfoService {

  private static final String SUB_INFO_NOT_FOUND_MSG = "Subscribe information not found";
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

  @Override
  public void unsubscribe(Channel channel, Member member) {
    Assert.notNull(channel, "Channel must be provided");
    Assert.notNull(member, "Member must be provided");

    final var subscribeInfo = subscribeInfoRepository.findByChannelAndMember(channel, member)
        .orElseThrow(() -> new NotFoundException(SUB_INFO_NOT_FOUND_MSG));
    subscribeInfoRepository.delete(subscribeInfo);
  }

  @Override
  public boolean isExistsByChannelAndMemberEmail(Channel channel, String email) {
    Assert.notNull(channel, "Channel must be provided");
    Assert.isTrue(isNotBlank(email), "Email must be provided");

    return subscribeInfoRepository.existsByChannelAndMemberEmail(channel, email).isPresent();
  }

  @Override
  public boolean isExistsByChannelAndMemberName(Channel channel, String name) {
    Assert.notNull(channel, "Channel must be provided");
    Assert.isTrue(isNotBlank(name), "Name must be provided");

    return subscribeInfoRepository.existsByChannelAndMemberName(channel, name).isPresent();
  }

  @Override
  public List<SubscribeInfo> findAllByMember(Member member) {
    Assert.notNull(member, "Member must be provided");

    return subscribeInfoRepository.findAllByMember(member);
  }
}
