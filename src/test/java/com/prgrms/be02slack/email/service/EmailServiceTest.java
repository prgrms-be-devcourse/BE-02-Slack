package com.prgrms.be02slack.email.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.exception.UnverifiedEmailException;
import com.prgrms.be02slack.email.repository.EmailRepository;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

  @Mock
  EmailRepository emailRepository;

  @InjectMocks
  EmailService emailService;

  @Nested
  @DisplayName("verifyCode 메서드는")
  class DescribeVerifyCode {

    @Nested
    @DisplayName("email에 해당하는 verification code가 존재하지 않는 경우")
    class ContextWithNotExistVerificationCode {

      @Test
      @DisplayName("NotFound 에러를 발생시킨다")
      void ItResponseNotFoundException() {
        //given
        final VerificationRequest request = new VerificationRequest("test@test.com", "testCode");
        given(emailRepository.findCodeByEmail(anyString())).willReturn(null);

        //when, then
        assertThrows(NotFoundException.class, () -> emailService.verifyCode(request));
      }
    }

    @Nested
    @DisplayName("verification code가 일치하지 않은 경우")
    class ContextWithIncorrectVerificationCode {

      @Test
      @DisplayName("UnverifiedEmailException 에러를 발생시킨다")
      void ItResponseUnverifiedEmailException() {
        //given
        final VerificationRequest request = new VerificationRequest("test@test.com", "testCode");
        given(emailRepository.findCodeByEmail(anyString())).willReturn("IncorrectCode");

        //when, then
        assertThrows(UnverifiedEmailException.class, () -> emailService.verifyCode(request));
      }
    }

    @Nested
    @DisplayName("verification code가 일치하는 경우")
    class ContextWithCorrectVerificationCode {

      @Test
      @DisplayName("에러가 발생하지 않는다")
      void ItResponseNothing() {
        //given
        final VerificationRequest request = new VerificationRequest("test@test.com", "testCode");
        given(emailRepository.findCodeByEmail(anyString())).willReturn("testCode");

        //when, then
        assertDoesNotThrow(() -> emailService.verifyCode(request));
      }
    }
  }
}
