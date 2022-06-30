package com.prgrms.be02slack.subscribeInfo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.common.exception.NotFoundException;
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
        final var channelSubscribeInfos =
            (List<SubscribeInfo>)ReflectionTestUtils.getField(channel, "subscribeInfos");
        final var memberSubscribeInfos =
            (List<SubscribeInfo>)ReflectionTestUtils.getField(member, "subscribeInfos");

        assert channelSubscribeInfos != null;
        assert memberSubscribeInfos != null;

        assertEquals(channelSubscribeInfos.size(), 1);
        assertEquals(memberSubscribeInfos.size(), 1);
        verify(subscribeInfoRepository).save(any(SubscribeInfo.class));
      }
    }
  }

  @Nested
  @DisplayName("unsubscribe 메서드는")
  class DescribeUnSubscribe {

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
            () -> subscribeInfoService.unsubscribe(null, member));
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
            () -> subscribeInfoService.unsubscribe(channel, null));
      }
    }

    @Nested
    @DisplayName("Channel, Member 값이 모두 전달되고, 해당 구독정보가 존재하면")
    class ContextWithPassAllParameterWithExistSubInfo {

      @Test
      @DisplayName("delete 메서드를 호출한다")
      void ItCallDelete() {
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
        final var subscribeInfo = SubscribeInfo.subscribe(channel, member);
        given(subscribeInfoRepository.findByChannelAndMember(channel, member))
            .willReturn(Optional.of(subscribeInfo));

        //when
        subscribeInfoService.unsubscribe(channel, member);

        //then
        verify(subscribeInfoRepository).delete(any(SubscribeInfo.class));
      }
    }

    @Nested
    @DisplayName("Channel, Member 값이 모두 전달되고, 해당 구독정보가 존재하지 않으면")
    class ContextWithPassAllParameterWithNotExistSubInfo {

      @Test
      @DisplayName("delete 메서드를 호출한다")
      void ItCallDelete() {
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
        final var subscribeInfo = SubscribeInfo.subscribe(channel, member);
        given(subscribeInfoRepository.findByChannelAndMember(channel, member))
            .willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class,
            () -> subscribeInfoService.unsubscribe(channel, member));
      }
    }
  }
}
