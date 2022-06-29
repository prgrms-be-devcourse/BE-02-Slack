package com.prgrms.be02slack.workspace.controller.dto;

import com.prgrms.be02slack.workspace.entity.Workspace;

public class WorkspaceResponse {

  private final Long id;

  private final String name;

  private final String url;

  private WorkspaceResponse(Long id, String name, String url) {
    this.id = id;
    this.name = name;
    this.url = url;
  }

  public static WorkspaceResponse from(Workspace workspace) {
    return new WorkspaceResponse(workspace.getId(), workspace.getName(), workspace.getUrl());
  }
}
