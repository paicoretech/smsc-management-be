package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.dto.BroadcastDTO;
import com.smsc.management.app.broadcast.mapper.BroadcastMapper;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.registry.FutureRegistry;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.broadcast.utilsTest.Utils;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.StaticMethods;
import org.junit.jupiter.api.Disabled;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastServiceTest {

    @Mock
    private BroadcastRepository broadcastRepository;

    @Mock
    private FutureRegistry futureRegistry;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BroadcastMapper broadcastMapper;

    @Mock
    private BroadcastFileService broadcastFileService;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private AppProperties appProperties;

    @Mock
    private BroadcastSchedulerService broadcastSchedulerService;

    @InjectMocks
    private BroadcastService broadcastService;

    @Test
    @DisplayName("Test broadcast pre-construction when broadcast schedule status exists")
    void testBroadcastWhenBroadcastScheduleStatusExistsThenDoItSuccessfully() {
        Broadcast broadcast = Utils.getBroadcast(true);
        List<Broadcast> broadcastList = new ArrayList<>();
        broadcastList.add(broadcast);
        Mockito.when(broadcastRepository.findByStatus(BroadcastStatus.SCHEDULED)).thenReturn(broadcastList);
        assertDoesNotThrow(() -> broadcastService.broadcast());
    }

    @Test
    void testCancelBroadcastWhenExistsAndCancelled() {
        int broadcastId = 123;
        when(futureRegistry.exists(broadcastId)).thenReturn(true);
        when(futureRegistry.cancel(broadcastId)).thenReturn(true);
        broadcastService.cancelBroadcast(broadcastId);
        verify(futureRegistry).cancel(broadcastId);
    }

    @Test
    void testCancelBroadcastWhenNotExists() {
        int broadcastId = 456;

                when(futureRegistry.exists(broadcastId)).thenReturn(false);
                broadcastService.cancelBroadcast(broadcastId);
                verify(futureRegistry, never()).cancel(anyInt());
        }

        @Test
        @DisplayName("Create should return error response when name already exists")
        void create_ShouldReturnErrorResponse_WhenNameExists() {
                BroadcastDTO newBroadcast = new BroadcastDTO();
                newBroadcast.setName("Duplicate Name");
                newBroadcast.setStartDateTime(LocalDateTime.now().plusHours(1));
                newBroadcast.setMaxExecutionDateTime(LocalDateTime.now().plusHours(2));

                when(broadcastRepository.existsByName(eq("Duplicate Name")))
                                .thenReturn(true);

                ApiResponse response = broadcastService.create(newBroadcast);

                assertEquals(500, response.status());
                assertEquals("error", response.message());
                String expectedComment = "New broadcast request with error (Broadcast name must be unique)";
                assertEquals(expectedComment, response.comment());
        }

        @Test
        @DisplayName("Update should return error response when name exists for another broadcast")
        void update_ShouldReturnErrorResponse_WhenNameExistsForAnother() {
                int broadcastId = 1;
                BroadcastDTO updateDto = new BroadcastDTO();
                updateDto.setName("Existing Name");

                Broadcast currentBroadcast = new Broadcast();
                currentBroadcast.setId(broadcastId);
                currentBroadcast.setStatus(BroadcastStatus.PENDING);

                when(broadcastRepository.findByIdAndStatusNot(broadcastId, BroadcastStatus.DELETED))
                                .thenReturn(Optional.of(currentBroadcast));

                when(broadcastRepository.existsByNameAndIdNot(eq("Existing Name"), eq(broadcastId)))
                                .thenReturn(true);

                ApiResponse response = broadcastService.update(broadcastId, updateDto, false);

                assertEquals(500, response.status());
                assertEquals("error", response.message());
                String expectedComment = "Update broadcast request with error (Broadcast name must be unique)";
                assertEquals(expectedComment, response.comment());
        }

        @Test
        @DisplayName("Clone should find unique name by incrementing counter")
        void clone_ShouldFindUniqueName() {
                Broadcast original = new Broadcast();
                original.setId(1);
                original.setName("Campaign");
                original.setStatus(BroadcastStatus.COMPLETED);

                when(broadcastRepository.findByIdAndStatusNot(1, BroadcastStatus.DELETED))
                                .thenReturn(Optional.of(original));

                when(broadcastRepository.existsByName("Campaign-copy"))
                                .thenReturn(true);
                when(broadcastRepository.existsByName("Campaign-copy-1"))
                                .thenReturn(true);
                when(broadcastRepository.existsByName("Campaign-copy-2"))
                                .thenReturn(false);

                when(broadcastRepository.save(any(Broadcast.class))).thenAnswer(invocation -> {
                        Broadcast b = invocation.getArgument(0);
                        return b;
                });

                broadcastService.cloneBroadcast(1);
        }

        @Test
        @DisplayName("ChangeStatus to DELETED should fail when broadcast is not DRAFT")
        void changeStatus_ShouldFailDeletion_WhenNotDraft() {
                int broadcastId = 1;
                Broadcast broadcast = new Broadcast();
                broadcast.setId(broadcastId);
                broadcast.setStatus(BroadcastStatus.COMPLETED);
                broadcast.setCreatedById(1);

                com.smsc.management.app.broadcast.dto.BroadcastStatusRequestDTO statusRequest = new com.smsc.management.app.broadcast.dto.BroadcastStatusRequestDTO();
                statusRequest.setBroadcastStatus("DELETED");
                statusRequest.setComment("Test deletion");

                when(broadcastRepository.findById(broadcastId))
                                .thenReturn(Optional.of(broadcast));

                ApiResponse response = broadcastService.changeStatus(broadcastId, statusRequest);

                assertEquals(500, response.status());
                assertTrue(response.comment().contains("Only DRAFT campaigns can be deleted"));
        }


    @Test
    @Disabled("Requires Mockito Inline for Static Mocking of StaticMethods, which conflicts with project PowerMock")
    void createBroadcast_WithMatchingSenderId_ShouldSucceed() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        Users user = new Users();
        user.setSenderIds(List.of("REQUIRED_ID"));
        user.setAllServiceProviders(true);
        user.setRoles(new ArrayList<>());
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        BroadcastDTO dto = new BroadcastDTO();
        dto.setSenderId("REQUIRED_ID");
        dto.setNetworkId(1);
        dto.setFileId(100);
        dto.setStartDateTime(LocalDateTime.now().plusHours(1));
        dto.setMaxExecutionDateTime(LocalDateTime.now().plusHours(2));

        when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt()))
                .thenReturn(true);
        when(broadcastFileService.getFileById(anyInt())).thenReturn(new BroadcastFile());
        when(broadcastMapper.toEntity(any(BroadcastDTO.class))).thenReturn(new Broadcast());
        when(broadcastRepository.save(any(Broadcast.class))).thenReturn(new Broadcast());
        when(appProperties.getUploadBroadcastDir()).thenReturn("/tmp");

        try (MockedStatic<StaticMethods> mockedStatic = Mockito.mockStatic(StaticMethods.class)) {
            mockedStatic.when(() -> StaticMethods.getInputStreamFromFile(any(), any()))
                    .thenReturn(new ByteArrayInputStream(new byte[0]));

            var response = broadcastService.create(dto);

            assertTrue(response.status() == 200, "Expected success response but got: " + response.comment());
        }
    }

    @Test
    void createBroadcast_WithMismatchingSenderId_ShouldFail() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        Users user = new Users();
        user.setSenderIds(List.of("REQUIRED_ID"));
        user.setAllServiceProviders(true);
        user.setRoles(new ArrayList<>());
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        BroadcastDTO dto = new BroadcastDTO();
        dto.setSenderId("WRONG_ID");
        dto.setNetworkId(1);
        dto.setStartDateTime(LocalDateTime.now().plusHours(1));
        dto.setMaxExecutionDateTime(LocalDateTime.now().plusHours(2));

        when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt()))
                .thenReturn(true);

        var response = broadcastService.create(dto);

        assertTrue(response.status() != 200);
        assertTrue(response.comment().contains("Invalid Sender ID"));
    }

    @Test
    @Disabled("Requires Mockito Inline for Static Mocking of StaticMethods, which conflicts with project PowerMock")
    void createBroadcast_WithNoSenderIdRestriction_ShouldSucceed() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("freeuser");

        Users user = new Users();
        user.setSenderIds(null);
        user.setAllServiceProviders(true);
        user.setRoles(new ArrayList<>());
        when(userRepository.findByUserName("freeuser")).thenReturn(Optional.of(user));

        BroadcastDTO dto = new BroadcastDTO();
        dto.setSenderId("ANY_ID");
        dto.setNetworkId(1);
        dto.setFileId(100);
        dto.setStartDateTime(LocalDateTime.now().plusHours(1));
        dto.setMaxExecutionDateTime(LocalDateTime.now().plusHours(2));

        when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt()))
                .thenReturn(true);
        when(broadcastFileService.getFileById(anyInt())).thenReturn(new BroadcastFile());
        when(broadcastMapper.toEntity(any(BroadcastDTO.class))).thenReturn(new Broadcast());
        when(broadcastRepository.save(any(Broadcast.class))).thenReturn(new Broadcast());
        when(appProperties.getUploadBroadcastDir()).thenReturn("/tmp");

        try (MockedStatic<StaticMethods> mockedStatic = Mockito.mockStatic(StaticMethods.class)) {
            mockedStatic.when(() -> StaticMethods.getInputStreamFromFile(any(), any()))
                    .thenReturn(new ByteArrayInputStream(new byte[0]));

            var response = broadcastService.create(dto);

            assertTrue(response.status() == 200, "Expected success response but got: " + response.comment());
        }
    }
}