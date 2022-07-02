package com.prgrms.be02slack.directmessagechannel.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.workspace.entity.Workspace;

@Entity
public class DirectMessageChannel extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member firstMember;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member secondMember;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "리workspace_id")
  private Workspace workspace;

  protected DirectMessageChannel() {
  }

  public DirectMessageChannel(Member firstMember, Member secondMember, Workspace workspace) {
    this.firstMember = firstMember;
    this.secondMember = secondMember;
    this.workspace = workspace;
  }

  public Long getId() {
    return id;
  }
}