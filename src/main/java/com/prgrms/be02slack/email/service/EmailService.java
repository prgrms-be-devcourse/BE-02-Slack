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
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.email.repository.EmailRepository;

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

	public void sendMail(EmailRequest request) throws
			MessagingException {
		String email = request.getEmail();
		String code = createCode();

		MimeMessage message = javaMailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, email);
		message.setSubject("Slack 확인 코드: " + code);
		message.setText(setContext(code), "utf-8", "html");
		javaMailSender.send(message);
		emailRepository.saveCode(email, code);
	}

	private String setContext(String code) {
		Context context = new Context();
		context.setVariable("code", code);
		return templateEngine.process("mail", context);
	}

	private String createCode() {
		StringBuilder code = new StringBuilder();
		Random rnd = new Random();
		for (int i = 0; i < 7; i++) {
			int rIndex = rnd.nextInt(3);
			switch (rIndex) {
				case 0:
					code.append((char) (rnd.nextInt(26) + 97));
					break;
				case 1:
					code.append((char) (rnd.nextInt(26) + 65));
					break;
				case 2:
					code.append((rnd.nextInt(10)));
					break;
			}
		}
		return code.toString();
	}
}
