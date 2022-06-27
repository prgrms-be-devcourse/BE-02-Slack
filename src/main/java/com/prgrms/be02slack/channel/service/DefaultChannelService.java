package com.prgrms.be02slack.channel.service;

import static org.apache.logging.log4j.util.Strings.*;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.exception.NameDuplicateException;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.member.service.DefaultMemberService;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.repository.WorkspaceRepository;

@Service
public class DefaultChannelService implements ChannelService {
  private final ChannelRepository channelRepository;
  private final WorkspaceRepository workspaceRepository;
  private final DefaultMemberService defaultMemberService;
  private final IdEncoder idEncoder;

  public DefaultChannelService(
      ChannelRepository channelRepository,
      WorkspaceRepository workspaceRepository,
      DefaultMemberService defaultMemberService,
      IdEncoder idEncoder) {
    this.channelRepository = channelRepository;
    this.workspaceRepository = workspaceRepository;
    this.defaultMemberService = defaultMemberService;
    this.idEncoder = idEncoder;
  }

  /**
   * 1. 멤버(소유주) 정보 파라미터로 넘어오면 이후 처리 구현 필요
   * 2. 테스트 수정 필요
   */
  @Override
  public String create(String workspaceId, ChannelSaveRequest channelSaveRequest) {
    Assert.isTrue(isNotBlank(workspaceId), "WorkspaceId must be provided");
    Assert.notNull(channelSaveRequest, "ChannelSaveRequest must be provided");

    long decodedWorkspaceId = idEncoder.decode(workspaceId);
    Workspace workspace = workspaceRepository.findById(decodedWorkspaceId)
        .orElseThrow(() -> new NotFoundException("Workspace not found"));

    validateName(decodedWorkspaceId, channelSaveRequest.getName());

    // 멤버 조회 로직 구현 필요

    Channel channel = Channel.builder()
        .name(channelSaveRequest.getName())
        .description(channelSaveRequest.getDescription())
        .isPrivate(channelSaveRequest.isPrivate())
        .workspace(workspace)
        .owner(null) // <- 이 부분 수정 필요
        .build();
    Channel savedChannel = channelRepository.save(channel);

    return idEncoder.encode(savedChannel.getId());
  }

  private void validateName(long decodedWorkspaceId, String name) {
    if (isDuplicateChannelName(decodedWorkspaceId, name)) {
      throw new NameDuplicateException(
          "Name is duplicate with the name of another channel in the same workspace");
    }
    if (defaultMemberService.isDuplicateMemberName(decodedWorkspaceId, name)) {
      throw new NameDuplicateException(
          "Name is duplicate with the name of another member in the same workspace");
    }
  }

  private boolean isDuplicateChannelName(Long decodedWorkspaceId, String name) {
    return channelRepository.existsByWorkspace_IdAndName(decodedWorkspaceId, name);
  }
}
