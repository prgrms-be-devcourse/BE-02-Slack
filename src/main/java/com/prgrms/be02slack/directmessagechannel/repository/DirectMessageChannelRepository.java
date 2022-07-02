package com.prgrms.be02slack.directmessagechannel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.member.entity.Member;

public interface DirectMessageChannelRepository extends JpaRepository<DirectMessageChannel, Long> {

  Optional<DirectMessageChannel> findByFirstMemberAndSecondMember(
      Member firstMember,
      Member secondMember);
}
