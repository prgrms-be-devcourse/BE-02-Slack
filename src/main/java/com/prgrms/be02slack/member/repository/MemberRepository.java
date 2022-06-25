package com.prgrms.be02slack.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.workspace.entity.Workspace;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmailAndWorkspace(String email, Workspace workspace);

  Optional<Member> findByNameAndWorkspace(String name, Workspace workspace);
}
