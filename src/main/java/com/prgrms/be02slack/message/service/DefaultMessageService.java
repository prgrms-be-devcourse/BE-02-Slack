package com.prgrms.be02slack.message.service;

import static org.apache.logging.log4j.util.Strings.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.be02slack.channel.service.ChannelService;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.message.entity.Message;
import com.prgrms.be02slack.message.repository.MessageRepository;
import com.prgrms.be02slack.subscribeInfo.service.SubscribeInfoService;

import io.jsonwebtoken.lang.Assert;

@Service
@Transactional
public class DefaultMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final MemberService memberService;
  private final ChannelService channelService;
  private final SubscribeInfoService subscribeInfoService;

  public DefaultMessageService(
      MessageRepository messageRepository,
      MemberService memberService,
      ChannelService channelService,
      SubscribeInfoService subscribeInfoService
  ) {
    this.messageRepository = messageRepository;
    this.memberService = memberService;
    this.channelService = channelService;
    this.subscribeInfoService = subscribeInfoService;
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

    final var channel = channelService.findByKey(encodedChannelId);

    if (!subscribeInfoService.isExistsByChannelAndMemberEmail(channel, member.getEmail())) {
      throw new IllegalArgumentException("Not a member exist on the channel");
    }

    final var message = Message.builder()
        .encodedChannelId(encodedChannelId)
        .member(findMember)
        .content(content)
        .build();

    return messageRepository.save(message);
  }
}
