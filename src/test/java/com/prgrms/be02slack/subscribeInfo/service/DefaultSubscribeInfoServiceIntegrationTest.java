package com.prgrms.be02slack.subscribeInfo.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.repository.MemberRepository;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.repository.WorkspaceRepository;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class DefaultSubscribeInfoServiceIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(
      DefaultSubscribeInfoServiceIntegrationTest.class);

  @Autowired
  private EntityManager em;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private SubscribeInfoService subscribeInfoService;

  private Member testMember;
  private Channel testChannel;

  @BeforeEach
  void setUp() {
    Workspace testWorkspace = Workspace.createDefaultWorkspace();

    workspaceRepository.save(testWorkspace);

    testMember = Member.builder()
        .email("test@naver.com")
        .name("test")
        .displayName("test")
        .role(Role.ROLE_OWNER)
        .workspace(testWorkspace)
        .build();

    memberRepository.save(testMember);

    testChannel = Channel.builder()
        .workspace(testWorkspace)
        .description("test")
        .name("test")
        .isPrivate(true)
        .owner(testMember)
        .build();

    channelRepository.save(testChannel);
  }

  @AfterEach
  void tearDown() {
    channelRepository.deleteAll();
    memberRepository.deleteAll();
    workspaceRepository.deleteAll();
  }

  @Order(1)
  @Test
  public void insertQueryTest() {
    log.info("query start");
    subscribeInfoService.subscribe(testChannel, testMember);
    log.info("query end");
  }

  @Order(2)
  @Test
  void deleteQueryTest() {
    //given
    subscribeInfoService.subscribe(testChannel, testMember);

    //when
    log.info("query start");
    subscribeInfoService.unsubscribe(testChannel, testMember);
    log.info("query end");
  }
}
