package com.prgrms.be02slack.workspace.controller;

import com.prgrms.be02slack.workspace.controller.dto.WorkspaceUpdateRequest;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/workspaces")
public class WorkspaceApiController {

  private final WorkspaceService workspaceService;

  public WorkspaceApiController(WorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }
}
