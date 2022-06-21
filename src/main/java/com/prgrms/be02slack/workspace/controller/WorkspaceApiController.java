package com.prgrms.be02slack.workspace.controller;

import com.prgrms.be02slack.workspace.service.WorkspaceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/workspaces")
public class WorkspaceApiController {

  private final WorkspaceService workspaceService;

  public WorkspaceApiController(WorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }
}
