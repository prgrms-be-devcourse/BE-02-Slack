package com.prgrms.be02slack.directmessagechannel.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.be02slack.directmessagechannel.controller.dto.DirectMessageChannelResponse;
import com.prgrms.be02slack.directmessagechannel.entity.DirectMessageChannel;
import com.prgrms.be02slack.directmessagechannel.service.DirectMessageChannelService;
import com.prgrms.be02slack.member.entity.Member;
import com.prgrms.be02slack.member.entity.Role;
import com.prgrms.be02slack.util.ControllerSetUp;
import com.prgrms.be02slack.util.WithMockCustomLoginMember;
import com.prgrms.be02slack.workspace.entity.Workspace;

@WebMvcTest(controllers = DirectMessageChannelApiController.class)
@MockBeans({@MockBean(JpaMetamodelMappingContext.class)})
@AutoConfigureRestDocs
public class DirectMessageChannelApiControllerTest extends ControllerSetUp {

  private static final String DM_CHANNEL_URL =
      "/api/v1/workspaces/testWorkspaceId/directMessageChannels";

  @Autowired
  private MockMvc mockMvc;
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
      @WithMockCustomLoginMember
      void itReturnDirectMessageChannelId() throws Exception {

        //given
        final String validReceiverEmail = "test@test.test";
        final String url = DM_CHANNEL_URL + "?receiverEmail=" + validReceiverEmail;
        final Workspace workspace = Workspace.createDefaultWorkspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);
        final Member member = Member.builder()
            .email("test@test.com")
            .name("test")
            .displayName("test")
            .role(Role.ROLE_USER)
            .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "workspace", workspace);

        given(directMessageChannelService.create("test", "test", member))
            .willReturn("testId");

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.post(url);

        final ResultActions response = mockMvc.perform(request);
        //then
        verify(directMessageChannelService).create(anyString(), anyString(), any());
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
      @ValueSource(strings = {"\t", " "})
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

  @Nested
  @DisplayName("getChannels 메서드는 테스트 할 때")
  class DescribeGetChannels {

    @Nested
    @DisplayName("유효한 workspaceId와 Member객체를 인자로 받으면")
    @WithMockCustomLoginMember
    class ContextValidRequest {

      @Test
      @DisplayName("200ok와 DM채널 리스트를 반환한다.")
      void itReturnOkAndDMChannelList() throws Exception {

        //given
        final DirectMessageChannelResponse firstResponse =
            new DirectMessageChannelResponse("testName","testId");
        final DirectMessageChannelResponse secondResponse =
            new DirectMessageChannelResponse("testName","testId");

        final List<DirectMessageChannelResponse> responseDto = List.of(firstResponse, secondResponse);


        given(directMessageChannelService.getChannels(any())).willReturn(responseDto);

        //when
        final MockHttpServletRequestBuilder request =
            RestDocumentationRequestBuilders.get(DM_CHANNEL_URL);

        final ResultActions response = mockMvc.perform(request);

        //then
        verify(directMessageChannelService).getChannels(any());
        response.andExpect(status().isOk())
            .andDo(document("Get DirectMessageChannels",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("[].memberName")
                        .type(JsonFieldType.STRING).description("상대 이름"),
                    fieldWithPath("[].encodedDirectMessageChannelId")
                        .type(JsonFieldType.STRING).description("채널 ID")
                )
            ));
      }
    }
  }
}
