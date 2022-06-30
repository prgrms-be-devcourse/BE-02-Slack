package com.prgrms.be02slack.member.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.service.MemberService;

@RestController
public class MemberApiController {

  private final MemberService memberService;

  public MemberApiController(MemberService memberService) {
    this.memberService = memberService;
  }

  @PostMapping("api/v1/members/verify")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse verify(@RequestBody @Valid VerificationRequest request) {
    return memberService.verify(request);
  }

  @PostMapping("api/v1/workspaces/{encodedWorkspaceId}/enter")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse enterWorkspace(
      @AuthenticationPrincipal String email,
      @PathVariable String encodedWorkspaceId) {
    return memberService.enterWorkspace(email, encodedWorkspaceId);
  }
}
