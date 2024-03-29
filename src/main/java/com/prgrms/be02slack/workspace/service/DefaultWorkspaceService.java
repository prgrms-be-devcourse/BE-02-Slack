package com.prgrms.be02slack.workspace.service;

import static org.apache.logging.log4j.util.Strings.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.repository.WorkspaceRepository;

@Service
@Transactional
public class DefaultWorkspaceService implements WorkspaceService {

  private final IdEncoder idEncoder;
  private final WorkspaceRepository workspaceRepository;

  public DefaultWorkspaceService(
      IdEncoder idEncoder,
      WorkspaceRepository workspaceRepository
  ) {
    this.idEncoder = idEncoder;
    this.workspaceRepository = workspaceRepository;
    ;
  }

  @Override
  public Workspace create() {
    final var workspace = Workspace.createDefaultWorkspace();
    final var createdWorkspace = workspaceRepository.save(workspace);
    createdWorkspace.makeDefaultUrl();

    return createdWorkspace;
  }

  @Override
  public void update(String key, Workspace updateWorkspace) {
    Assert.isTrue(isNotBlank(key), "Key must be provided");
    Assert.notNull(updateWorkspace, "Workspace must be provided");

    final var id = idEncoder.decode(key);

    final var saved = workspaceRepository.findById(id)
                                         .orElseThrow(
                                             () -> new NotFoundException("Workspace not found"));

    saved.update(updateWorkspace);
  }

  @Override
  public Workspace findByKey(String key) {
    Assert.isTrue(isNotBlank(key), "Key must be provided");

    final var id = idEncoder.decode(key);

    return workspaceRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Workspace not found"));
  }

  @Override
  public List<Workspace> findAllByMemberEmail(String memberEmail) {
    Assert.isTrue(isNotBlank(memberEmail), "Member Email must be provided");

    final var foundWorkspaces = this.workspaceRepository.findAllByMemberEmail(memberEmail);

    Assert.notEmpty(foundWorkspaces, "email with no matching member");

    return foundWorkspaces;
  }
}
