package com.prgrms.be02slack.workspace.service;

import java.util.List;

import com.prgrms.be02slack.workspace.entity.Workspace;

public interface WorkspaceService {

  Workspace create();

  void update(String key, Workspace updateWorkspace);

  Workspace findByKey(String key);

  List<Workspace> findAllByMemberEmail(String memberEmail);
}
