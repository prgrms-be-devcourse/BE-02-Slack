package com.prgrms.be02slack.message.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.member.entity.Member;

@Entity
public class Message extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "channel_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Channel channel;

  @JoinColumn(name = "member_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Member author;

  @Enumerated(EnumType.STRING)
  private MessageType messageType;

  @Column(length = 12000)
  private String content;

  protected Message() {/*no-op*/}

  private Message(Builder builder) {
    this.channel = builder.channel;
    this.author = builder.author;
    this.content = builder.content;
  }

  public static class Builder {
    private Channel channel;
    private Member author;
    private String content;

    public Builder channel(Channel channel) {
      this.channel = channel;
      return this;
    }

    public Builder author(Member author) {
      this.author = author;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }
  }
}
