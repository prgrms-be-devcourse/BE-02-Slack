package com.prgrms.be02slack.member.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.service.MemberService;

@RestController
@RequestMapping("api/v1/members")
public class MemberApiController {

  private final MemberService memberService;

  public MemberApiController(MemberService memberService) {
    this.memberService = memberService;
  }

  @PostMapping("verify")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse verify(@RequestBody @Valid VerificationRequest request) {
    return memberService.verify(request);
  }
}
