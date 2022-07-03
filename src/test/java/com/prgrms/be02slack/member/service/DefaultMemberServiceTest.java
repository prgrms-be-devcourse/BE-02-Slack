package com.prgrms.be02slack.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.email.service.EmailService;
import com.prgrms.be02slack.member.controller.dto.MemberResponse;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.repository.MemberRepository;
import com.prgrms.be02slack.security.TokenProvider;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class DefaultMemberServiceTest {

  @Mock
  MemberRepository repository;

  @Mock
  WorkspaceService workspaceService;

  @Mock
  EmailService emailService;

  @Mock
  TokenProvider tokenProvider;

  @Mock
  IdEncoder idEncoder;

  @Spy
  @InjectMocks
  DefaultMemberService memberService;

  @Nested
  @DisplayName("FindByEmailAndWorkspace 메서드는")
  class DescribeFindByEmailAndWorkspace {

    @Nested
    @DisplayName("존재하지 않는 멤버 email 값을 인자로 받으면")
    class ContextWithNotExistEmail {

      @Test
      @DisplayName("Throw NotfoundException")
      void ItThrowNotFoundException() {

        final String email = "notfound@exception.com";
        final String key = "ABC123ABC";
        final Workspace findWorkspace = new Workspace("test", "test");

        when(workspaceService.findByKey(key)).thenReturn(findWorkspace);
        when(repository.findByEmailAndWorkspace(email, findWorkspace)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> memberService.findByEmailAndWorkspaceKey(email, key))
            .isInstanceOf(NotFoundException.class);
      }
    }

    @Nested
    @DisplayName("key가 비어있는 인자를 받으면")
    class ContextWithKeyNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String key) {
        final String email = "test@test.com";
        Assertions.assertThatThrownBy(() -> memberService.findByEmailAndWorkspaceKey(email, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("email이 비어있는 인자를 받으면")
    class ContextWithEmailNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String email) {
        final String key = "AAAADB24";
        Assertions.assertThatThrownBy(() -> memberService.findByEmailAndWorkspaceKey(email, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("존재하는 멤버 email 값을 인자로 받으면")
    class ContextWithExistEmail {

      @Test
      @DisplayName("해당 멤버를 반환한다")
      void ItThrowNotFoundException() {
        //given
        final var savedEmail = "test@naver.com";

        final var findWorkspace = new Workspace("test", "test");
        final var savedMember = Member.builder().name("test")
            .workspace(findWorkspace)
            .email(savedEmail)
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();

        given(workspaceService.findByKey(anyString())).willReturn(findWorkspace);
        given(repository.findByEmailAndWorkspace(anyString(), any(Workspace.class)))
            .willReturn(Optional.of(savedMember));

        //when
        final var foundMember = memberService.findByEmailAndWorkspaceKey(savedEmail, "test");

        //then
        final var foundMemberEmail = ReflectionTestUtils.getField(foundMember, "email");
        assertEquals(savedEmail, foundMemberEmail);
      }
    }
  }

  @Nested
  @DisplayName("checkMemberName 메서드는")
  class DescribeCheckMemberName {

    @Nested
    @DisplayName("id 값이 null 이라면")
    class ContextNullIdArgument {

      @Test
      @DisplayName("IllegalArgumentException 을 던진다.")
      void itTrowIllegalArgumentException() {
        //given
        final String channelName = "ABC123";

        //then
        Assertions.assertThatThrownBy(
                () -> memberService.isDuplicateName(null, channelName))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("비어있는 channelName 값을 인자로 받으면")
    class ContextNullEmptyNameArgument {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 던진다.")
      void itTrowIllegalArgumentException(String channelName) {
        //given
        final Long validWorkspaceId = 123L;

        //then
        Assertions.assertThatThrownBy(
                () -> memberService.isDuplicateName(validWorkspaceId, channelName))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("유효한 workspaceId 와 channelName 값을 인자로 받으면")
    class ContextValidArgument {

      @Test
      @DisplayName("해당 이름을 가진 사용자가 존재할 경우 false 를 반환한다.")
      void itReturnFalse() {
        //given
        final Long validWorkspaceId = 123L;
        final String validChannelName = "hello";
        final Optional<Member> member = Optional.of(
            Member.builder()
                .name("test")
                .build()
        );

        when(repository.findByNameAndWorkspace_Id(any(), any())).thenReturn(member);

        //when
        final boolean expected =
            memberService.isDuplicateName(validWorkspaceId, validChannelName);

        //then
        assertThat(false).isEqualTo(expected);
      }

      @Test
      @DisplayName("해당 이름을 가진 사용자가 존재하지 않을 경우 true 를 반환한다.")
      void itReturnTrue() {
        //given
        final Long validWorkspaceId = 123L;
        final String validChannelName = "hello";
        final Optional<Member> member = Optional.empty();

        when(repository.findByNameAndWorkspace_Id(any(), any())).thenReturn(member);

        //when
        final boolean expected =
            memberService.isDuplicateName(validWorkspaceId, validChannelName);

        //then
        assertThat(true).isEqualTo(expected);
      }
    }
  }

  @Nested
  @DisplayName("verify 메서드는")
  class DescribeVerify {

    @Nested
    @DisplayName("인자에 null 값이 전달되면")
    class ContextWithVerificationRequestNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다.")
      void ItResponseIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> memberService.verify(null));
      }
    }

    @Nested
    @DisplayName("해당 이메일이 속한 워크스페이스가 없을 경우")
    class ContextWithNotIncludedAnyWorkspace {

      @Test
      @DisplayName("멤버를 생성하고 토큰을 발급한다")
      void ItResponseCreateMemberAndIssueToken() {
        //given
        final VerificationRequest verificationRequest =
            new VerificationRequest("test@test.com", "test");
        final Workspace workspace = Workspace.createDefaultWorkspace();
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_OWNER)
            .workspace(workspace)
            .build();
        final AuthResponse verificationResponse =
            new AuthResponse("testToken");

        doNothing().when(emailService).verifyCode(verificationRequest);
        given(repository.findByEmail(anyString())).willReturn(Optional.empty());
        given(workspaceService.create()).willReturn(workspace);
        given(repository.save(any(Member.class))).willReturn(member);
        given(tokenProvider.createLoginToken(anyString())).willReturn("testToken");

        //when
        AuthResponse response = memberService.verify(verificationRequest);

        //then
        verify(workspaceService).create();
        verify(repository).save(any(Member.class));
        verify(tokenProvider).createLoginToken(anyString());
        assertThat(response).usingRecursiveComparison().isEqualTo(verificationResponse);
      }
    }

    @Nested
    @DisplayName("해당 이메일이 속한 워크스페이스가 있을 경우")
    class ContextWithIncludedAnyWorkspace {

      @Test
      @DisplayName("토큰을 발급한다")
      void ItResponseIssueToken() {
        //given
        final VerificationRequest verificationRequest =
            new VerificationRequest("test@test.com", "test");
        final Workspace workspace = Workspace.createDefaultWorkspace();
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_OWNER)
            .workspace(workspace)
            .build();
        final AuthResponse verificationResponse = new AuthResponse("testToken");

        doNothing().when(emailService).verifyCode(verificationRequest);
        given(repository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(tokenProvider.createLoginToken(anyString())).willReturn("testToken");

        //when
        AuthResponse response = memberService.verify(verificationRequest);

        //then
        verify(tokenProvider).createLoginToken(anyString());
        assertThat(response).usingRecursiveComparison().isEqualTo(verificationResponse);
      }
    }
  }

  @Nested
  @DisplayName("enterWorkspace 메서드는")
  class DescribeEnterWorkspace {

    @Nested
    @DisplayName("email이 비어있는 인자를 받으면")
    class ContextWithKeyNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 반환한다")
      void ItThrowIllegalArgumentException(String email) {
        final String encodedWorkspaceId = "TESTID";
        assertThatThrownBy(() -> memberService.enterWorkspace(email, encodedWorkspaceId))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("encodedWorkspaceId가 비어있는 인자를 받으면")
    class ContextWithEncodedWorkspaceIdNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 반환한다")
      void ItThrowIllegalArgumentException(String encodedWorkspaceId) {
        final String email = "test@test.com";
        assertThatThrownBy(() -> memberService.enterWorkspace(email, encodedWorkspaceId))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("유효한 email과 encodedWorkspaceId 값을 인자로 받으면")
    class ContextValidArgument {

      @Test
      @DisplayName("토큰을 발급한다")
      void ItResponseIssueToken() {
        //given
        final String email = "test@test.com";
        final String encodedWorkspaceId = "TESTID";
        final Workspace workspace = Workspace.createDefaultWorkspace();
        final Member member = Member.builder()
            .email(email)
            .name("test")
            .displayName("test")
            .role(Role.ROLE_OWNER)
            .workspace(workspace)
            .build();
        final AuthResponse verificationResponse = new AuthResponse("testToken");
        doReturn(member).when(memberService).findByEmailAndWorkspaceKey(anyString(), anyString());
        given(tokenProvider.createMemberToken(anyString(), anyString())).willReturn("testToken");

        //when
        Member member1 = memberService.findByEmailAndWorkspaceKey(email,
            encodedWorkspaceId);
        final AuthResponse response = memberService.enterWorkspace(email, encodedWorkspaceId);

        //then
        verify(tokenProvider).createMemberToken(anyString(), anyString());
        assertThat(response).usingRecursiveComparison().isEqualTo(verificationResponse);
      }
    }
  }

  @Nested
  @DisplayName("FindByNameAndWorkspace 메서드는")
  class DescribeFindByNameAndWorkspace {

    @Nested
    @DisplayName("존재하지 않는 멤버 name 값을 인자로 받으면")
    class ContextWithNotExistName {

      @Test
      @DisplayName("Throw NotfoundException")
      void ItThrowNotFoundException() {

        final String name = "testName";
        final String key = "ABC123ABC";
        final Long decodedId = 123L;

        when(idEncoder.decode(anyString())).thenReturn(decodedId);
        when(repository.findByNameAndWorkspace_Id(name, decodedId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> memberService.findByNameAndWorkspaceKey(name, key))
            .isInstanceOf(NotFoundException.class);
      }
    }

    @Nested
    @DisplayName("key가 비어있는 인자를 받으면")
    class ContextWithKeyNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String key) {
        final String name = "testName";
        Assertions.assertThatThrownBy(() -> memberService.findByNameAndWorkspaceKey(name, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("name이 비어있는 인자를 받으면")
    class ContextWithNameNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String name) {
        final String key = "AAAADB24";
        Assertions.assertThatThrownBy(() -> memberService.findByNameAndWorkspaceKey(name, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("존재하는 멤버 name 값을 인자로 받으면")
    class ContextWithExistentName {

      @Test
      @DisplayName("해당 멤버를 반환한다")
      void ItReturnsMember() {
        //given
        final var decodedId = 123L;
        final var savedName = "testName";

        final var findWorkspace = new Workspace("test", "test");
        final var savedMember = Member.builder().name(savedName)
            .workspace(findWorkspace)
            .email("test@gmail.com")
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();

        given(idEncoder.decode(anyString())).willReturn(decodedId);
        given(repository.findByNameAndWorkspace_Id(anyString(), anyLong()))
            .willReturn(Optional.of(savedMember));

        //when
        final var foundMember = memberService.findByNameAndWorkspaceKey(savedName, "test");

        //then
        final var foundMemberEmail = ReflectionTestUtils.getField(foundMember, "name");
        assertEquals(savedName, foundMemberEmail);
      }
    }
  }

  @Nested
  @DisplayName("IsExistsByNameAndWorkspaceKey 메서드는")
  class DescribeIsExistsByNameAndWorkspaceKey {
    @Nested
    @DisplayName("key 가 비어있는 인자를 받으면")
    class ContextWithKeyNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String key) {
        String name = "testName";
        Assertions.assertThatThrownBy(() -> memberService.isExistsByNameAndWorkspaceKey(name, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("name 이 비어있는 인자를 받으면")
    class ContextWithNameNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String name) {
        String key = "AAAADB24";
        Assertions.assertThatThrownBy(() -> memberService.isExistsByNameAndWorkspaceKey(name, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("워크스페이스에 해당 이름을 가진 멤버가 존재한다면")
    class ContextWithExistMember {

      @Test
      @DisplayName("true 를 반환한다")
      void ItReturnTrue() {
        //given
        String name = "testName";
        String key = "AAAADB24";
        Long decodedWorkspaceId = 123L;

        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspaceId);
        given(repository.existsByNameAndWorkspace_Id(anyString(), anyLong()))
            .willReturn(true);

        //when
        boolean isExists = memberService.isExistsByNameAndWorkspaceKey(name, key);

        //then
        verify(repository).existsByNameAndWorkspace_Id(anyString(), anyLong());
        assertThat(isExists).isTrue();
      }
    }

    @Nested
    @DisplayName("워크스페이스에 해당 이름을 가진 멤버가 존재하지 않는다면")
    class ContextWithNonexistentMember {

      @Test
      @DisplayName("false 를 반환한다")
      void ItReturnFalse() {
        //given
        String name = "testName";
        String key = "AAAADB24";
        Long decodedWorkspaceId = 123L;

        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspaceId);
        given(repository.existsByNameAndWorkspace_Id(anyString(), anyLong()))
            .willReturn(false);

        //when
        boolean isExists = memberService.isExistsByNameAndWorkspaceKey(name, key);

        //then
        assertThat(isExists).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("IsExistsByEmailAndWorkspaceKey 메서드는")
  class DescribeIsExistsByEmailAndWorkspaceKey {
    @Nested
    @DisplayName("key 가 비어있는 인자를 받으면")
    class ContextWithKeyNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String key) {
        String email = "test@gmail.com";
        Assertions.assertThatThrownBy(
                () -> memberService.isExistsByEmailAndWorkspaceKey(email, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("email 이 비어있는 인자를 받으면")
    class ContextWithEmailNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException 을 반환한다.")
      void ItThrowIllegalArgumentException(String email) {
        String key = "AAAADB24";
        Assertions.assertThatThrownBy(
                () -> memberService.isExistsByEmailAndWorkspaceKey(email, key))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("워크스페이스에 해당 이메일을 가진 멤버가 존재한다면")
    class ContextWithExistentMember {

      @Test
      @DisplayName("true 를 반환한다")
      void ItReturnTrue() {
        //given
        String email = "test@gmail.com";
        String key = "AAAADB24";
        Long decodedWorkspaceId = 123L;

        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspaceId);
        given(repository.existsByEmailAndWorkspace_Id(anyString(), anyLong()))
            .willReturn(true);

        //when
        boolean isExists = memberService.isExistsByEmailAndWorkspaceKey(email, key);

        //then
        verify(repository).existsByEmailAndWorkspace_Id(anyString(), anyLong());
        assertThat(isExists).isTrue();
      }
    }

    @Nested
    @DisplayName("워크스페이스에 해당 메일을 가진 멤버가 존재하지 않는다면")
    class ContextWithNonexistentMember {

      @Test
      @DisplayName("false 를 반환한다")
      void ItReturnFalse() {
        //given
        String email = "test@gmail.com";
        String key = "AAAADB24";
        Long decodedWorkspaceId = 123L;

        given(idEncoder.decode(anyString()))
            .willReturn(decodedWorkspaceId);
        given(repository.existsByEmailAndWorkspace_Id(anyString(), anyLong()))
            .willReturn(false);

        //when
        boolean isExists = memberService.isExistsByEmailAndWorkspaceKey(email, key);

        //then
        assertThat(isExists).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("getOne 메서드는")
  class DescribeGetOne {

    @Nested
    @DisplayName("member에 null 값이 전달되면")
    class ContextWithMemberNull {

      @Test
      @DisplayName("IllegalArgumentException을 반환한다")
      void ItResponseIllegalArgumentException() {
        String encodedMemberId = "TESTID";
        assertThrows(IllegalArgumentException.class, () ->
            memberService.getOne(null, encodedMemberId));
      }
    }

    @Nested
    @DisplayName("encodedMemberId가 비어있는 인자를 받으면")
    class ContextWithEncodedMemberIdNullAndEmptySource {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 반환한다")
      void ItThrowIllegalArgumentException(String encodedMemberId) {
        //given
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "workspace", workspace);

        //when, then
        assertThrows(IllegalArgumentException.class, () ->
            memberService.getOne(member, encodedMemberId));
      }
    }

    @Nested
    @DisplayName("해당 멤버 id와 워크스페이스 id을 가진 멤버가 존재하지 않은 경우")
    class ContextWithNotExistedMember {

      @Test
      @DisplayName("NotFoundException을 반환한다")
      void ItResponseNotFoundException() {
        //given
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "workspace", workspace);
        final String encodedMemberId = "TESTID";
        given(idEncoder.decode(anyString())).willReturn(1L);
        given(repository.findByIdAndWorkspace_id(anyLong(), anyLong())).willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class, () ->
            memberService.getOne(member, encodedMemberId));
      }
    }

    @Nested
    @DisplayName("해당 멤버 id와 워크스페이스 id을 가진 멤버가 존재할 경우")
    class ContextWithExistedMember {

      @Test
      @DisplayName("멤버 정보를 담은 memberResponse을 반환한다")
      void ItResponseMemberResponse() {
        //given
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "workspace", workspace);
        final String encodedMemberId = "TESTID";
        final MemberResponse memberResponse = MemberResponse.builder()
            .id("TESTID")
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();
        given(idEncoder.decode(anyString())).willReturn(1L);
        given(repository.findByIdAndWorkspace_id(anyLong(), anyLong())).willReturn(Optional.of(member));

        //when
        final MemberResponse foundMemberResponse = memberService.getOne(member, encodedMemberId);

        // then
        assertThat(foundMemberResponse).usingRecursiveComparison().isEqualTo(memberResponse);
      }
    }
  }
}
