package com.prgrms.be02slack.channel.entity;

import java.util.ArrayList;
import java.util.List;

import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.common.enums.EntityIdType;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;
import com.prgrms.be02slack.workspace.entity.Workspace;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
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

  @Transient
  private EntityIdType type = EntityIdType.CHANNEL;

  protected Channel() {/*no-op*/}

  private Channel(Builder builder) {
    this.name = builder.name;
    this.description = builder.description;
    this.isPrivate = builder.isPrivate;
    this.workspace = builder.workspace;
    this.owner = builder.owner;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id")
  private Workspace workspace;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member owner;

  @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<SubscribeInfo> subscribeInfos = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public void addSubscribeInfo(SubscribeInfo subscribeInfo) {
    subscribeInfos.add(subscribeInfo);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String description;
    private boolean isPrivate;
    private Workspace workspace;
    private Member owner;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder isPrivate(boolean isPrivate) {
      this.isPrivate = isPrivate;
      return this;
    }

    public Builder workspace(Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder owner(Member owner) {
      this.owner = owner;
      return this;
    }

    public Channel build() {
      return new Channel(this);
    }
  }

  public EntityIdType getType() {
    return type;
  }
}
