package com.prgrms.be02slack.workspace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.prgrms.be02slack.workspace.entity.Workspace;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

  @Query("select ws from Workspace ws "
      + "where ws.id in ( select m.workspace.id from Member m where m.email = :memberEmail)")
  List<Workspace> findAllByMemberEmail(@Param("memberEmail") String memberEmail);
}
