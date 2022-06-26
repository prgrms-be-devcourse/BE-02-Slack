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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.email.service.EmailService;
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

        Assertions.assertThatThrownBy(() -> memberService.findByEmailAndWorkspaceKey(key, email))
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
        Assertions.assertThatThrownBy(() -> memberService.findByEmailAndWorkspaceKey(key, email))
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
        Assertions.assertThatThrownBy(() -> memberService.findByEmailAndWorkspaceKey(key, email))
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
            .role(Role.USER)
            .build();

        given(workspaceService.findByKey(anyString())).willReturn(findWorkspace);
        given(repository.findByEmailAndWorkspace(anyString(), any(Workspace.class)))
            .willReturn(Optional.of(savedMember));

        //when
        final var foundMember = memberService.findByEmailAndWorkspaceKey("test", savedEmail);

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
    @DisplayName("비어있는 key값을 인자로 받으면")
    class ContextNullEmptyKeyArgument {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 던진다.")
      void itTrowIllegalArgumentException(String workspacekey) {
        //given
        final String channelName = "ABC123";

        //then
        Assertions.assertThatThrownBy(
                () -> memberService.isDuplicatedMemberName(workspacekey, channelName))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("비어있는 channelName값을 인자로 받으면")
    class ContextNullEmptyNameArgument {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\t", "\n"})
      @DisplayName("IllegalArgumentException을 던진다.")
      void itTrowIllegalArgumentException(String channelName) {
        //given
        final String validWorkspaceKey = "ABC123";

        //then
        Assertions.assertThatThrownBy(
                () -> memberService.isDuplicatedMemberName(validWorkspaceKey, channelName))
            .isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Nested
    @DisplayName("유효한 workspacekey와 channelName값을 인자로 받으면")
    class ContextValidArgument {

      @Test
      @DisplayName("해당 이름을 가진 사용자가 존재할 경우 false를 반환한다.")
      void itReturnFalse() {
        //given
        final String validWorkspaceKey = "ABC123";
        final String validChannelName = "hello";
        final Optional<Member> member = Optional.of(
            Member.builder()
                .name("test")
                .build()
        );

        when(repository.findByNameAndWorkspace(any(), any())).thenReturn(member);

        //when
        final boolean expected =
            memberService.isDuplicatedMemberName(validWorkspaceKey, validChannelName);

        //then
        assertThat(false).isEqualTo(expected);
      }

      @Test
      @DisplayName("해당 이름을 가진 사용자가 존재하지 않을 경우 true를 반환한다.")
      void itReturnTrue() {
        //given
        final String validWorkspaceKey = "ABC123";
        final String validChannelName = "hello";
        final Optional<Member> member = Optional.empty();

        when(repository.findByNameAndWorkspace(any(), any())).thenReturn(member);

        //when
        final boolean expected =
            memberService.isDuplicatedMemberName(validWorkspaceKey, validChannelName);

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
            .role(Role.OWNER)
            .workspace(workspace)
            .build();
        final AuthResponse verificationResponse =
            new AuthResponse("testToken");

        doNothing().when(emailService).verifyCode(verificationRequest);
        given(repository.findByEmail(anyString())).willReturn(Optional.empty());
        given(workspaceService.create()).willReturn(workspace);
        given(repository.save(any(Member.class))).willReturn(member);
        given(tokenProvider.createToken(anyString())).willReturn("testToken");

        //when
        AuthResponse response = memberService.verify(verificationRequest);

        //then
        verify(workspaceService).create();
        verify(repository).save(any(Member.class));
        verify(tokenProvider).createToken(anyString());
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
            .role(Role.OWNER)
            .workspace(workspace)
            .build();
        final AuthResponse verificationResponse = new AuthResponse("testToken");

        doNothing().when(emailService).verifyCode(verificationRequest);
        given(repository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(tokenProvider.createToken(anyString())).willReturn("testToken");

        //when
        AuthResponse response = memberService.verify(verificationRequest);

        //then
        verify(tokenProvider).createToken(anyString());
        assertThat(response).usingRecursiveComparison().isEqualTo(verificationResponse);
      }
    }
  }
}
