package com.prgrms.be02slack.subscribeInfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.subscribeInfo.entity.SubscribeInfo;

public interface SubscribeInfoRepository extends JpaRepository<SubscribeInfo, Long> {
}
