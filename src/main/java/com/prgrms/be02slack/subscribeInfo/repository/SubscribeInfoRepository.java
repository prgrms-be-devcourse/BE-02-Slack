package com.prgrms.be02slack.subscribeInfo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;

public interface SubscribeInfoRepository extends JpaRepository<SubscribeInfo, Long> {
  Optional<SubscribeInfo> findByChannelAndMember(Channel channel, Member member);
}
