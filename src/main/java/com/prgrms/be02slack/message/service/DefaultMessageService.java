package com.prgrms.be02slack.message.service;

import static org.apache.logging.log4j.util.Strings.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.message.entity.Message;
import com.prgrms.be02slack.message.repository.MessageRepository;

import io.jsonwebtoken.lang.Assert;

@Service
@Transactional
public class DefaultMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final MemberService memberService;

  public DefaultMessageService(
      MessageRepository messageRepository,
      MemberService memberService
  ) {
    this.messageRepository = messageRepository;
    this.memberService = memberService;
  }

  @Override
  public Message sendMessage(
      Member member,
      String encodedWorkspaceId,
      String encodedChannelId,
      String content
  ) {
    Assert.notNull(member, "Member must be provided");
    Assert.isTrue(isNotBlank(encodedChannelId), "Channel must be provided");
    Assert.isTrue(isNotBlank(content), "Content must be provided");

    final var findMember = memberService.findByEmailAndWorkspaceKey(member.getEmail(),
        encodedWorkspaceId);

    final var message = Message.builder()
        .encodedChannelId(encodedChannelId)
        .member(findMember)
        .content(content)
        .build();

    return messageRepository.save(message);
  }
}
