package com.prgrms.be02slack.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.channel.entity.Channel;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
  boolean existsByWorkspaceAndName(Long workspaceId, String name);
}
