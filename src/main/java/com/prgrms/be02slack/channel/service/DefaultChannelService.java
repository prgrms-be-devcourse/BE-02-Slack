package com.prgrms.be02slack.channel.service;

import static com.prgrms.be02slack.channel.exception.ErrorMessage.*;
import static com.prgrms.be02slack.common.exception.ErrorMessage.*;
import static org.apache.logging.log4j.util.Strings.*;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.be02slack.channel.controller.dto.ChannelResponse;
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
@Transactional
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
      MemberService memberService,
      IdEncoder idEncoder,
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

  @Override
  public String create(
      Member member,
      String workspaceId,
      ChannelSaveRequest channelSaveRequest) {
    Assert.notNull(member, "Member must be provided");
    Assert.isTrue(isNotBlank(workspaceId), "WorkspaceId must be provided");
    Assert.notNull(channelSaveRequest, "ChannelSaveRequest must be provided");

    Workspace workspace = workspaceService.findByKey(workspaceId);

    long decodedWorkspaceId = idEncoder.decode(workspaceId);
    validateName(decodedWorkspaceId, channelSaveRequest.getName());

    Channel channel = Channel.builder()
        .name(channelSaveRequest.getName())
        .description(channelSaveRequest.getDescription())
        .isPrivate(channelSaveRequest.isPrivate())
        .workspace(workspace)
        .owner(member)
        .build();
    Channel savedChannel = channelRepository.save(channel);
    subscribeInfoService.subscribe(channel, member);

    return idEncoder.encode(savedChannel.getId(), savedChannel.getType());
  }

  @Override
  public void invite(String workspaceId,
      String channelId,
      InviteRequest inviteRequest)
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
  public AuthResponse participate(String workspaceId,
      String channelId,
      String token) {
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

  @Override
  public List<ChannelResponse> findAllByMember(Member member) {
    Assert.notNull(member, "Member must be provided");

    return subscribeInfoService.findAllByMember(member)
        .stream()
        .map((subscribeInfo -> subscribeInfo.getChannel()))
        .map((c) -> new ChannelResponse(idEncoder.encode(c.getId(), c.getType()), c.getName(), c.isPrivate()))
        .collect(Collectors.toList());
  }

  @Override
  public void leave(String encodedChannelId, Member member) {
    Assert.isTrue(isNotBlank(encodedChannelId), "EncodedChannelId must be provided");
    Assert.notNull(member, "Member must be provided");

    Channel channel = findByKey(encodedChannelId);
    subscribeInfoService.unsubscribe(channel, member);
  }

  @Override
  public void inviteMember(Member sender, String encodedWorkspaceId, InviteRequest inviteRequest)
      throws MessagingException {
    Assert.notNull(sender, "Member must be provided");
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "encodedWorkspaceId must be provided");
    Assert.notNull(inviteRequest, "RecipientE  mail must be provided");

    final var workspaceId = idEncoder.decode(encodedWorkspaceId);
    final var memberWorkspaceId = sender.getWorkspace().getId();

    Assert.isTrue(workspaceId == memberWorkspaceId,
                  "Only workspace member create workspace invitation");

    final var channelId = getDefaultChannelIdByMember(sender);

    invite(encodedWorkspaceId, channelId, inviteRequest);
  }

  private String getDefaultChannelIdByMember(Member sender) {
    return findAllByMember(sender).get(0).getId();
  }

  private boolean isValidEmail(String email) {
    boolean err = false;
    String emailRegex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
    Pattern p = Pattern.compile(emailRegex);
    Matcher m = p.matcher(email);
    if (m.matches()) {
      err = true;
    }
    return err;
  }

  private boolean isSubscriber(Channel channel,
      String inviteeInfo,
      boolean isInviteeInfoEmail) {
    if (isInviteeInfoEmail) {
      return subscribeInfoService.isExistsByChannelAndMemberEmail(channel, inviteeInfo);
    }
    return subscribeInfoService.isExistsByChannelAndMemberName(channel, inviteeInfo);
  }

  private boolean isMemberOfWorkspace(String inviteeInfo,
      String workspaceId,
      boolean inviteeInfoIsEmail) {
    if (inviteeInfoIsEmail) {
      return memberService.isExistsByEmailAndWorkspaceKey(inviteeInfo, workspaceId);
    }
    return memberService.isExistsByNameAndWorkspaceKey(inviteeInfo, workspaceId);
  }

  private void sendInviteEmail(
      String email,
      String workspaceId,
      String workspaceName,
      String channelId,
      String sender) throws MessagingException {
    String loginToken = tokenProvider.createLoginToken(email);

    emailService.sendInviteMail(new EmailRequest(email), loginToken, workspaceId,
        channelId, workspaceName, sender);
  }

  private Member findMemberByWorkspaceAndEmailOrName(String workspaceId,
      String inviteeInfo,
      boolean inviteeInfoIsEmail) {
    if (inviteeInfoIsEmail) {
      return memberService.findByEmailAndWorkspaceKey(inviteeInfo, workspaceId);
    }
    return memberService.findByNameAndWorkspaceKey(inviteeInfo, workspaceId);
  }

  private void validateName(long decodedWorkspaceId,
      String name) {
    if (isDuplicateName(decodedWorkspaceId, name)) {
      throw new NameDuplicateException(
          "Name is duplicate with the name of another channel in the same workspace");
    }
    if (memberService.isDuplicateName(decodedWorkspaceId, name)) {
      throw new NameDuplicateException(
          "Name is duplicate with the name of another member in the same workspace");
    }
  }

  private boolean isDuplicateName(Long decodedWorkspaceId,
      String name) {
    return channelRepository.existsByWorkspace_IdAndName(decodedWorkspaceId, name);
  }
}
