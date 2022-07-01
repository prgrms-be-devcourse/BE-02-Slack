package com.prgrms.be02slack.channel.service;

import static com.prgrms.be02slack.channel.exception.ErrorMessage.*;
import static com.prgrms.be02slack.common.exception.ErrorMessage.*;
import static org.apache.logging.log4j.util.Strings.*;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.controller.dto.InviteRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.exception.NameDuplicateException;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.email.controller.dto.EmailRequest;
import com.prgrms.be02slack.email.service.EmailService;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.security.TokenProvider;
import com.prgrms.be02slack.subscribeInfo.service.SubscribeInfoService;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@Service
public class DefaultChannelService implements ChannelService {
  private final ChannelRepository channelRepository;
  private final WorkspaceService workspaceService;
  private final MemberService memberService;
  private final IdEncoder idEncoder;
  private final TokenProvider tokenProvider;
  private final SubscribeInfoService subscribeInfoService;
  private final EmailService emailService;

  public DefaultChannelService(
      ChannelRepository channelRepository,
      WorkspaceService workspaceService,
      MemberService memberService, IdEncoder idEncoder,
      TokenProvider tokenProvider,
      SubscribeInfoService subscribeInfoService,
      EmailService emailService) {
    this.channelRepository = channelRepository;
    this.workspaceService = workspaceService;
    this.memberService = memberService;
    this.idEncoder = idEncoder;
    this.tokenProvider = tokenProvider;
    this.subscribeInfoService = subscribeInfoService;
    this.emailService = emailService;
  }

  /**
   * 1. 멤버(소유주) 정보 파라미터로 넘어오면 이후 처리 구현 필요
   * 2. 테스트 수정 필요
   */
  @Override
  public String create(String workspaceId, ChannelSaveRequest channelSaveRequest) {
    Assert.isTrue(isNotBlank(workspaceId), "WorkspaceId must be provided");
    Assert.notNull(channelSaveRequest, "ChannelSaveRequest must be provided");

    Workspace workspace = workspaceService.findByKey(workspaceId);

    long decodedWorkspaceId = idEncoder.decode(workspaceId);
    validateName(decodedWorkspaceId, channelSaveRequest.getName());

    // 멤버 조회 로직 구현 필요

    Channel channel = Channel.builder()
        .name(channelSaveRequest.getName())
        .description(channelSaveRequest.getDescription())
        .isPrivate(channelSaveRequest.isPrivate())
        .workspace(workspace)
        .owner(null) // <- 이 부분 수정 필요
        .build();
    Channel savedChannel = channelRepository.save(channel);

    return idEncoder.encode(savedChannel.getId());
  }

  @Override
  public void invite(String workspaceId, String channelId, InviteRequest inviteRequest)
      throws MessagingException {
    Assert.isTrue(isNotBlank(workspaceId), "WorkspaceId must be provided");
    Assert.isTrue(isNotBlank(channelId), "ChannelId must be provided");
    Assert.notNull(inviteRequest, "InviteRequest must be provided");

    Channel channel = findByKey(channelId);
    Workspace workspace = workspaceService.findByKey(workspaceId);

    Set<String> inviteeInfos = inviteRequest.getInviteeInfos();
    for (String inviteeInfo : inviteeInfos) {
      boolean isInviteeInfoEmail = false;
      if (isValidEmail(inviteeInfo)) {
        isInviteeInfoEmail = true;
        if (!isMemberOfWorkspace(inviteeInfo, workspaceId, isInviteeInfoEmail)) {
          sendInviteEmail(inviteeInfo, workspaceId, workspace.getName(),
              channelId, inviteRequest.getSender());
          continue;
        }
        if (isSubscriber(channel, inviteeInfo, isInviteeInfoEmail)) {
          throw new IllegalArgumentException(ALREADY_SUBSCRIBER.getMsg());
        }

        Member member = findMemberByWorkspaceAndEmailOrName(workspaceId, inviteeInfo,
            isInviteeInfoEmail);
        subscribeInfoService.subscribe(channel, member);
        continue;
      }
      if (!isMemberOfWorkspace(inviteeInfo, workspaceId, isInviteeInfoEmail)) {
        throw new IllegalArgumentException(NOT_WORKSPACE_MEMBER.getMsg());
      }
      if (isSubscriber(channel, inviteeInfo, isInviteeInfoEmail)) {
        throw new IllegalArgumentException(ALREADY_SUBSCRIBER.getMsg());
      }
      Member member = findMemberByWorkspaceAndEmailOrName(workspaceId, inviteeInfo,
          isInviteeInfoEmail);
      subscribeInfoService.subscribe(channel, member);
    }
  }

  @Override
  public AuthResponse participate(String workspaceId, String channelId, String token) {
    Assert.isTrue(isNotBlank(workspaceId), "WorkspaceId must be provided");
    Assert.isTrue(isNotBlank(channelId), "ChannelId must be provided");
    Assert.isTrue(isNotBlank(token), "Token must be provided");

    workspaceService.findByKey(workspaceId);
    Channel channel = findByKey(channelId);

    if (!tokenProvider.validateToken(token)) {
      throw new IllegalArgumentException(INVALID_TOKEN.getMsg());
    }

    String email = tokenProvider.getEmailFromToken(token);
    String name = email.substring(0, email.indexOf("@"));

    Member member = memberService.save(name, email, Role.ROLE_USER, workspaceId, name);

    subscribeInfoService.subscribe(channel, member);

    return new AuthResponse(tokenProvider.createMemberToken(email, workspaceId));
  }

  @Override
  public Channel findByKey(String key) {
    Assert.isTrue(isNotBlank(key), "EncodedChannelId must be provided");

    Long decodedChannelId = idEncoder.decode(key);
    return channelRepository.findById(decodedChannelId)
        .orElseThrow(() -> new NotFoundException("channel notfound"));
  }

  private boolean isValidEmail(String email) {
    boolean err = false;
    String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(email);
    if (m.matches()) {
      err = true;
    }
    return err;
  }

  private boolean isSubscriber(Channel channel, String inviteeInfo, boolean isInviteeInfoEmail) {
    if (isInviteeInfoEmail) {
      return subscribeInfoService.isExistsByChannelAndMemberEmail(channel, inviteeInfo);
    }
    return subscribeInfoService.isExistsByChannelAndMemberName(channel, inviteeInfo);
  }

  private boolean isMemberOfWorkspace(String inviteeInfo, String workspaceId,
      boolean inviteeInfoIsEmail) {
    if (inviteeInfoIsEmail) {
      return memberService.isExistsByEmailAndWorkspaceKey(inviteeInfo, workspaceId);
    }
    return memberService.isExistsByNameAndWorkspaceKey(inviteeInfo, workspaceId);
  }

  private void sendInviteEmail(
      String email, String workspaceId, String workspaceName,
      String channelId, String sender) throws MessagingException {
    String loginToken = tokenProvider.createLoginToken(email);

    emailService.sendInviteMail(new EmailRequest(email), loginToken, workspaceId,
        channelId, workspaceName, sender);
  }

  private Member findMemberByWorkspaceAndEmailOrName(String workspaceId, String inviteeInfo,
      boolean inviteeInfoIsEmail) {
    if (inviteeInfoIsEmail) {
      return memberService.findByEmailAndWorkspaceKey(inviteeInfo, workspaceId);
    }
    return memberService.findByNameAndWorkspaceKey(inviteeInfo, workspaceId);
  }

  private void validateName(long decodedWorkspaceId, String name) {
    if (isDuplicateName(decodedWorkspaceId, name)) {
      throw new NameDuplicateException(
          "Name is duplicate with the name of another channel in the same workspace");
    }
    if (memberService.isDuplicateName(decodedWorkspaceId, name)) {
      throw new NameDuplicateException(
          "Name is duplicate with the name of another member in the same workspace");
    }
  }

  private boolean isDuplicateName(Long decodedWorkspaceId, String name) {
    return channelRepository.existsByWorkspace_IdAndName(decodedWorkspaceId, name);
  }
}
