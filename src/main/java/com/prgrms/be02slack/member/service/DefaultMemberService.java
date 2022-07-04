package com.prgrms.be02slack.member.service;

import static org.apache.logging.log4j.util.Strings.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.email.service.EmailService;
import com.prgrms.be02slack.member.controller.dto.MemberResponse;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.repository.MemberRepository;
import com.prgrms.be02slack.security.TokenProvider;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@Service
@Transactional
public class DefaultMemberService implements MemberService {

  private final MemberRepository memberRepository;
  private final WorkspaceService workspaceService;
  private final EmailService emailService;
  private final TokenProvider tokenProvider;
  private final IdEncoder idEncoder;

  public DefaultMemberService(
      MemberRepository memberRepository,
      WorkspaceService workspaceService,
      EmailService emailService,
      TokenProvider tokenProvider,
      IdEncoder idEncoder) {
    this.memberRepository = memberRepository;
    this.workspaceService = workspaceService;
    this.emailService = emailService;
    this.tokenProvider = tokenProvider;
    this.idEncoder = idEncoder;
  }

  @Override
  public Member findByEmailAndWorkspaceKey(String email, String encodedWorkspaceId) {
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "id must be provided");
    Assert.isTrue(isNotBlank(email), "email must be provided");

    final var findWorkspace = workspaceService.findByKey(encodedWorkspaceId);

    return memberRepository.findByEmailAndWorkspace(email, findWorkspace)
        .orElseThrow(() -> new NotFoundException("member notfound"));
  }

  @Override
  public Member findByNameAndWorkspaceKey(String name, String encodedWorkspaceId) {
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "id must be provided");
    Assert.isTrue(isNotBlank(name), "name must be provided");

    Long decodedWorkspaceId = idEncoder.decode(encodedWorkspaceId);
    return memberRepository.findByNameAndWorkspace_Id(name, decodedWorkspaceId)
        .orElseThrow(() -> new NotFoundException("member notfound"));
  }

  @Override
  public boolean isDuplicateName(Long decodedWorkspaceId, String channelName) {
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

    return new AuthResponse(tokenProvider.createLoginToken(member.getEmail()));
  }

  @Override
  public AuthResponse enterWorkspace(String email, String encodedWorkspaceId) {
    Assert.isTrue(isNotBlank(email), "email must be provided");
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "encodedWorkspaceId must be provided");

    final Member member = findByEmailAndWorkspaceKey(email, encodedWorkspaceId);

    return new AuthResponse(tokenProvider.createMemberToken(member.getEmail(), encodedWorkspaceId));
  }

  public boolean isExistsByNameAndWorkspaceKey(String name, String encodedWorkspaceId) {
    Assert.isTrue(isNotBlank(name), "Name must be provided");
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "Id must be provided");

    long decodedWorkspaceId = idEncoder.decode(encodedWorkspaceId);
    return memberRepository.existsByNameAndWorkspace_Id(name, decodedWorkspaceId);
  }

  @Override
  public boolean isExistsByEmailAndWorkspaceKey(String email, String encodedWorkspaceId) {
    Assert.isTrue(isNotBlank(email), "Email must be provided");
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "Id must be provided");

    long decodedWorkspaceId = idEncoder.decode(encodedWorkspaceId);
    return memberRepository.existsByEmailAndWorkspace_Id(email, decodedWorkspaceId);
  }

  @Override
  public Member save(String name, String email, Role role, String workspaceId, String displayName) {
    Assert.isTrue(isNotBlank(name), "Name must be provided");
    Assert.isTrue(isNotBlank(email), "Email must be provided");
    Assert.notNull(role, "Role must not be null");
    Assert.isTrue(isNotBlank(workspaceId), "WorkspaceId must be provided");
    Assert.isTrue(isNotBlank(displayName), "DisplayName must be provided");

    Workspace workspace = workspaceService.findByKey(workspaceId);
    Member member = Member.builder()
        .name(name)
        .email(email)
        .role(role)
        .workspace(workspace)
        .displayName(displayName)
        .build();

    return memberRepository.save(member);
  }

  @Override
  public MemberResponse getOne(Member member, String encodedMemberId) {
    Assert.notNull(member, "Member must be provided");
    Assert.isTrue(isNotBlank(encodedMemberId), "EncodedMemberId must be provided");

    final long memberId = idEncoder.decode(encodedMemberId);
    final Member foundMember = memberRepository
        .findByIdAndWorkspace_id(memberId, member.getWorkspace().getId())
        .orElseThrow(() -> new NotFoundException("Member not found"));

    return MemberResponse.from(foundMember, encodedMemberId);
  }

  @Override
  public List<MemberResponse> getAllFromChannel(Member member, String encodedChannelId) {
    return null;
  }

  private Member createMember(String email) {
    final String[] splitEmail = email.split("@");
    final String defaultName = splitEmail[0];
    final Workspace workspace = workspaceService.create();

    final Member member = Member.builder()
        .email(email)
        .name(defaultName)
        .displayName(defaultName)
        .role(Role.ROLE_OWNER)
        .workspace(workspace)
        .build();

    return memberRepository.save(member);
  }
}
