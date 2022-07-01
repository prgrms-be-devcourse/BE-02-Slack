package com.prgrms.be02slack.email.service;

import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.exception.UnverifiedEmailException;
import com.prgrms.be02slack.email.controller.dto.EmailRequest;
import com.prgrms.be02slack.email.repository.EmailRepository;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;

@Service
public class EmailService {
  private final JavaMailSender javaMailSender;
  private final TemplateEngine templateEngine;
  private final EmailRepository emailRepository;

  public EmailService(
      JavaMailSender javaMailSender,
      TemplateEngine templateEngine,
      EmailRepository emailRepository
  ) {
    this.javaMailSender = javaMailSender;
    this.templateEngine = templateEngine;
    this.emailRepository = emailRepository;
  }

  public void setLoginMail(EmailRequest request) throws
      MessagingException {
    String email = request.getEmail();
    String code = createCode();

    MimeMessage message = javaMailSender.createMimeMessage();
    message.addRecipients(MimeMessage.RecipientType.TO, email);
    message.setSubject("Slack 확인 코드: " + code);
    message.setText(setLoginContext(code), "utf-8", "html");
    javaMailSender.send(message);
    emailRepository.saveCode(email, code);
  }

  private String setLoginContext(String code) {
    Context context = new Context();
    context.setVariable("code", code);
    return templateEngine.process("loginMail", context);
  }

  public void sendInviteEmail(
      EmailRequest request, String token, String workspaceId,
      String channelId, String workspaceName, String sender) throws
      MessagingException {
    String email = request.getEmail();

    MimeMessage message = javaMailSender.createMimeMessage();
    message.addRecipients(MimeMessage.RecipientType.TO, email);
    message.setSubject(sender + "님이 Slack에서 함께 작업할 수 있도록 고객님을 초대했습니다.");
    message.setText(setInviteContext(token, workspaceId, channelId, workspaceName, sender), "utf-8",
        "html");
    javaMailSender.send(message);
  }

  private String setInviteContext(
      String token, String workspaceId,
      String channelId, String workspaceName, String sender) {
    Context context = new Context();
    context.setVariable("token", token);
    context.setVariable("workspaceId", workspaceId);
    context.setVariable("channelId", channelId);
    context.setVariable("workspaceName", workspaceName);
    context.setVariable("sender", sender);

    return templateEngine.process("inviteMail", context);
  }

  private String createCode() {
    StringBuilder code = new StringBuilder();
    Random rnd = new Random();
    for (int i = 0; i < 7; i++) {
      int rIndex = rnd.nextInt(3);
      switch (rIndex) {
        case 0:
          code.append((char)(rnd.nextInt(26) + 97));
          break;
        case 1:
          code.append((char)(rnd.nextInt(26) + 65));
          break;
        case 2:
          code.append((rnd.nextInt(10)));
          break;
      }
    }
    return code.toString();
  }

  public void verifyCode(VerificationRequest request) {
    String foundCode = emailRepository.findCodeByEmail(request.getEmail());

    if (foundCode == null) {
      throw new NotFoundException("Verification Code not found");
    }

    if (!foundCode.equals(request.getVerificationCode())) {
      throw new UnverifiedEmailException("Incorret Vericiation Code");
    }
  }
}
