package com.prgrms.be02slack.channel.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.prgrms.be02slack.channel.controller.dto.ChannelSaveRequest;
import com.prgrms.be02slack.channel.entity.Channel;
import com.prgrms.be02slack.channel.repository.ChannelRepository;
import com.prgrms.be02slack.common.exception.NotFoundException;
import com.prgrms.be02slack.common.util.IdEncoder;
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
          false, "testWorkspaceId");
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
      String encodedChannelId = defaultChannelService.create(channelSaveRequest);

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
            () -> defaultChannelService.create(null));
      }
    }
  }

  @Nested
  @DisplayName("WorkspaceId에 해당하는 workspace 가 존재하지 않는 경우")
  class ContextWithNotExistWorkspace {

    @Test
    @DisplayName("NotFoundException 에러를 발생시킨다")
    void ItThrowsNotfoundException() {
      //given
      ChannelSaveRequest channelSaveRequest = new ChannelSaveRequest("testName", "testDescription",
          false, "testWorkspaceId");

      given(idEncoder.decode(anyString()))
          .willReturn(1L);
      given(workspaceRepository.findById(anyLong()))
          .willReturn(Optional.empty());

      //when, then
      assertThrows(NotFoundException.class,
          () -> defaultChannelService.create(channelSaveRequest));
    }
  }
}