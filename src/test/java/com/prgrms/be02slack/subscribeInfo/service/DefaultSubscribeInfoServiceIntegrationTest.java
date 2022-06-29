package com.prgrms.be02slack.subscribeInfo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.repository.MemberRepository;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.repository.WorkspaceRepository;

@SpringBootTest
public class DefaultSubscribeInfoServiceIntegrationTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private SubscribeInfoService subscribeInfoService;

  @Test
  public void queryTest() {
    final var workspace = Workspace.createDefaultWorkspace();
    workspaceRepository.save(workspace);

    final var member = Member.builder()
        .email("test@naver.com")
        .name("test")
        .displayName("test")
        .role(Role.ROLE_OWNER)
        .workspace(workspace)
        .build();

    memberRepository.save(member);

    final var channel = Channel.builder()
        .workspace(workspace)
        .description("test")
        .name("test")
        .isPrivate(true)
        .owner(member)
        .build();

    channelRepository.save(channel);

    //when
    subscribeInfoService.subscribe(channel, member);
  }
}
