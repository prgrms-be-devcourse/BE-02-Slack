package com.prgrms.be02slack.message.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeAll;
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

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.message.entity.Message;
import com.prgrms.be02slack.message.repository.MessageRepository;
import com.prgrms.be02slack.workspace.entity.Workspace;

@ExtendWith(MockitoExtension.class)
class DefaultMessageServiceTest {

  private static Member testMember;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private MemberService memberService;

  @InjectMocks
  private DefaultMessageService messageService;

  @BeforeAll
  static void beforeAll() {
    Workspace testWorkspace = Workspace.createDefaultWorkspace();
    testMember = Member.builder()
        .email("test@test.com")
        .name("test")
        .displayName("test")
        .role(Role.ROLE_USER)
        .workspace(testWorkspace)
        .build();
  }

  @Nested
  @DisplayName("sendMessage 메서드는")
  class DescribeSendMessage {

    @Nested
    @DisplayName("모든 값이 전달되면")
    class ContextWithPassAllValue {

      @Test
      @DisplayName("save 메서드를 호출한다")
      void ItCallSave() {
        //given
        given(memberService.findByEmailAndWorkspaceKey(anyString(), anyString()))
            .willReturn(testMember);

        //when
        messageService.sendMessage(testMember, "TEST", "TEST", "testContent");

        //then
        verify(messageRepository).save(any(Message.class));
      }
    }

    @Nested
    @DisplayName("member 가 null 값이면")
    class ContextWithNullMember {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> messageService.sendMessage(null, "TEST", "TEST", "testContent"));
      }
    }

    @Nested
    @DisplayName("encodedWorkspaceId 가 빈값이면")
    class ContextWithBlankWorkspaceId {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", " "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String src) {
        assertThrows(IllegalArgumentException.class,
            () -> messageService.sendMessage(testMember, src, "TEST", "testContent"));
      }
    }

    @Nested
    @DisplayName("encodedChannelId 가 빈값이면")
    class ContextWithBlankChannelId {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", " "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String src) {
        assertThrows(IllegalArgumentException.class,
            () -> messageService.sendMessage(testMember, "TEST", src, "testContent"));
      }
    }

    @Nested
    @DisplayName("content 가 빈값이면")
    class ContextWithBlankContent {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", " "})
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException(String src) {
        assertThrows(IllegalArgumentException.class,
            () -> messageService.sendMessage(testMember, "TEST", "TEST", src));
      }
    }
  }
}
