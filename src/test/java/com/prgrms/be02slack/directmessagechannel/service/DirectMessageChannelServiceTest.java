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
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.common.enums.EntityIdType;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.directmessagechannel.controller.dto.DirectMessageChannelResponse;
import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.directmessagechannel.repository.DirectMessageChannelRepository;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.util.WithMockCustomLoginUser;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
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
      @ValueSource(strings = {"\t", "\n", " "})
      @DisplayName("IllegalArgumentException을 반환한다.")
      void itThrowIllegalArgumentException(String workspaceId) {
        //given
        final String validReceiverEmail = "test@test.com";
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .workspace(workspace)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(workspaceId, validReceiverEmail, member))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("빈 값의 receiverEmail을 인자로 받으면")
    class ContextNullAndEmptyReceiverEmail {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n", " "})
      @DisplayName("IllegalArgumentException을 반환한다.")
      void itThrowIllegalArgumentException(String receiverEmail) {
        //given
        final String validWorkspaceId = "testId";
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .workspace(workspace)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(validWorkspaceId, receiverEmail, member))
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
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .workspace(workspace)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        when(workspaceService.findByKey(anyString())).thenThrow(NotFoundException.class);

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(
                    validWorkspaceId,
                    validReceiverEmail,
                    member))
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
        final Member member =
            Member.builder()
                .name("test")
                .email("hey")
                .build();

        when(workspaceService.findByKey(any())).thenReturn(workspace);

        when(memberService.findByEmailAndWorkspaceKey(any(), any()))
            .thenThrow(NotFoundException.class);

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.create(validWorkspaceId,
                    notExistReceiverEmail,
                    member))
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
        final Member member =
            Member.builder()
                .name("test")
                .email("hey")
                .build();

        when(workspaceService.findByKey(any())).thenReturn(workspace);

        ReflectionTestUtils.setField(testDirectMessageChannel, "id", 1L);

        when(memberService.findByEmailAndWorkspaceKey(any(), any()))
            .thenReturn(testMember);

        when(directMessageChannelRepository
            .findByFirstMemberAndSecondMember(any(), any()))
            .thenReturn(Optional.of(testDirectMessageChannel));

        when(idEncoder.encode(testDirectMessageChannel.getId(), testDirectMessageChannel.getType()))
            .thenReturn("testtest");

        //when
        final String actualId =
            directMessageChannelService.create(validWorkspaceId, validReceiverEmail, member);

        final String expectedId = "testtest";

        //then
        Assertions.assertThat(expectedId).isEqualTo(actualId);

        verify(workspaceService).findByKey(any());
        verify(memberService, times(1))
            .findByEmailAndWorkspaceKey(any(), any());
        verify(directMessageChannelRepository, times(1))
            .findByFirstMemberAndSecondMember(any(), any());
        verify(idEncoder).encode(anyLong(), any());
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
        final Member member =
            Member.builder()
                .name("test")
                .email("hey")
                .build();

        when(workspaceService.findByKey(any())).thenReturn(workspace);

        when(memberService.findByEmailAndWorkspaceKey(any(), any()))
            .thenReturn(testMember);

        when(directMessageChannelRepository
            .findByFirstMemberAndSecondMember(any(), any()))
            .thenReturn(Optional.empty());

        MockedConstruction<DirectMessageChannel> mockedConstruction2 =
            Mockito.mockConstruction(DirectMessageChannel.class,
                (mock, context) -> {
              when(mock.getId()).thenReturn(1L);
              when(mock.getType()).thenReturn(EntityIdType.DMCHANNEL);
                });

        when(idEncoder.encode(1L, EntityIdType.DMCHANNEL)).thenReturn("testtest");

        //when
        final String actualId =
            directMessageChannelService.create(validWorkspaceId, validReceiverEmail, member);

        final String expectedId = "testtest";

        //then
        Assertions.assertThat(expectedId).isEqualTo(actualId);

        verify(workspaceService).findByKey(any());
        verify(memberService, times(1))
            .findByEmailAndWorkspaceKey(any(), any());
        verify(directMessageChannelRepository, times(1))
            .findByFirstMemberAndSecondMember(any(), any());
        verify(idEncoder).encode(anyLong(), any());
      }
    }
  }

  @Nested
  @DisplayName("getChannel 메서드는")
  @WithMockCustomLoginUser
  class DescribeGetChannel {

    @Nested
    @DisplayName("Null인 Member를 인자로 받으면")
    class ContextNullMember {

      @Test
      @DisplayName("IllegalArgumentException을 반환한다.")
      void itThrowIllegalArgumentException() {

        //given
        final Member member = null;

        //then
        Assertions.assertThatThrownBy(
                () -> directMessageChannelService.getChannels(member))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("Member의 생성된 DM채널이 존재하지 않는 다면")
    class ContextNotExistWorkspaceId {

      @Test
      @DisplayName("빈 리스트를 반환한다.")
      void itThrowNotFoundException() {

        //given
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .workspace(workspace)
            .build();

        final List<DirectMessageChannel> emptyList = List.of();
        when(directMessageChannelRepository.findAllByMember(any())).thenReturn(emptyList);

        final List<DirectMessageChannelResponse> expect = List.of();

        //when
        final List<DirectMessageChannelResponse> actual =
            directMessageChannelService.getChannels(member);

        //then
        Assertions.assertThat(actual).isEqualTo(expect);
        verify(directMessageChannelRepository).findAllByMember(any());
      }
    }

    @Nested
    @DisplayName("해당 멤버의 Dm채들이 존재한다면")
    class ContextExistDirectMessageChannel {

      @Test
      @DisplayName("해당 채널들의 ResponseDto List를 반환한다.")
      void itReturnResponseDtoList() {

        //given
        final Workspace workspace = new Workspace("test", "test");
        final Member member =
            Member.builder()
                .name("test")
                .email("hey")
                .workspace(workspace)
                .role(Role.ROLE_OWNER)
                .build();
        final DirectMessageChannel firstChannel =
            new DirectMessageChannel(member, member, workspace);
        final DirectMessageChannel secondChannel =
            new DirectMessageChannel(member, member, workspace);
        ReflectionTestUtils.setField(firstChannel, "id", 1L);
        ReflectionTestUtils.setField(secondChannel, "id", 2L);
        final List<DirectMessageChannel> channels = List.of(firstChannel, secondChannel);

        when(directMessageChannelRepository.findAllByMember(any())).thenReturn(channels);

        final String firstExpectedId = idEncoder.encode(1, EntityIdType.DMCHANNEL);
        final String secondExpectedId = idEncoder.encode(2, EntityIdType.DMCHANNEL);

        //when
        final List<DirectMessageChannelResponse> actual =
            directMessageChannelService.getChannels(member);

        //then
        Assertions.assertThat(actual.size()).isEqualTo(2);
        Assertions.assertThat(actual.get(0).getEncodedDirectMessageChannelId())
            .isEqualTo(firstExpectedId);
        Assertions.assertThat(actual.get(1).getEncodedDirectMessageChannelId())
            .isEqualTo(secondExpectedId);
      }
    }
  }
}
