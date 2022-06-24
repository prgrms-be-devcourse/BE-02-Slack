package com.prgrms.be02slack.workspace.service;

import com.prgrms.be02slack.workspace.entity.Workspace;

public interface WorkspaceService {

  void update(String key, Workspace updateWorkspace);
}
