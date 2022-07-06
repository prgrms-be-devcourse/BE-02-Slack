package com.prgrms.be02slack.common.configuration.websocket;

import java.util.Objects;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.prgrms.be02slack.security.DefaultUserDetailsService;
import com.prgrms.be02slack.security.TokenProvider;

@Component
public class StompHandler implements ChannelInterceptor {

  private final TokenProvider tokenProvider;
  private final DefaultUserDetailsService customUserDetailsService;

  public StompHandler(
      TokenProvider tokenProvider,
      DefaultUserDetailsService customUserDetailsService
  ) {
    this.tokenProvider = tokenProvider;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      final var token = Objects.requireNonNull(accessor.getFirstNativeHeader("Authorization"))
          .substring(7);
      tokenProvider.validateToken(token);

      final String email = tokenProvider.getEmailFromToken(token);
      final String encodedWorkspaceId = tokenProvider.getEncodedWorkspaceIdFromToken(token);
      final String tokenPayloadStr = email + " " + encodedWorkspaceId;
      final UserDetails userDetails = customUserDetailsService.loadUserByUsername(tokenPayloadStr);

      if (userDetails == null) {
        throw new AccessDeniedException("Invalid User token");
      }

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
      );
      accessor.setUser(authentication);
    }
    return message;
  }
}
