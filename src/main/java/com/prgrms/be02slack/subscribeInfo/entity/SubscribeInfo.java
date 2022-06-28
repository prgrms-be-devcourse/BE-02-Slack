package com.prgrms.be02slack.subscribeInfo.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.member.entity.Member;

@Entity
public class SubscribeInfo extends BaseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "channel_id")
  private List<Channel> channels = new ArrayList<>();

  protected SubscribeInfo() {/*no-op*/}
}
