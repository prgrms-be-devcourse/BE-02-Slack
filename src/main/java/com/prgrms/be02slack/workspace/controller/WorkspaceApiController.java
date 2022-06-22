package com.prgrms.be02slack.workspace.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.workspace.service.WorkspaceService;

@RestController
@RequestMapping("api/v1/workspaces")
public class WorkspaceApiController {

  private final WorkspaceService workspaceService;

  public WorkspaceApiController(WorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }
}
