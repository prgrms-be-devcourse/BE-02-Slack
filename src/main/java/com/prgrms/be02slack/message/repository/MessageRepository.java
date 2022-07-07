package com.prgrms.be02slack.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prgrms.be02slack.message.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
