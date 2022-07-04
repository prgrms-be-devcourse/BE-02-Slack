package com.prgrms.be02slack.member.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.member.controller.dto.MemberResponse;
import com.prgrms.be02slack.member.controller.dto.VerificationRequest;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.service.MemberService;
import com.prgrms.be02slack.security.CurrentMember;

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

  @PostMapping("api/v1/workspaces/{encodedWorkspaceId}/token")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse enterWorkspace(
      @AuthenticationPrincipal String email,
      @PathVariable @NotBlank String encodedWorkspaceId) {
    return memberService.enterWorkspace(email, encodedWorkspaceId);
  }

  @GetMapping("api/v1/members/{encodedMemberId}")
  @ResponseStatus(HttpStatus.OK)
  public MemberResponse getOne(
      @CurrentMember Member member,
      @PathVariable @NotBlank String encodedMemberId) {
    return memberService.getOne(member, encodedMemberId);
  }

  @GetMapping("api/v1/channels/{encodedChannelId}/members")
  @ResponseStatus(HttpStatus.OK)
  public List<MemberResponse> getAllFromChannel(
      @CurrentMember Member member,
      @PathVariable @NotBlank String encodedChannelId) {
    return memberService.getAllFromChannel(member, encodedChannelId);
  }
}
