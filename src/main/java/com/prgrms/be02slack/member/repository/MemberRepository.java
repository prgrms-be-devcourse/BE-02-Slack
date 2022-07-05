package com.prgrms.be02slack.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.workspace.entity.Workspace;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmailAndWorkspace(String email, Workspace workspace);

  Optional<Member> findByNameAndWorkspace_Id(String name, Long workspaceId);

  boolean existsByNameAndWorkspace_Id(String name, Long workspaceId);

  boolean existsByEmailAndWorkspace_Id(String email, Long workspaceId);

  Optional<Member> findByEmail(String email);

  Optional<Member> findByIdAndWorkspace_id(Long id, Long workspaceId);

  List<Member> findAllByWorkspace_id(long workspaceId);
}
