package com.prgrms.be02slack.member.controller.dto;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;

public class MemberResponse {

  private final String id;
  private final String email;
  private final String name;
  private final String displayName;
  private final Role role;

  private MemberResponse(Builder builder) {
    this.id = builder.id;
    this.email = builder.email;
    this.name = builder.name;
    this.displayName = builder.displayName;
    this.role = builder.role;
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Role getRole() {
    return role;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String id;
    private String email;
    private String name;
    private String displayName;
    private Role role;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

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

    public MemberResponse build() {
      return new MemberResponse(this);
    }
  }

  public static MemberResponse from(Member member, String encodedMemberId) {
    return MemberResponse.builder()
        .id(encodedMemberId)
        .email(member.getEmail())
        .name(member.getName())
        .displayName(member.getDisplayName())
        .role(member.getRole())
        .build();
  }
}
