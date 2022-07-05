package com.prgrms.be02slack.directmessagechannel.controller;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.directmessagechannel.controller.dto.DirectMessageChannelResponse;
import com.prgrms.be02slack.directmessagechannel.service.DirectMessageChannelService;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.security.CurrentMember;

@RestController
@Validated
@RequestMapping("api/v1/workspaces/{workspaceId}/directMessageChannels")
public class DirectMessageChannelApiController {

  private final DirectMessageChannelService directMessageChannelService;

  public DirectMessageChannelApiController(
      DirectMessageChannelService directMessageChannelService) {
    this.directMessageChannelService = directMessageChannelService;
  }

  @PostMapping
  public String create(
      @CurrentMember Member member,
      @PathVariable @NotBlank String workspaceId,
      @RequestParam @NotBlank @Email String receiverEmail
  ) {
    return directMessageChannelService.create(workspaceId, receiverEmail, member);
  }

  @GetMapping
  public List<DirectMessageChannelResponse> getChannels(
      @PathVariable @NotBlank String workspaceId,
      @CurrentMember Member member
  ) {
    return directMessageChannelService.getChannels(member);
  }
}
