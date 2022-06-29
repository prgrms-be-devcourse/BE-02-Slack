package com.prgrms.be02slack.subscribeInfo.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.util.Assert;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;

@Entity
public class SubscribeInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id")
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  protected SubscribeInfo() {/*no-op*/}

  private SubscribeInfo(Channel channel, Member member) {
    Assert.notNull(channel, "Channel must be provided");
    Assert.notNull(member, "Member must be provided");

    this.channel = channel;
    this.member = member;
  }

  public static SubscribeInfo subscribe(Channel channel, Member member) {
    Assert.notNull(channel, "Channel must be provided");
    Assert.notNull(member, "Member must be provided");

    final var subscribeInfo = new SubscribeInfo(channel, member);

    subscribeInfo.getChannel()
        .getSubscribeInfos()
        .add(subscribeInfo);

    subscribeInfo.getMember()
        .getSubscribeInfos()
        .add(subscribeInfo);

    return subscribeInfo;
  }

  public Channel getChannel() {
    return channel;
  }

  public Member getMember() {
    return member;
  }
}
