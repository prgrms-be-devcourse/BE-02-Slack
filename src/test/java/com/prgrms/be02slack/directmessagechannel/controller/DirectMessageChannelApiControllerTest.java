package com.prgrms.be02slack.directmessagechannel.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.common.configuration.security.SecurityConfig;
import com.prgrms.be02slack.directmessagechannel.service.DirectMessageChannelService;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.util.ControllerSetUp;

@WebMvcTest(controllers = DirectMessageChannelApiController.class)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
public class DirectMessageChannelApiControllerTest extends ControllerSetUp {

  private static final String DM_CHANNEL_URL =
      "/api/v1/workspaces/testWorkspaceId/directMessageChannels";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private DirectMessageChannelService directMessageChannelService;

  @Nested
  @DisplayName("create 메서드는 테스트 할 때")
  class DescribeCreate {

    @Nested
    @DisplayName("유효한 워크스페이스 id와 유효한 memberEmail을 인자로 받으면")
    class ContextValidWorkspaceAndValidMemberEmail {

      @Test
      @DisplayName("다이렉트 메세지 채널 ID를 반환한다.")
      void itReturnDirectMessageChannelId() throws Exception {

        //given
        final String validReceiverEmail = "test@test.test";
        final String url = DM_CHANNEL_URL + "?receiverEmail=" + validReceiverEmail;

        given(directMessageChannelService.create("test", "test"))
            .willReturn("testId");

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(url);

        final ResultActions response = mockMvc.perform(request);
        //then
        verify(directMessageChannelService).create(anyString(), anyString());
        response.andExpect(status().isOk())
            .andDo(document("Create DirectMessageChannel",
                requestParameters(
                    parameterWithName("receiverEmail").description("receiver email")
                )
            ));
      }
    }

    @Nested
    @DisplayName("빈값의 워크스페이스 id를 받으면")
    class ContextBlankWorkspaceId {

      @ParameterizedTest
      @ValueSource(strings = {"\t", "\n", " "})
      @DisplayName("BadRequest를 반환한다.")
      void itReturnBadRequest(String workspaceId) throws Exception {
        //given
        final String validEmail = "test@test.test";
        final String url = "/api/v1/workspaces/" + workspaceId +
            "/directMessageChannels" + "?receiverEmail=" + validEmail;

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(url);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("빈값의 email 을 받으면")
    class ContextBlankEmail {

      @ParameterizedTest
      @ValueSource(strings = {"\t", "\n", " "})
      @DisplayName("BadRequest를 반환한다.")
      void itReturnBadRequest(String receiverEmail) throws Exception {
        //given
        final String workspaceId = "testId";
        final String url = "/api/v1/workspaces/" + workspaceId +
            "/directMessageChannels" + "?receiverEmail=" + receiverEmail;

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(url);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }

    @Nested
    @DisplayName("email형식이 아닌 receiverEmail 을 받으면")
    class ContextInvalidEmail {

      @ParameterizedTest
      @ValueSource(strings = {"hello", "@hello", "hello.test"})
      @DisplayName("BadRequest를 반환한다.")
      void itReturnBadRequest(String receiverEmail) throws Exception {
        //given
        final String workspaceId = "testId";
        final String url = "/api/v1/workspaces/" + workspaceId +
            "/directMessageChannels" + "?receiverEmail=" + receiverEmail;

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(url);

        final ResultActions response = mockMvc.perform(request);

        //then
        response.andExpect(status().isBadRequest());
      }
    }
  }
}
