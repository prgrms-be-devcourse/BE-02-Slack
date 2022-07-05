package com.prgrms.be02slack.directmessagechannel.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.util.Assert;

import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.common.enums.EntityIdType;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.workspace.entity.Workspace;

@Entity
public class DirectMessageChannel extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id1")
  private Member firstMember;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id2")
  private Member secondMember;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id")
  private Workspace workspace;

  @Transient
  private EntityIdType type = EntityIdType.DMCHANNEL;

  protected DirectMessageChannel() {/*no-op*/}

  public DirectMessageChannel(Member firstMember, Member secondMember, Workspace workspace) {
    Assert.notNull(firstMember,  "firstMember must be provided");
    Assert.notNull(secondMember,  "secondMember must be provided");
    Assert.notNull(workspace,  "workspace must be provided");

    this.firstMember = firstMember;
    this.secondMember = secondMember;
    this.workspace = workspace;
  }

  public Long getId() {
    return id;
  }

  public Member getFirstMember() {
    return firstMember;
  }

  public Member getSecondMember() {
    return secondMember;
  }

  public EntityIdType getType() {
    return type;
  }
}
