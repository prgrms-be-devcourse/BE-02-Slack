package com.prgrms.be02slack.workspace.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.prgrms.be02slack.workspace.entity.Workspace;

public class WorkspaceUpdateRequest {

  @NotBlank
  @Size(min = 1, max = 50)
  private final String name;

  @NotBlank
  @Size(min = 1, max = 21)
  private final String url;

  public WorkspaceUpdateRequest(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public static Workspace toEntity(WorkspaceUpdateRequest request) {
    return new Workspace(request.getName(), request.getUrl());
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }
}
