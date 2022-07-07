package com.prgrms.be02slack.message.entity;

import static org.apache.logging.log4j.util.Strings.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.common.enums.EntityIdType;
import com.prgrms.be02slack.member.entity.Member;

@Entity
public class Message extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @JoinColumn(name = "member_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @NotBlank
  private String encodedChannelId;

  @NotBlank
  @Column(length = 12000)
  private String content;

  @Transient
  private EntityIdType type = EntityIdType.MESSAGE;

  protected Message() {/*no-op*/}

  private Message(Builder builder) {
    Assert.notNull(builder.member, "Member must be provided");
    Assert.isTrue(isNotBlank(builder.encodedChannelId), "Encoded Channel id must be provided");
    Assert.isTrue(isNotBlank(builder.content), "Content must be provided");

    this.member = builder.member;
    this.encodedChannelId = builder.encodedChannelId;
    this.content = builder.content;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Member getMember() {
    return member;
  }

  public String getContent() {
    return content;
  }

  public static class Builder {
    private Member member;
    private String encodedChannelId;
    private String content;

    public Builder member(Member member) {
      this.member = member;
      return this;
    }

    public Builder encodedChannelId(String encodedChannelId) {
      this.encodedChannelId = encodedChannelId;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Message build() {
      return new Message(this);
    }
  }
}
