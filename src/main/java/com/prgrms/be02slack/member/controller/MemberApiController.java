package com.prgrms.be02slack.member.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.member.service.MemberService;

@RestController
@RequestMapping("api/v1/members")
public class MemberApiController {

  private final MemberService memeberService;

  public MemberApiController(MemberService memeberService) {
    this.memeberService = memeberService;
  }
}
