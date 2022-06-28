package com.prgrms.be02slack.member.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;
import com.prgrms.be02slack.common.entity.BaseTime;
import com.prgrms.be02slack.workspace.entity.Workspace;

@Entity
public class Member extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Email
  private String email;

  @NotNull
  @Size(min = 1, max = 80)
  private String name;

  @NotNull
  @Size(min = 1, max = 80)
  private String displayName;

  @Enumerated(EnumType.STRING)
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id")
  private Workspace workspace;

  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<SubscribeInfo> subscribeInfos = new HashSet<>();

  protected Member() {}

  private Member(Builder builder) {
    this.email = builder.email;
    this.name = builder.name;
    this.displayName = builder.displayName;
    this.role = builder.role;
    this.workspace = builder.workspace;
  }

  public String getEmail() {
    return email;
  }

  public String getRoleName() {
    return role.name();
  }

  public static class Builder {

    private String email;
    private String name;
    private String displayName;
    private Role role;
    private Workspace workspace;

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder role(Role role) {
      this.role = role;
      return this;
    }

    public Builder workspace(Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Member build() {
      return new Member(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
