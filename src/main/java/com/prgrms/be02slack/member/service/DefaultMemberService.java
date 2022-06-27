package com.prgrms.be02slack.member.service;

import static org.apache.logging.log4j.util.Strings.*;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.email.service.EmailService;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.repository.MemberRepository;
import com.prgrms.be02slack.security.TokenProvider;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@Service
public class DefaultMemberService implements MemberService {

  private final MemberRepository memberRepository;
  private final WorkspaceService workspaceService;
  private final EmailService emailService;
  private final TokenProvider tokenProvider;

  public DefaultMemberService(
      MemberRepository memberRepository,
      WorkspaceService workspaceService,
      EmailService emailService,
      TokenProvider tokenProvider) {
    this.memberRepository = memberRepository;
    this.workspaceService = workspaceService;
    this.emailService = emailService;
    this.tokenProvider = tokenProvider;
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
  public boolean isDuplicatedWithOtherMemberName(Long decodedWorkspaceId, String channelName) {
    Assert.notNull(decodedWorkspaceId, "decodedWorkspaceId must be provided");
    Assert.isTrue(isNotBlank(channelName), "channelName must be provided");

    return memberRepository.findByNameAndWorkspace_Id(channelName, decodedWorkspaceId).isEmpty();
  }

  @Override
  public AuthResponse verify(VerificationRequest request) {
    Assert.notNull(request, "Request must not be null");

    emailService.verifyCode(request);

    final String email = request.getEmail();
    final Member member = memberRepository.findByEmail(email).orElseGet(() -> createMember(email));

    return new AuthResponse(tokenProvider.createToken(member.getEmail()));
  }

  private Member createMember(String email) {
    final String[] splitEmail = email.split("@");
    final String defaultName = splitEmail[0];
    final Workspace workspace = workspaceService.create();

    final Member member = Member.builder()
        .email(email)
        .name(defaultName)
        .displayName(defaultName)
        .role(Role.OWNER)
        .workspace(workspace)
        .build();

    return memberRepository.save(member);
  }
}
