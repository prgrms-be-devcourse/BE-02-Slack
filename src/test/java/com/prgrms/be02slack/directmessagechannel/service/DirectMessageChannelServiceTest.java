package com.prgrms.be02slack.directmessagechannel.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.directmessagechannel.repository.DirectMessageChannelRepository;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.util.WithMockCustomLoginUser;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DirectMessageChannelServiceTest {

  @Mock
  private DirectMessageChannelRepository directMessageChannelRepository;

  @Mock
  private WorkspaceService workspaceService;

  @Mock
  private MemberService memberService;

  @Mock
  private IdEncoder idEncoder;

  @Mock
  private SecurityContext securityContextMocked;

  @Mock
  private Authentication authenticationMocked;

  @InjectMocks
  DefaultDirectMessageChannelService directMessageChannelService;

  @Nested
  @DisplayName("create 메서드는")
  @WithMockCustomLoginUser
  class DescribeCreate {

    final Member testMember = Member.builder()
        .email("test@test.test")
        .name("test")
        .build();

    @Nested
    @DisplayName("빈 값의 workspaceId를 인자로 받으면")
    class ContextNullAndEmptyWorkspaceId {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 반환한다.")
      void itThrowIllegalArgumentException(String workspaceId) {
        //given
        final String validReceiverEmail = "test@test.com";

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(workspaceId, validReceiverEmail))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("빈 값의 receiverEmail을 인자로 받으면")
    class ContextNullAndEmptyReceiverEmail {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 반환한다.")
      void itThrowIllegalArgumentException(String receiverEmail) {
        //given
        final String validWorkspaceId = "testId";

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(validWorkspaceId, receiverEmail))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("workspace가 존재하지 않는 workspaceId이라면")
    class ContextNotExistWorkspaceId {

      @Test
      @DisplayName("NotfoundException을 반환한다.")
      void itThrowNotFoundException() {
        //given
        final String validWorkspaceId = "testId";
        final String validReceiverEmail = "test@test.test";
        when(workspaceService.findByKey(anyString())).thenThrow(NotFoundException.class);

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(validWorkspaceId, validReceiverEmail))
            .isInstanceOf(NotFoundException.class);
      }
    }

    @Nested
    @DisplayName("receiverEmail이 존재하지 않는 다면")
    class ContextNotExistEmail {

      @Test
      @DisplayName("NotfoundException을 반환한다.")
      void itThrowNotFoundException() {
        //given
        final String validWorkspaceId = "testId";
        final String notExistReceiverEmail = "test@test.test";
        final Workspace workspace = new Workspace("test", "test");

        when(workspaceService.findByKey(any())).thenReturn(workspace);

        when(authenticationMocked.getName()).thenReturn("test");
        when(securityContextMocked.getAuthentication()).thenReturn(authenticationMocked);
        SecurityContextHolder.setContext(securityContextMocked);

        when(memberService.findByEmailAndWorkspaceKey(any(), any()))
            .thenThrow(NotFoundException.class);

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(validWorkspaceId, notExistReceiverEmail))
            .isInstanceOf(NotFoundException.class);
      }
    }

    @Nested
    @DisplayName("유효한 인자를 받았을때 해당 dm 채널이 존재하면")
    class ContextValidArgumentAndeExistChannel {

      @Test
      @DisplayName("해당 채널 Id를 반환한다.")
      void itReturnDirectMessageChannelId() {

        //given
        final String validWorkspaceId = "testId";
        final String validReceiverEmail = "test@test.test";
        final Workspace workspace =
            new Workspace(
                "test",
                "test"
            );
        final DirectMessageChannel testDirectMessageChannel =
            new DirectMessageChannel(
                testMember,
                testMember,
                workspace
            );

        ReflectionTestUtils.setField(testDirectMessageChannel, "id", 1L);

        when(workspaceService.findByKey(any())).thenReturn(workspace);

        when(authenticationMocked.getName()).thenReturn("test");
        when(securityContextMocked.getAuthentication()).thenReturn(authenticationMocked);
        SecurityContextHolder.setContext(securityContextMocked);

        when(memberService.findByEmailAndWorkspaceKey(any(), any()))
            .thenReturn(testMember);

        when(directMessageChannelRepository
            .findByFirstMemberAndSecondMember(testMember, testMember))
            .thenReturn(Optional.of(testDirectMessageChannel));

        when(idEncoder.encode(testDirectMessageChannel.getId())).thenReturn("testtest");

        //when
        final String actualId =
            directMessageChannelService.create(validWorkspaceId, validReceiverEmail);

        final String expectedId = "testtest";

        //then
        Assertions.assertThat(expectedId).isEqualTo(actualId);

        verify(workspaceService).findByKey(any());
        verify(memberService, times(2))
            .findByEmailAndWorkspaceKey(any(), any());
        verify(directMessageChannelRepository, times(1))
            .findByFirstMemberAndSecondMember(any(), any());
        verify(idEncoder).encode(anyLong());
      }
    }

    @Nested
    @DisplayName("유효한 인자를 받았을때 해당 dm 채널이 존재하지 않으면")
    class ContextValidArgumentAndNotExistChannel {

      @Test
      @DisplayName("새로운 채널을 생성하고 Id를 반환한다.")
      void itReturnDirectMessageChannelId() {

        //given
        final String validWorkspaceId = "testId";
        final String validReceiverEmail = "test@test.test";
        final Workspace workspace =
            new Workspace(
                "test",
                "test"
            );

        when(workspaceService.findByKey(any())).thenReturn(workspace);

        when(authenticationMocked.getName()).thenReturn("test");
        when(securityContextMocked.getAuthentication()).thenReturn(authenticationMocked);
        SecurityContextHolder.setContext(securityContextMocked);

        when(memberService.findByEmailAndWorkspaceKey(any(), any()))
            .thenReturn(testMember);

        when(directMessageChannelRepository
            .findByFirstMemberAndSecondMember(testMember, testMember))
            .thenReturn(Optional.empty());

        MockedConstruction<DirectMessageChannel> mockedConstruction2 =
            Mockito.mockConstruction(DirectMessageChannel.class,
                (mock, context) -> when(mock.getId()).thenReturn(1L));

        when(idEncoder.encode(anyLong())).thenReturn("testtest");

        //when
        final String actualId =
            directMessageChannelService.create(validWorkspaceId, validReceiverEmail);

        final String expectedId = "testtest";

        //then
        Assertions.assertThat(expectedId).isEqualTo(actualId);

        verify(workspaceService).findByKey(any());
        verify(memberService, times(2))
            .findByEmailAndWorkspaceKey(any(), any());
        verify(directMessageChannelRepository, times(2))
            .findByFirstMemberAndSecondMember(any(), any());
        verify(idEncoder).encode(anyLong());
      }
    }
  }
}
