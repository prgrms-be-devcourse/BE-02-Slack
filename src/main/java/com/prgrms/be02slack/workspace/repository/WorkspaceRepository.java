package com.prgrms.be02slack.workspace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.workspace.entity.Workspace;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
