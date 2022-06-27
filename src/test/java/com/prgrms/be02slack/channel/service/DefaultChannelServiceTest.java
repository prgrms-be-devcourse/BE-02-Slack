package com.prgrms.be02slack.channel.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.common.dto.ApiResponse;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
import com.prgrms.be02slack.member.service.DefaultMemberService;
import com.prgrms.be02slack.workspace.entity.Workspace;
import com.prgrms.be02slack.workspace.repository.WorkspaceRepository;

@ExtendWith(MockitoExtension.class)
class DefaultChannelServiceTest {
  @Mock
  private IdEncoder idEncoder;

  @Mock
  private WorkspaceRepository workspaceRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private DefaultMemberService defaultMemberService;

  @InjectMocks
  private DefaultChannelService defaultChannelService;

  @Nested
  @DisplayName("create 메서드는")
  class DescribeCreate {

    @Test
    @DisplayName("Channel 을 저장하고 인코딩된 id를 반환한다")
    void ItSavesChannelThenReturnsEncodedId() {
      //given
      ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName", "testDescription",
          false);
      Channel channel = Channel.builder()
          .name(channelSaveRequest.getName())
          .description(channelSaveRequest.getDescription())
          .isPrivate(channelSaveRequest.isPrivate())
          .build();
      ReflectionTestUtils.setField(channel, "id", 1L);

      given(idEncoder.decode(anyString()))
          .willReturn(1L);
      given(workspaceRepository.findById(anyLong()))
          .willReturn(Optional.of(Workspace.createDefaultWorkspace()));
      given(channelRepository.save(any(Channel.class)))
          .willReturn(channel);
      given(idEncoder.encode(anyLong()))
          .willReturn("encodedTestId");

      //when
      String encodedChannelId = defaultChannelService.create("workspaceId", channelSaveRequest);

      //then
      verify(idEncoder).decode(anyString());
      verify(workspaceRepository).findById(anyLong());
      verify(channelRepository).save(any(Channel.class));
      verify(idEncoder).encode(anyLong());
      assertThat(encodedChannelId).isNotBlank();
    }

    @Nested
    @DisplayName("channelSaveRequest 파라미터에 null 값이 전달되면")
    class ContextWithChannelSaveRequestNull {

      @Test
      @DisplayName("IllegalArgumentException 에러를 발생시킨다")
      void ItThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.create("workspaceId", null));
      }
    }

    @Nested
    @DisplayName("workspaceId 파라미터에 빈 값이 전달되면")
    class ContextWithNullOrEmptyString {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", "", ""})
      @DisplayName("IllegalArgumentException 에러를 던진다")
      void ItThrowsIllegalArgumentException(String workspaceId) {
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.verifyName(workspaceId, "testName"));
      }
    }

    @Nested
    @DisplayName("WorkspaceId에 해당하는 workspace 가 존재하지 않는 경우")
    class ContextWithNotExistWorkspace {

      @Test
      @DisplayName("NotFoundException 에러를 발생시킨다")
      void ItThrowsNotfoundException() {
        //given
        ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName",
            "testDescription",
            false);

        given(idEncoder.decode(anyString()))
            .willReturn(1L);
        given(workspaceRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when, then
        assertThrows(NotFoundException.class,
            () -> defaultChannelService.create("workspaceId", channelSaveRequest));
      }
    }
  }

  @Nested
  @DisplayName("verifyName 메서드는")
  class DescribeVerifyName {

    @Nested
    @DisplayName("name 이 사용 가능한 이름이라면")
    class ContextWithNameIsValid {

      @Test
      @DisplayName("성공 ApiResponse 를 반환한다")
      void ItReturnsSuccessApiResponse() {
        //given
        given(channelRepository.existsByWorkspaceAndName(anyLong(), anyString()))
            .willReturn(false);
        given(defaultMemberService.isDuplicatedWithOtherMemberName(anyString(), anyString()))
            .willReturn(false);

        //when
        ApiResponse apiResponse = defaultChannelService.verifyName("testWorkspaceId", "testName");

        //then
        assertThat(apiResponse.getResult()).isEqualTo("ok");
        assertThat(apiResponse.getMessage()).isEqualTo("success");
      }
    }

    @Nested
    @DisplayName("workspaceId 파라미터에 빈 값이 전달되면")
    class ContextWithWorkspaceIdNullOrEmptyString {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", "", ""})
      @DisplayName("IllegalArgumentException 에러를 던진다")
      void ItThrowsIllegalArgumentException(String workspaceId) {
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.verifyName(workspaceId, "testName"));
      }
    }

    @Nested
    @DisplayName("name 파라미터에 빈 값이 전달되면")
    class ContextWithNameNullOrEmptyString {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"\n", "\t", "", ""})
      @DisplayName("IllegalArgumentException 에러를 던진다")
      void ItThrowsIllegalArgumentException(String name) {
        assertThrows(IllegalArgumentException.class,
            () -> defaultChannelService.verifyName("testWorkspaceId", name));
      }
    }

    @Nested
    @DisplayName("name 이 같은 워크스페이스내에 다른 채널의 이름과 같다면")
    class ContextWithNameIsDuplicatedWithTheNameOfAnotherChannelInTheSameWorkspace {

      @Test
      @DisplayName("실패 ApiResponse 를 반환한다")
      void ItReturnsFailApiResponse() {
        //given
        given(channelRepository.existsByWorkspaceAndName(anyLong(), anyString()))
            .willReturn(true);

        // when
        ApiResponse apiResponse = defaultChannelService.verifyName("testWorkspaceId", "testName");

        //then
        assertThat(apiResponse.getResult()).isEqualTo("fail");
        assertThat(apiResponse.getMessage())
            .isEqualTo("Name is duplicated with the name of another channel in the same workspace");

      }
    }

    @Nested
    @DisplayName("name 이 같은 워크스페이스내에 다른 멤버의 이름과 같다면")
    class ContextWithNameIsDuplicatedWithTheNameOfAnotherMemberInTheSameWorkspace {

      @Test
      @DisplayName("실패 ApiResponse 를 반환한다")
      void ItReturnsFailApiResponse() {
        //given
        given(channelRepository.existsByWorkspaceAndName(anyLong(), anyString()))
            .willReturn(false);
        given(defaultMemberService.isDuplicatedWithOtherMemberName(anyString(), anyString()))
            .willReturn(true);

        // when
        ApiResponse apiResponse = defaultChannelService.verifyName("testWorkspaceId", "testName");

        //then
        assertThat(apiResponse.getResult()).isEqualTo("fail");
        assertThat(apiResponse.getMessage())
            .isEqualTo("Name is duplicated with the name of another member in the same workspace");

      }
    }
  }
}
