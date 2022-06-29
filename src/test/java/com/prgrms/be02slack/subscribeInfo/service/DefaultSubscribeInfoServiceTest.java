package com.prgrms.be02slack.subscribeInfo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;
import com.prgrms.be02slack.subscribeInfo.repository.SubscribeInfoRepository;
import com.prgrms.be02slack.workspace.entity.Workspace;

@ExtendWith(MockitoExtension.class)
class DefaultSubscribeInfoServiceTest {

  @Mock
  private SubscribeInfoRepository subscribeInfoRepository;

  @InjectMocks
  private DefaultSubscribeInfoService subscribeInfoService;

  @Nested
  @DisplayName("Subscribe 메서드는")
  class DescribeSubscribe {

    @Nested
    @DisplayName("Channel 값이 null 이면")
    class ContextWithChannelNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        final var workspace = Workspace.createDefaultWorkspace();
        final var member = Member.builder()
            .email("test@naver.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .workspace(workspace)
            .build();

        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> subscribeInfoService.subscribe(null, member));
      }
    }

    @Nested
    @DisplayName("Member 값이 null 이면")
    class ContextWithMemberNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        final var workspace = Workspace.createDefaultWorkspace();
        final var owner = Member.builder()
            .email("test@naver.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_OWNER)
            .workspace(workspace)
            .build();

        final var channel = Channel.builder()
            .workspace(workspace)
            .description("test")
            .name("test")
            .isPrivate(true)
            .owner(owner)
            .build();

        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> subscribeInfoService.subscribe(channel, null));
      }
    }

    @Nested
    @DisplayName("모든 값이 전달되면")
    class ContextWithNotNull {

      @Test
      @DisplayName("SubscribeInfoRepository 의 save 메서드를 호출한다")
      void ItCallSaveAtSubscribeInfoRepository() {
        //given
        final var workspace = Workspace.createDefaultWorkspace();
        final var member = Member.builder()
            .email("test@naver.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_OWNER)
            .workspace(workspace)
            .build();

        final var channel = Channel.builder()
            .workspace(workspace)
            .description("test")
            .name("test")
            .isPrivate(true)
            .owner(member)
            .build();

        //when
        subscribeInfoService.subscribe(channel, member);

        //then
        assertEquals(channel.getSubscribeInfos().size(), 1);
        assertEquals(member.getSubscribeInfos().size(), 1);
        verify(subscribeInfoRepository).save(any(SubscribeInfo.class));
      }
    }
  }
}
