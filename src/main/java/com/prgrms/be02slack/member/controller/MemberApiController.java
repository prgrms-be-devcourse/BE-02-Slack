package com.prgrms.be02slack.member.controller;

import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.member.service.MemberService;

@RestController
public class MemberApiController {

  private final MemberService memeberService;

  public MemberApiController(MemberService memeberService) {
    this.memeberService = memeberService;
  }
}
