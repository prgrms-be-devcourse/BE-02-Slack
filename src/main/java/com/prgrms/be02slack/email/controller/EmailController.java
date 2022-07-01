package com.prgrms.be02slack.email.controller;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.email.controller.dto.EmailRequest;
import com.prgrms.be02slack.email.service.EmailService;

@RestController
@RequestMapping("api/v1/email")
public class EmailController {

  private final EmailService emailService;

  public EmailController(EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void send(@Valid @RequestBody EmailRequest request) throws MessagingException {
    emailService.setLoginMail(request);
  }
}
