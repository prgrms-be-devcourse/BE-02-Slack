package com.prgrms.be02slack.member.service;

import static org.apache.logging.log4j.util.Strings.*;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.repository.MemberRepository;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@Service
public class DefaultMemberService implements MemberService {

  private final MemberRepository memberRepository;
  private final WorkspaceService workspaceService;

  public DefaultMemberService(
      MemberRepository memberRepository,
      WorkspaceService workspaceService
  ) {
    this.memberRepository = memberRepository;
    this.workspaceService = workspaceService;
  }

  public Member findByEmailAndWorkspaceKey(String key, String email) {
    Assert.isTrue(isNotBlank(key), "Key must be provided");
    Assert.isTrue(isNotBlank(email), "Workspace must be provided");

    final var findWorkspace = workspaceService.findByKey(key);

    return memberRepository.findByEmailAndWorkspace(email, findWorkspace)
        .orElseThrow(() -> new NotFoundException("member notfound"));
  }
}
