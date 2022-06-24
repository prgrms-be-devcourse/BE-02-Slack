package com.prgrms.be02slack.channel.entity;

import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.workspace.entity.Workspace;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Channel extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 1, max = 80)
  private String name;

  @Lob
  private String description;

  @NotNull
  private boolean isPrivate;

  protected Channel() {/*no-op*/}

  public Channel(String name, String description, boolean isPrivate,
      Workspace workspace, Member owner) {
    this.name = name;
    this.description = description;
    this.isPrivate = isPrivate;
    this.workspace = workspace;
    this.owner = owner;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id")
  private Workspace workspace;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member owner;
}
