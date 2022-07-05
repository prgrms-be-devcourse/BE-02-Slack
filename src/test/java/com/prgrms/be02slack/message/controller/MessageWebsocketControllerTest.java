package com.prgrms.be02slack.message.controller;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.message.controller.dto.MessageWebsocketRequest;
import com.prgrms.be02slack.message.controller.dto.MessageWebsocketResponse;
import com.prgrms.be02slack.message.entity.Message;
import com.prgrms.be02slack.message.service.MessageService;
import com.prgrms.be02slack.security.DefaultUserDetailsService;
import com.prgrms.be02slack.security.MemberDetails;
import com.prgrms.be02slack.security.TokenProvider;
import com.prgrms.be02slack.workspace.entity.Workspace;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MessageWebsocketControllerTest {

  static final String WS_URI = "ws://localhost:8080/ws-stomp";

  @Autowired
  private TokenProvider tokenProvider;

  @MockBean
  private MessageService messageService;

  @MockBean
  private DefaultUserDetailsService userDetailsService;

  private BlockingQueue<Object> blockingQueue;
  private WebSocketStompClient stompClient;
  private Workspace testWorkspace;
  private Member testMember;

  @BeforeEach
  public void setup() {
    blockingQueue = new LinkedBlockingDeque<>();
    stompClient = new WebSocketStompClient(
        new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));

    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    testWorkspace = Workspace.createDefaultWorkspace();

    testMember = Member.builder()
        .name("test")
        .displayName("test")
        .role(Role.ROLE_USER)
        .workspace(testWorkspace)
        .email("test@test.com")
        .build();
  }

  private StompSessionHandlerAdapter getStompSessionHandlerAdapter() {
    return new StompSessionHandlerAdapter() {
    };
  }

  private StompFrameHandler getStompFrameHandler(Class<?> type) {
    return new StompFrameHandler() {

      @Override
      public Type getPayloadType(StompHeaders headers) {
        return type;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        blockingQueue.add(payload);
      }
    };
  }

  @Nested
  @DisplayName("sendMessage 메서드는")
  class DescribeSendMessage {

    @Nested
    @DisplayName("내용이 있는 메시지가 전달되면")
    class ContextWithMessageRequested {

      @Test
      @DisplayName("해당 메시지를 발행한다")
      void ItPublishMessage() throws Exception {
        //given
        final var memberDetails = MemberDetails.create(testMember);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(memberDetails);

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        final var token = tokenProvider.createMemberToken("test@test.com", "TEST");
        connectHeaders.add("Authorization", "Bearer: " + token);
        StompSession session = stompClient.connect(WS_URI, handshakeHeaders, connectHeaders,
            getStompSessionHandlerAdapter()).get(1, SECONDS);

        String subUrl = MessageFormat.format("/topic/channel.{0}", "TEST");
        session.subscribe(subUrl, getStompFrameHandler(MessageWebsocketResponse.class));

        final var message = Message.builder()
            .encodedChannelId("TEST123")
            .member(testMember)
            .content("test").build();
        given(messageService.sendMessage(anyString(), anyString(), anyString())).willReturn(
            message);

        //when
        String pubUrl = MessageFormat.format("/app/channel.{0}", "TEST");
        session.send(pubUrl, new MessageWebsocketRequest("TEST"));

        //then
        final var expectedResponse = MessageWebsocketResponse.from(message);
        final var resultResponse = (MessageWebsocketResponse)blockingQueue.poll(1, SECONDS);
        assert resultResponse != null;

        assertEquals(expectedResponse.getContent(), resultResponse.getContent());
        assertEquals(expectedResponse.getAuthor().getDisplayName(),
            expectedResponse.getAuthor().getDisplayName());
        assertEquals(expectedResponse.getAuthor().getEmail(),
            expectedResponse.getAuthor().getEmail());
      }
    }

    @Nested
    @DisplayName("내용이 없는 메시지가 전달되면")
    class ContextWithNullMessageRequested {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", " "})
      @DisplayName("에러메시지를 유저에게 반환한다")
      void ItPublishMessage(String src) throws Exception {
        //given
        final var memberDetails = MemberDetails.create(testMember);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(memberDetails);

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        final var token = tokenProvider.createMemberToken("test@test.com", "TEST");
        connectHeaders.add("Authorization", "Bearer: " + token);
        StompSession session = stompClient.connect(WS_URI, handshakeHeaders, connectHeaders,
            getStompSessionHandlerAdapter()).get(1, SECONDS);

        String subUrl = "/user/queue.error";
        session.subscribe(subUrl, getStompFrameHandler(MessageWebsocketResponse.class));

        final var message = Message.builder()
            .encodedChannelId("TEST123")
            .member(testMember)
            .content("test").build();
        given(messageService.sendMessage(anyString(), anyString(), anyString())).willReturn(
            message);

        //when
        String pubUrl = MessageFormat.format("/app/channel.{0}", "TEST");
        session.send(pubUrl, new MessageWebsocketRequest(src));

        //then
        final var resultResponse = (MessageWebsocketResponse)blockingQueue.poll(1, SECONDS);
        assertNotNull(resultResponse);
      }
    }
  }
}
