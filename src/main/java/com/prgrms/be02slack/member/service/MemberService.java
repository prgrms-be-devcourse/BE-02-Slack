package com.prgrms.be02slack.member.service;

import com.prgrms.be02slack.member.entity.Member;

public interface MemberService {

  Member findByEmailAndWorkspaceKey(String key, String email);

  boolean isDuplicatedMemberName(String encodedWorkspaceId, String channelName);
}
