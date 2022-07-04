package com.prgrms.be02slack.message.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.prgrms.be02slack.message.controller.dto.MessageWebsocketRequest;
import com.prgrms.be02slack.message.controller.dto.MessageWebsocketResponse;
import com.prgrms.be02slack.message.service.MessageService;

@Validated
@Controller
public class MessageWebsocketController {

  private final Logger log = LoggerFactory.getLogger(MessageWebsocketController.class);
  private final MessageService messageService;

  public MessageWebsocketController(MessageService messageService) {
    this.messageService = messageService;
  }

  @MessageMapping("/channel.{encodedChannelId}")
  @SendTo("/topic/channel.{encodedChannelId}")
  public MessageWebsocketResponse sendMessage(
      @DestinationVariable String encodedChannelId,
      @Valid MessageWebsocketRequest channelMessageRequest
  ) {
    log.info("channel id : {}, content : {}", encodedChannelId, channelMessageRequest.getContent());

    final var sendMessage = messageService.sendMessage(encodedChannelId,
        channelMessageRequest.getContent());

    return MessageWebsocketResponse.from(sendMessage);
  }

  @MessageExceptionHandler(RuntimeException.class)
  @SendToUser("/queue.error")
  public MessageWebsocketResponse handleException(RuntimeException e) {
    log.info(e.toString());
    return MessageWebsocketResponse.error(e.getMessage());
  }
}
