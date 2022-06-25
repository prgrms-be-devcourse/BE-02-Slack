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

  @Override
  public Member findByEmailAndWorkspaceKey(String encodedWorkspaceId, String email) {
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "id must be provided");
    Assert.isTrue(isNotBlank(email), "email must be provided");

    final var findWorkspace = workspaceService.findByKey(encodedWorkspaceId);

    return memberRepository.findByEmailAndWorkspace(email, findWorkspace)
        .orElseThrow(() -> new NotFoundException("member notfound"));
  }

  @Override
  public boolean isDuplicatedMemberName(String encodedWorkspaceId, String channelName) {
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "id must be provided");
    Assert.isTrue(isNotBlank(channelName), "channelName must be provided");

    final var workspace = workspaceService.findByKey(encodedWorkspaceId);

    return memberRepository.findByNameAndWorkspace(channelName, workspace)
            .isEmpty();
  }
}
