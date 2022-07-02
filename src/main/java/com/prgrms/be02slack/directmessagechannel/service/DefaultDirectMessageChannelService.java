package com.prgrms.be02slack.directmessagechannel.service;

import static org.apache.logging.log4j.util.Strings.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.directmessagechannel.repository.DirectMessageChannelRepository;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@Service
@Transactional
public class DefaultDirectMessageChannelService implements DirectMessageChannelService {

  private final DirectMessageChannelRepository directMessageChannelRepository;
  private final WorkspaceService workspaceService;
  private final MemberService memberService;
  private final IdEncoder idEncoder;

  public DefaultDirectMessageChannelService(
      DirectMessageChannelRepository directMessageChannelRepository,
      WorkspaceService workspaceService,
      MemberService memberService,
      IdEncoder idEncoder) {
    this.directMessageChannelRepository = directMessageChannelRepository;
    this.workspaceService = workspaceService;
    this.memberService = memberService;
    this.idEncoder = idEncoder;
  }

  @Override
  public String create(String encodedWorkspaceId, String receiverEmail) {
    Assert.isTrue(isNotBlank(encodedWorkspaceId), "EncodedWorkspaceId must be provided");
    Assert.isTrue(isNotBlank(receiverEmail), "EncodedReceiverId must be provided");

    final Workspace workspace = workspaceService.findByKey(encodedWorkspaceId);
    final String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    final Member sender =
        memberService.findByEmailAndWorkspaceKey(senderEmail, encodedWorkspaceId);
    final Member receiver =
        memberService.findByEmailAndWorkspaceKey(receiverEmail, encodedWorkspaceId);

    final DirectMessageChannel directMessageChannel =
        directMessageChannelRepository.findByFirstMemberAndSecondMember(sender, receiver)
            .orElseGet(() -> directMessageChannelRepository
                .findByFirstMemberAndSecondMember(receiver, sender)
                .orElseGet(() -> new DirectMessageChannel(sender, receiver, workspace)
                )
            );

    return idEncoder.encode(directMessageChannel.getId());
  }
}
