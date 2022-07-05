package com.prgrms.be02slack.channel.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.mail.MessagingException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.channel.controller.dto.ChannelResponse;
import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.controller.dto.InviteRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.exception.NameDuplicateException;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.email.controller.dto.EmailRequest;
import com.prgrms.be02slack.email.service.EmailService;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.service.DefaultMemberService;
import com.prgrms.be02slack.security.TokenProvider;
import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;
import com.prgrms.be02slack.subscribeInfo.service.SubscribeInfoService;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.DefaultWorkspaceService;

@ExtendWith(MockitoExtension.class)
class DefaultChannelServiceTest {
  @Mock
  private IdEncoder idEncoder;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private DefaultMemberService defaultMemberService;

  @Mock
  private DefaultWorkspaceService defaultWorkspaceService;

  @Mock
  private SubscribeInfoService subscribeInfoService;

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private DefaultChannelService defaultChannelService;

  @Nested
  @DisplayName("create 메서드는")
  class DescribeCreate {

    @Test
    @DisplayName("Channel 을 저장하고 인코딩된 id를 반환한다")
    void ItSavesChannelThenReturnsEncodedId() {
      //given
      ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName", "testDescription",
          false);
      Channel channel = Channel.builder()
          .name(channelSaveRequest.getName())
          .description(channelSaveRequest.getDescription())
          .isPrivate(channelSaveRequest.isPrivate())
          .build();
      ReflectionTestUtils.setField(channel, "id", 1L);

      given(idEncoder.decode(anyString()))
          .willReturn(1L);
      given(defaultWorkspaceService.findByKey(anyString()))
          .willReturn(Workspace.createDefaultWorkspace());
      given(channelRepository.existsByWorkspace_IdAndName(anyLong(), anyString()))
          .willReturn(false);
      given(defaultMemberService.isDuplicateName(anyLong(), anyString()))
          .willReturn(false);
      given(channelRepository.save(any(Channel.class)))
          .willReturn(channel);
      given(idEncoder.encode(anyLong()))
          .willReturn("encodedTestId");

      //when
      String encodedChannelId = defaultChannelService.create("workspaceId", channelSaveRequest);

      //then
      verify(idEncoder).decode(anyString());
      verify(defaultWorkspaceService).findByKey(anyString());
      verify(channelRepository).save(any(Channel.class));
      verify(idEncoder).encode(anyLong());
      assertThat(encodedChannelId).isNotBlank();
    }

    @Nested
    @DisplayName("channelSaveRequest 파라미터에 null 값이 전달되면")
    class ContextWithChannelSaveRequestNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.create("workspaceId", null));
      }
    }

    @Nested
    @DisplayName("WorkspaceId에 해당하는 workspace 가 존재하지 않는 경우")
    class ContextWithNotExistWorkspace {

      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName",
            "testDescription",
            false);

        given(defaultWorkspaceService.findByKey(anyString()))
            .willThrow(NotFoundException.class);

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.create("workspaceId", channelSaveRequest));
      }
    }

    @Nested
    @DisplayName("name 이 다른 채널 이름과 중복이라면")
    class ContextWithNameIsDuplicateWithNameOfAnotherChannel {

      @Test
      @DisplayName("NameDuplicateException 에러를 발생시킨다")
      void ItThrowsNameDuplicateException() {
        //given
        ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName",
            "testDescription",
            false);

        given(idEncoder.decode(anyString()))
            .willReturn(1L);
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(Workspace.createDefaultWorkspace());
        given(channelRepository.existsByWorkspace_IdAndName(anyLong(), anyString()))
            .willReturn(true);

        //when, then
        assertThrows(NameDuplicateException.class,
            () -> defaultChannelService.create("workspaceId", channelSaveRequest));
      }
    }

    @Nested
    @DisplayName("name 이 다른 멤버 이름과 중복이라면")
    class ContextWithNameIsDuplicateWithNameOfAnotherMember {

      @Test
      @DisplayName("NameDuplicateException 에러를 발생시킨다")
      void ItThrowsNameDuplicateException() {
        //given
        ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName",
            "testDescription",
            false);

        given(idEncoder.decode(anyString()))
            .willReturn(1L);
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(Workspace.createDefaultWorkspace());
        given(channelRepository.existsByWorkspace_IdAndName(anyLong(), anyString()))
            .willReturn(false);
        given(defaultMemberService.isDuplicateName(anyLong(), anyString()))
            .willReturn(true);

        //when, then
        assertThrows(NameDuplicateException.class,
            () -> defaultChannelService.create("workspaceId", channelSaveRequest));
      }
    }
  }

  @Nested
  @DisplayName("invite 메서드는")
  class DescribeInvite {
    @Nested
    @DisplayName("워크스페이스의 멤버이지만 채널은 구독하지 않은 사람의 이메일 정보가 넘어온다면")
    class ContextWithUnsubscribedMemberEmail {
      @Test
      @DisplayName("구독 테이블에 멤버를 insert 한다")
      void ItInvite() throws MessagingException {
        //given
        String workspaceId = "workspaceId";
        String channelId = "channelId";
        InviteRequest inviteRequest = new InviteRequest(Set.of("test@naver.com"),
            "senderName");
        Workspace workspace = Workspace.createDefaultWorkspace();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .workspace(workspace)
            .role(Role.ROLE_USER)
            .email("test@gmail.com")
            .build();
        Channel channel = Channel.builder()
            .name("testName")
            .workspace(workspace)
            .owner(member)
            .isPrivate(false)
            .build();

        Long decodedWorkspace = 123L;
        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspace);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(workspace);
        given(defaultMemberService.isExistsByEmailAndWorkspaceKey(anyString(), anyString()))
            .willReturn(true);
        given(defaultMemberService.findByEmailAndWorkspaceKey(anyString(), anyString()))
            .willReturn(member);
        given(subscribeInfoService.isExistsByChannelAndMemberEmail(any(Channel.class), anyString()))
            .willReturn(false);

        //when
        defaultChannelService.invite(workspaceId, channelId, inviteRequest);

        //then
        verify(idEncoder).decode(anyString());
        verify(channelRepository).findById(anyLong());
        verify(defaultWorkspaceService).findByKey(anyString());
        verify(defaultMemberService).isExistsByEmailAndWorkspaceKey(anyString(), anyString());
        verify(defaultMemberService).findByEmailAndWorkspaceKey(anyString(), anyString());
        verify(subscribeInfoService).subscribe(any(Channel.class), any(Member.class));
      }
    }

    @Nested
    @DisplayName("워크스페이스의 멤버이고 채널도 구독한 사람의 이메일 정보가 넘어온다면")
    class ContextWithSubscriberEmail {
      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        String workspaceId = "workspaceId";
        String channelId = "channelId";
        InviteRequest inviteRequest = new InviteRequest(Set.of("test@naver.com"),
            "senderName");
        Workspace workspace = Workspace.createDefaultWorkspace();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .workspace(workspace)
            .role(Role.ROLE_USER)
            .email("test@gmail.com")
            .build();
        Channel channel = Channel.builder()
            .name("testName")
            .workspace(workspace)
            .owner(member)
            .isPrivate(false)
            .build();

        Long decodedWorkspace = 123L;
        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspace);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(workspace);
        given(defaultMemberService.isExistsByEmailAndWorkspaceKey(anyString(), anyString()))
            .willReturn(true);
        given(subscribeInfoService.isExistsByChannelAndMemberEmail(any(Channel.class), anyString()))
            .willReturn(true);

        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.invite(workspaceId, channelId, inviteRequest));
      }
    }

    @Nested
    @DisplayName("워크스페이스의 멤버가 아닌 사람의 이메일 정보가 넘어온다면")
    class ContextWithUnSubscriberEmail {
      @Test
      @DisplayName("초대 이메일을 보낸다")
      void ItSendsInvitationMail() throws MessagingException {
        //given
        String workspaceId = "workspaceId";
        String channelId = "channelId";
        InviteRequest inviteRequest = new InviteRequest(Set.of("test@naver.com"),
            "senderName");
        Workspace workspace = Workspace.createDefaultWorkspace();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .workspace(workspace)
            .role(Role.ROLE_USER)
            .email("test@gmail.com")
            .build();
        Channel channel = Channel.builder()
            .name("testName")
            .workspace(workspace)
            .owner(member)
            .isPrivate(false)
            .build();

        Long decodedWorkspace = 123L;
        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspace);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(workspace);
        given(defaultMemberService.isExistsByEmailAndWorkspaceKey(anyString(), anyString()))
            .willReturn(false);
        given(tokenProvider.createLoginToken(anyString()))
            .willReturn("token");

        //when
        defaultChannelService.invite(workspaceId, channelId, inviteRequest);

        //then
        verify(tokenProvider).createLoginToken(anyString());
        verify(emailService).sendInviteMail(any(EmailRequest.class), anyString(), anyString(),
            anyString(), anyString(), anyString());
      }
    }

    @Nested
    @DisplayName("워크스페이스의 멤버이지만 채널을 구독하지 않은 멤버의 이름이 넘어온다면")
    class ContextWithUnsubscribedMemberName {
      @Test
      @DisplayName("구독 테이블에 멤버를 insert 한다")
      void ItInvite() throws MessagingException {
        //given
        String workspaceId = "workspaceId";
        String channelId = "channelId";
        InviteRequest inviteRequest = new InviteRequest(Set.of("testName"),
            "senderName");
        Workspace workspace = Workspace.createDefaultWorkspace();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .workspace(workspace)
            .role(Role.ROLE_USER)
            .email("test@gmail.com")
            .build();
        Channel channel = Channel.builder()
            .name("testName")
            .workspace(workspace)
            .owner(member)
            .isPrivate(false)
            .build();

        Long decodedWorkspace = 123L;
        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspace);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(workspace);
        given(defaultMemberService.isExistsByNameAndWorkspaceKey(anyString(), anyString()))
            .willReturn(true);
        given(subscribeInfoService.isExistsByChannelAndMemberName(any(Channel.class), anyString()))
            .willReturn(false);
        given(defaultMemberService.findByNameAndWorkspaceKey(anyString(), anyString()))
            .willReturn(member);

        //when
        defaultChannelService.invite(workspaceId, channelId, inviteRequest);

        //then
        verify(idEncoder).decode(anyString());
        verify(channelRepository).findById(anyLong());
        verify(defaultWorkspaceService).findByKey(anyString());
        verify(defaultMemberService).isExistsByNameAndWorkspaceKey(anyString(), anyString());
        verify(subscribeInfoService).isExistsByChannelAndMemberName(any(Channel.class),
            anyString());
        verify(defaultMemberService).findByNameAndWorkspaceKey(anyString(), anyString());
        verify(subscribeInfoService).subscribe(any(Channel.class), any(Member.class));
      }
    }

    @Nested
    @DisplayName("워크스페이스의 멤버이고 채널도 구독한 사람의 이름이 넘어온다면")
    class ContextWithSubscriberName {
      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        String workspaceId = "workspaceId";
        String channelId = "channelId";
        InviteRequest inviteRequest = new InviteRequest(Set.of("testName"),
            "senderName");
        Workspace workspace = Workspace.createDefaultWorkspace();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .workspace(workspace)
            .role(Role.ROLE_USER)
            .email("test@gmail.com")
            .build();
        Channel channel = Channel.builder()
            .name("testName")
            .workspace(workspace)
            .owner(member)
            .isPrivate(false)
            .build();

        Long decodedWorkspace = 123L;
        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspace);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(workspace);
        given(defaultMemberService.isExistsByNameAndWorkspaceKey(anyString(), anyString()))
            .willReturn(true);
        given(subscribeInfoService.isExistsByChannelAndMemberName(any(Channel.class), anyString()))
            .willReturn(true);

        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.invite(workspaceId, channelId, inviteRequest));
      }
    }

    @Nested
    @DisplayName("워크스페이스의 멤버가 아닌 사람의 이름이 넘어온다면")
    class ContextWithUnSubscriberName {
      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        String workspaceId = "workspaceId";
        String channelId = "channelId";
        InviteRequest inviteRequest = new InviteRequest(Set.of("testName"),
            "senderName");
        Workspace workspace = Workspace.createDefaultWorkspace();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .workspace(workspace)
            .role(Role.ROLE_USER)
            .email("test@gmail.com")
            .build();
        Channel channel = Channel.builder()
            .name("testName")
            .workspace(workspace)
            .owner(member)
            .isPrivate(false)
            .build();

        Long decodedWorkspace = 123L;
        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspace);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(workspace);
        given(defaultMemberService.isExistsByNameAndWorkspaceKey(anyString(), anyString()))
            .willReturn(false);

        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.invite(workspaceId, channelId, inviteRequest));
      }
    }

    @Nested
    @DisplayName("workspaceId 파라미터가 null 이거나 공백 또는 빈 값이라면")
    class ContextWithWorkspaceIdBlank {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String workspaceId) {
        //given
        InviteRequest inviteRequest = new InviteRequest(new HashSet<>(), "senderName");

        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.invite(workspaceId, "channelId", inviteRequest));
      }
    }

    @Nested
    @DisplayName("channelId 파라미터가 null 이거나 공백 또는 빈 값이라면")
    class ContextWithChannelIdBlank {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String channelId) {
        //given
        InviteRequest inviteRequest = new InviteRequest(new HashSet<>(), "senderName");

        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.invite("workspaceId", channelId, inviteRequest));
      }
    }

    @Nested
    @DisplayName("InviteRequest 파라미터가 null 이라면")
    class ContextWithInviteRequestNull {

      @ParameterizedTest
      @NullSource
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(InviteRequest inviteRequest) {
        //given
        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.invite("workspaceId", "channelId", inviteRequest));
      }
    }

    @Nested
    @DisplayName("WorkspaceId에 해당하는 workspace 가 존재하지 않는 경우")
    class ContextWithNotExistWorkspace {

      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        Long workspaceId = 123L;
        Channel channel = Channel.builder()
            .name("testName")
            .isPrivate(false)
            .build();
        InviteRequest inviteRequest = new InviteRequest(new HashSet<>(), "senderName");
        given(idEncoder.decode(anyString()))
            .willReturn(workspaceId);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(defaultWorkspaceService.findByKey(anyString()))
            .willThrow(NotFoundException.class);

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.invite("workspaceId", "channelId", inviteRequest));
      }
    }

    @Nested
    @DisplayName("ChannelId에 해당하는 channel 이 존재하지 않는 경우")
    class ContextWithNotExistChannel {

      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        Long workspaceId = 123L;
        InviteRequest inviteRequest = new InviteRequest(new HashSet<>(), "senderName");
        given(idEncoder.decode(anyString()))
            .willReturn(workspaceId);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.invite("workspaceId", "channelId", inviteRequest));
      }
    }
  }

  @Nested
  @DisplayName("participate 메서드는")
  class DescribeParticipate {
    @Nested
    @DisplayName("올바른 데이터가 넘어온다면")
    class ContextWithValidData {

      @Test
      @DisplayName("구독 테이블에 insert 하고 AuthResponse 를 반환한다")
      void ItSavesSubscribeInfoThenReturnsAuthResponse() {
        //given
        Long workspaceId = 123L;
        String email = "test@gmail.com";
        String memberToken = "loginToken";
        Channel channel = Channel.builder()
            .name("testName")
            .isPrivate(false)
            .build();
        Member member = Member.builder()
            .name("testName")
            .displayName("testName")
            .role(Role.ROLE_USER)
            .build();

        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(Workspace.createDefaultWorkspace());
        given(idEncoder.decode(anyString()))
            .willReturn(workspaceId);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(tokenProvider.validateToken(anyString()))
            .willReturn(true);
        given(tokenProvider.getEmailFromToken(anyString()))
            .willReturn(email);
        given(defaultMemberService.save(anyString(), anyString(), any(Role.class), anyString(),
            anyString()))
            .willReturn(member);
        given(tokenProvider.createMemberToken(anyString(), anyString()))
            .willReturn(memberToken);

        //when
        AuthResponse authResponse = defaultChannelService.participate("workspaceId", "channelId",
            "token");

        //then
        verify(defaultWorkspaceService).findByKey(anyString());
        verify(idEncoder).decode(anyString());
        verify(channelRepository).findById(anyLong());
        verify(tokenProvider).validateToken(anyString());
        verify(tokenProvider).getEmailFromToken(anyString());
        verify(defaultMemberService).save(anyString(), anyString(), any(Role.class), anyString(),
            anyString());
        verify(subscribeInfoService).subscribe(any(Channel.class), any(Member.class));
        verify(tokenProvider).createMemberToken(anyString(), anyString());
        assertThat(authResponse.getToken()).isEqualTo(memberToken);
      }
    }

    @Nested
    @DisplayName("workspaceId 파라미터가 null 이거나 공백 또는 빈 값이라면")
    class ContextWithWorkspaceIdBlank {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String workspaceId) {
        //given
        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.participate(workspaceId, "channelId", "token"));
      }
    }

    @Nested
    @DisplayName("channelId 파라미터가 null 이거나 공백 또는 빈 값이라면")
    class ContextWithChannelIdBlank {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String channelId) {
        //given
        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.participate("workspaceId", channelId, "token"));
      }
    }

    @Nested
    @DisplayName("token 파라미터가 null 이거나 공백 또는 빈 값이라면")
    class ContextWithTokenBlank {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String token) {
        //given
        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.participate("workspaceId", "channelId", token));
      }
    }

    @Nested
    @DisplayName("WorkspaceId에 해당하는 workspace 가 존재하지 않는 경우")
    class ContextWithNotExistWorkspace {

      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        given(defaultWorkspaceService.findByKey(anyString()))
            .willThrow(NotFoundException.class);

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.participate("workspaceId", "channelId", "token"));
      }
    }

    @Nested
    @DisplayName("ChannelId에 해당하는 channel 이 존재하지 않는 경우")
    class ContextWithNotExistChannel {
      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        Long workspaceId = 123L;
        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(Workspace.createDefaultWorkspace());
        given(idEncoder.decode(anyString()))
            .willReturn(workspaceId);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.participate("workspaceId", "channelId", "token"));
      }
    }

    @Nested
    @DisplayName("유효하지 않은 토큰이 넘어온 경우")
    class ContextWithInvalidToken {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        Long workspaceId = 123L;
        Channel channel = Channel.builder()
            .name("testName")
            .isPrivate(false)
            .build();

        given(defaultWorkspaceService.findByKey(anyString()))
            .willReturn(Workspace.createDefaultWorkspace());
        given(idEncoder.decode(anyString()))
            .willReturn(workspaceId);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.of(channel));
        given(tokenProvider.validateToken(anyString()))
            .willReturn(false);

        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.participate("workspaceId", "channelId", "token"));
      }
    }
  }

  @Nested
  @DisplayName("findByKey 메서드는")
  class DescribeFindByKey {
    @Test
    @DisplayName("encodedId로 채널을 조회한다")
    void ItReturnsChannel() {
      //given
      String encodedId = "encodedId";
      Long decodedId = 123L;
      Channel foundChannel = Channel.builder()
          .name("testName")
          .isPrivate(false)
          .build();

      given(idEncoder.decode(anyString()))
          .willReturn(decodedId);
      given(channelRepository.findById(anyLong()))
          .willReturn(Optional.of(foundChannel));

      //when
      Channel channel = defaultChannelService.findByKey(encodedId);

      //then
      verify(idEncoder).decode(anyString());
      verify(channelRepository).findById(anyLong());
      assertThat(channel).isEqualTo(foundChannel);
    }

    @Nested
    @DisplayName("encodedId가 null 이거나 공백 또는 빈 값 이라면")
    class DescribeEncodedIdBlank {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      @DisplayName("IllegalArgumentException 예외를 발생시킨다")
      void ItThrowsIllegalArgumentException(String encodedId) {
        //given
        //when, then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.findByKey(encodedId));
      }
    }

    @Nested
    @DisplayName("channelId에 해당하는 channel 이 존재하지 않는다면")
    class ContextWithNonexistentChannel {
      @Test
      @DisplayName("NotFoundException 예외를 발생시킨다")
      void ItThrowsNotFoundException() {
        //given
        String encodedId = "encodedId";
        Long decodedId = 123L;

        given(idEncoder.decode(anyString()))
            .willReturn(decodedId);
        given(channelRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.findByKey(encodedId));
      }
    }
  }

  @Nested
  @DisplayName("findAllByMember 메서드는")
  class DescribeFindAllByMember {

    @Test
    @DisplayName("멤버가 구독한 채널 목록을 반환한다")
    void ItReturnsSubscribedChannelList() {
      //given
      Workspace workspace = Workspace.createDefaultWorkspace();
      Member member = Member.builder()
          .name("testName")
          .role(Role.ROLE_USER)
          .displayName("testDisplayName")
          .workspace(workspace)
          .build();
      Channel channel = Channel.builder()
          .name("testChannel1")
          .description("channel")
          .isPrivate(false)
          .workspace(workspace)
          .owner(member)
          .build();
      ReflectionTestUtils.setField(channel, "id", 1L);
      List<SubscribeInfo> subscribeInfos = List.of(SubscribeInfo.subscribe(channel, member));
      List<Channel> subscribedChannels = List.of(channel);

      given(idEncoder.encode(anyLong()))
          .willReturn("encodedChannelId");
      given(subscribeInfoService.findAllByMember(any(Member.class)))
          .willReturn(subscribeInfos);

      //when
      List<ChannelResponse> channelResponses = defaultChannelService.findAllByMember(member);

      //then
      verify(idEncoder).encode(anyLong());
      verify(subscribeInfoService).findAllByMember(any(Member.class));
      assertThat(channelResponses.size()).isEqualTo(subscribedChannels.size());
      assertThat(channelResponses.get(0).getName()).isEqualTo(subscribedChannels.get(0).getName());
      assertThat(channelResponses.get(0).isPrivate()).isEqualTo(
          subscribedChannels.get(0).isPrivate());
    }

    @Nested
    @DisplayName("member 파라미터가 null 이라면")
    class ContextWithMemberNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        //given
        //when,then
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.findAllByMember(null));
      }
    }
  }
}
