package com.prgrms.be02slack.member.service;

import com.prgrms.be02slack.member.controller.dto.MemberResponse;
import com.prgrms.be02slack.member.entity.Member;

import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Role;

public interface MemberService {
  Member findByEmailAndWorkspaceKey(String key, String email);

  Member findByNameAndWorkspaceKey(String name, String key);

  boolean isDuplicateName(Long decodedWorkspaceId, String channelName);

  AuthResponse verify(VerificationRequest request);

  AuthResponse enterWorkspace(String email, String encodedWorkspaceId);

  boolean isExistsByEmailAndWorkspaceKey(String email, String key);

  boolean isExistsByNameAndWorkspaceKey(String name, String key);

  Member save(String name, String email, Role role, String workspaceKey, String displayName);

  MemberResponse getOne(Member member, String encodedMemberId);
}
