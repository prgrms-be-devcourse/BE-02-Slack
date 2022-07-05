package com.prgrms.be02slack.channel.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.channel.controller.dto.ChannelResponse;
import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.controller.dto.InviteRequest;
import com.prgrms.be02slack.channel.exception.NameDuplicateException;
import com.prgrms.be02slack.channel.service.ChannelService;
import com.prgrms.be02slack.common.dto.AuthResponse;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.util.ControllerSetUp;
import com.prgrms.be02slack.util.WithMockCustomLoginMember;

@WebMvcTest(
    controllers = ChannelApiController.class
)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
class ChannelApiControllerTest extends ControllerSetUp {
  private static final String API_URL = "/api/v1/";
  private static final String CREATE_CHANNEL_URL = API_URL + "workspaces/testWorkspaceId/channels";

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ChannelService channelService;

  static class NameSourceOutOfRange implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(""),
          Arguments.of("\t"),
          Arguments.of("\n"),
          Arguments.of("a".repeat(81))
      );
    }
  }

  static class InviteeInfosSourceNullOrEmpty implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(new HashSet<>())
      );
    }
  }

  static class TokenSourceBlank implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of((Object)null),
          Arguments.of(""),
          Arguments.of("\t"),
          Arguments.of("\n")
      );
    }
  }

  @Nested
  @DisplayName("create 메서드는")
  class DescribeCreate {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {
      @Test
      @DisplayName("인코딩된 id를 응답한다")
      void ItResponseOk() throws Exception {
        //given
        given(channelService.create(anyString(), any(ChannelSaveRequest.class)))
            .willReturn("testId");

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "workspaces/{workspaceId}/channels", "testWorkspaceId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).create(anyString(), any(ChannelSaveRequest.class));
        response.andExpect(status().isOk())
            .andDo(document("Create channel",
                pathParameters(
                    parameterWithName("workspaceId").description("workspace id")
                ),
                requestFields(
                    fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("channel name"),
                    fieldWithPath("description")
                        .type(JsonFieldType.STRING)
                        .description("channel description"),
                    fieldWithPath("isPrivate")
                        .type(JsonFieldType.BOOLEAN)
                        .description("whether the channel is open to the public")
                )
            ));
      }
    }

    @Nested
    @DisplayName("name 의 길이가 범위를 벗어나면")
    class ContextWithNameOutOfRange {

      @ParameterizedTest
      @ArgumentsSource(NameSourceOutOfRange.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String name) throws Exception {
        //given
        HashMap<Object, Object> requestMap = new HashMap<>();
        requestMap.put("name", name);
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                CREATE_CHANNEL_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("name 이 멤버 이름 또는 다른 채널 이름과 중복이라면")
    class ContextWithNameIsDuplicated {

      @Test
      @DisplayName("Conflict 를 응답한다")
      void ItResponseConflict() throws Exception {
        //given
        HashMap<Object, Object> requestMap = new HashMap<>();
        requestMap.put("name", "testName");
        requestMap.put("description", "testDescription");
        requestMap.put("isPrivate", false);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        given(channelService.create(anyString(), any(ChannelSaveRequest.class)))
            .willThrow(new NameDuplicateException());

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                CREATE_CHANNEL_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isConflict());
      }
    }
  }

  @Nested
  @DisplayName("invite 메서드는")
  class DescribeInvite {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {
      @Test
      @DisplayName("Ok 를 응답한다")
      void ItResponseOk() throws Exception {
        //given
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("sender", "testSenderName");
        requestMap.put("inviteeInfos", Set.of("name1", "test@gmail.com"));

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "/workspaces/{workspaceId}/channels/{channelId}/invite",
                "workspaceId", "channelId"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).invite(anyString(), anyString(), any(InviteRequest.class));
        response.andExpect(status().isOk())
            .andDo(document("Invite to channel",
                pathParameters(
                    parameterWithName("workspaceId").description("workspace id"),
                    parameterWithName("channelId").description("channel id")
                ),
                requestFields(
                    fieldWithPath("sender")
                        .type(JsonFieldType.STRING)
                        .description("invitation sender name"),
                    fieldWithPath("inviteeInfos")
                        .type(JsonFieldType.ARRAY)
                        .description("list of invitees")
                )
            ));
      }
    }

    @Nested
    @DisplayName("sender 의 길이가 범위를 벗어나면")
    class ContextWithSenderOutOfRange {

      @ParameterizedTest
      @ArgumentsSource(NameSourceOutOfRange.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String sender) throws Exception {
        //given
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("sender", sender);
        requestMap.put("inviteeInfos", Set.of("name1", "test@gmail.com"));

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "/workspaces/{workspaceId}/channels/{channelId}/invite",
                "workspaceId", "channelId"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("inviteeInfos 가 null 이거나 빈 리스트라면")
    class ContextWithInviteeInfosNullOrEmpty {

      @ParameterizedTest
      @ArgumentsSource(InviteeInfosSourceNullOrEmpty.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(Set<Object> inviteeInfos) throws Exception {
        //given
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("sender", "sender");
        requestMap.put("inviteeInfos", inviteeInfos);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "/workspaces/{workspaceId}/channels/{channelId}/invite",
                "workspaceId", "channelId"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("초대를 보낼 수 없는 사람이 한명이라도 있다면")
    class ContextWithCantSendInvitation {

      @Test
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest() throws Exception {
        //given
        doThrow(IllegalArgumentException.class)
            .when(channelService).invite(anyString(), anyString(), any(InviteRequest.class));

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("sender", "sender");
        requestMap.put("inviteeInfos", Set.of("name1", "test@gmail.com"));

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "/workspaces/{workspaceId}/channels/{channelId}/invite",
                "workspaceId", "channelId"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @DisplayName("participate 메서드는")
  class DescribeParticipate {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {
      @Test
      @DisplayName("AuthResponse 를 응답한다")
      void ItResponseAuthResponse() throws Exception {
        //given
        given(channelService.participate(anyString(), anyString(), anyString()))
            .willReturn(new AuthResponse("token"));

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("token", "token");

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "/workspaces/{workspaceId}/channels/{channelId}/participate?token={token}",
                "workspaceId", "channelId", "token"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).participate(anyString(), anyString(), anyString());
        response.andExpect(status().isOk())
            .andDo(document("Participate to channel",
                pathParameters(
                    parameterWithName("workspaceId").description("workspace id"),
                    parameterWithName("channelId").description("channel id")
                ),
                requestFields(
                    fieldWithPath("token")
                        .type(JsonFieldType.STRING)
                        .description("login token")
                ),
                responseFields(
                    fieldWithPath("token").type(JsonFieldType.STRING).description("token"),
                    fieldWithPath("tokenType").type(JsonFieldType.STRING)
                        .description("token type")
                )));
      }
    }

    @Nested
    @DisplayName("token 이 null 이거나 빈 값 또는 공백이라면")
    class ContextWithTokenBlank {

      @ParameterizedTest
      @ArgumentsSource(TokenSourceBlank.class)
      @DisplayName("BadRequest 를 응답한다")
      void ItResponseBadRequest(String token) throws Exception {
        //given
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("token", token);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
                API_URL + "/workspaces/{workspaceId}/channels/{channelId}/invite",
                "workspaceId", "channelId"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

        ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @WithMockCustomLoginMember
  @DisplayName("findAllByMember 메서드는")
  class DescribeFindAllByMember {

    @Test
    @DisplayName("멤버가 구독한 채널들을 응답한다")
    void ItResponseSubscribedChannels() throws Exception {
      //given
      given(channelService.findAllByMember(any(Member.class)))
          .willReturn(List.of(new ChannelResponse("SDFASD", "testName", false)));

      //when
      MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.get(
          API_URL + "/workspaces/{workspaceId}/channels",
          "workspaceId"
      );

      ResultActions response = mockMvc.perform(request);

      //then
      verify(channelService).findAllByMember(any(Member.class));
      response.andExpect(status().isOk())
          .andDo(document("Look up channel",
              pathParameters(
                  parameterWithName("workspaceId").description("workspace id")
              ),
              responseFields(
                  fieldWithPath("[].id").type(JsonFieldType.STRING).description("channel id"),
                  fieldWithPath("[].name").type(JsonFieldType.STRING)
                      .description("channel name"),
                  fieldWithPath("[].isPrivate").type(JsonFieldType.BOOLEAN)
                      .description("whether the channel is open or not")
              )));
    }
  }

  @Nested
  @WithMockCustomLoginMember
  @DisplayName("leave 메서드는")
  class DescribeLeave {

    @Nested
    @DisplayName("유효한 값이 전달되면")
    class ContextWithValidData {
      @Test
      @DisplayName("Ok 를 응답한다")
      void ItResponseOK() throws Exception {
        //given
        //when
        MockHttpServletRequestBuilder request = RestDocumentationRequestBuilders.post(
            API_URL + "/workspaces/{workspaceId}/channels/{channelId}/leave",
            "workspaceId", "channelId"
        );

        ResultActions response = mockMvc.perform(request);

        //then
        verify(channelService).leave(anyString(), any(Member.class));
        response.andExpect(status().isOk())
            .andDo(document("Leave channel",
                pathParameters(
                    parameterWithName("workspaceId").description("workspace id"),
                    parameterWithName("channelId").description("channel id")
                )));
      }
    }
  }
}
