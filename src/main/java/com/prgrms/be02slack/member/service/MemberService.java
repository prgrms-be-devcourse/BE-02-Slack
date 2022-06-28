package com.prgrms.be02slack.member.service;

import com.prgrms.be02slack.member.entity.Member;

import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;

public interface MemberService {
  Member findByEmailAndWorkspaceKey(String key, String email);

  boolean isDuplicateName(Long decodedWorkspaceId, String channelName);

  AuthResponse verify(VerificationRequest request);
}
