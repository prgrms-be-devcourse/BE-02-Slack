package com.prgrms.be02slack.workspace.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.prgrms.be02slack.workspace.controller.dto.WorkspaceResponse;
import com.prgrms.be02slack.workspace.controller.dto.WorkspaceUpdateRequest;
import com.prgrms.be02slack.workspace.service.WorkspaceService;

@RestController
@RequestMapping("api/v1/workspaces")
@Validated
public class WorkspaceApiController {

  private final WorkspaceService workspaceService;

  public WorkspaceApiController(WorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }

  @PutMapping("{key}")
  @ResponseStatus(HttpStatus.OK)
  public void update(@PathVariable String key, @RequestBody WorkspaceUpdateRequest request) {
    final var workspace = WorkspaceUpdateRequest.toEntity(request);
    workspaceService.update(key, workspace);
  }

  @GetMapping()
  @ResponseStatus(HttpStatus.OK)
  public List<WorkspaceResponse> findAllByMemberEmail(@RequestParam("memberEmail") @Email String memberEmail) {
    return this.workspaceService.findAllByMemberEmail(memberEmail)
                                .stream()
                                .map(WorkspaceResponse::from)
                                .collect(Collectors.toList());
  }

}
