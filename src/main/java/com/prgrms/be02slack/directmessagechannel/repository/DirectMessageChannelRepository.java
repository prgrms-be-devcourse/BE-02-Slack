package com.prgrms.be02slack.directmessagechannel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.member.entity.Member;

import io.lettuce.core.dynamic.annotation.Param;

public interface DirectMessageChannelRepository extends JpaRepository<DirectMessageChannel, Long> {

  @Query("select d from DirectMessageChannel d where (d.firstMember = :firstMember "
      + "and d.secondMember = :secondMember) or (d.firstMember = :secondMember "
      + "and d.secondMember = :firstMember)")
  Optional<DirectMessageChannel> findByFirstMemberAndSecondMember(
      @Param("firstMember") Member firstMember,
      @Param("secondMember") Member secondMember);
}
