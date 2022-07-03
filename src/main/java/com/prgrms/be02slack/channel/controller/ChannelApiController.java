package com.prgrms.be02slack.channel.controller;

import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.controller.dto.InviteRequest;
import com.prgrms.be02slack.channel.service.ChannelService;
import com.prgrms.be02slack.common.dto.AuthResponse;

@Validated
@RestController
@RequestMapping("api/v1/workspaces/{workspaceId}/channels")
public class ChannelApiController {
  private final ChannelService channelService;

  public ChannelApiController(ChannelService channelService) {
    this.channelService = channelService;
  }

  @PostMapping
  public String create(
      @PathVariable @NotBlank String workspaceId,
      @Valid @RequestBody ChannelSaveRequest channelSaveRequest) {
    return channelService.create(workspaceId, channelSaveRequest);
  }

  @PostMapping("{channelId}/invite")
  @ResponseStatus(HttpStatus.OK)
  public void invite(
      @PathVariable @NotBlank String workspaceId,
      @PathVariable @NotBlank String channelId,
      @RequestBody @Valid InviteRequest inviteRequest) throws MessagingException {
    channelService.invite(workspaceId, channelId, inviteRequest);
  }

  @PostMapping("{channelId}/participate")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse participate(
      @PathVariable @NotBlank String workspaceId,
      @PathVariable @NotBlank String channelId,
      @RequestParam @NotBlank String token) {
    return channelService.participate(workspaceId, channelId, token);
  }
}
