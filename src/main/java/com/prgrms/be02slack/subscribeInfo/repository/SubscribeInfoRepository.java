package com.prgrms.be02slack.subscribeInfo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;

public interface SubscribeInfoRepository extends JpaRepository<SubscribeInfo, Long> {
  Optional<SubscribeInfo> findByChannelAndMember(Channel channel, Member member);

  @Query("select s from SubscribeInfo s where s.channel =:channel and s.member.email = :email")
  Optional<SubscribeInfo> existsByChannelAndMemberEmail(@Param("channel") Channel channel,
      @Param("email") String email);

  @Query("select s from SubscribeInfo s where s.channel =:channel and s.member.name = :name")
  Optional<SubscribeInfo> existsByChannelAndMemberName(@Param("channel") Channel channel,
      @Param("name") String name);

  @Query("select s from SubscribeInfo s join fetch s.channel join fetch s.member where s.member =:member")
  List<SubscribeInfo> findAllByMember(@Param("member") Member member);
}
