package com.smsc.management.app.ss7.service;

import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaDTO;
import com.smsc.management.app.ss7.dto.M3uaSocketsDTO;
import com.smsc.management.app.ss7.mapper.M3uaMapperImpl;
import com.smsc.management.app.ss7.model.entity.M3ua;
import com.smsc.management.app.ss7.model.entity.M3uaAssociations;
import com.smsc.management.app.ss7.model.entity.M3uaSockets;
import com.smsc.management.app.ss7.model.repository.M3uaAssociationsRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRepository;
import com.smsc.management.app.ss7.model.repository.M3uaSocketsRepository;
import com.smsc.management.app.ss7.utilsTest.Utils;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class M3uaServiceTest {
    @InjectMocks
    private M3uaService m3uaService;

    @Mock
    private M3uaRepository m3uaRepository;

    @Mock
    private M3uaSocketsRepository m3uaSocketsRepository;

    @Mock
    private M3uaAssociationsRepository m3uaAssociationsRepository;

    @InjectMocks
    private M3uaMapperImpl m3uaMapper;

    @Mock
    private UtilsBase utilsBase;

    @Test
    void propagateStatusForM3uaTest() {
        M3uaDTO m3uaDTOMock = Utils.getM3uatDtoMock();
        M3ua m3uaEntity = m3uaMapper.toEntity(m3uaDTOMock);

        M3uaSocketsDTO m3uaSocketsDTOMock = Utils.getM3uaSocketsMock();
        M3uaSockets m3uaSocketsEntity = m3uaMapper.toEntityServer(m3uaSocketsDTOMock);

        M3uaAssociationsDTO m3uaAssociationsDTOMock = Utils.getM3uaAssociationsDTO();
        M3uaAssociations m3uaAssociationsEntity = m3uaMapper.toEntityAssociation(m3uaAssociationsDTOMock);

        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(m3uaEntity);
        Mockito.when(m3uaSocketsRepository.findBySs7M3uaId(anyInt())).thenReturn(List.of(m3uaSocketsEntity));
        Mockito.when(m3uaAssociationsRepository.findByM3uaSocketId(anyInt())).thenReturn(List.of(m3uaAssociationsEntity));

        Assertions.assertDoesNotThrow(() -> m3uaService.propagateStatusForM3ua(1, 1));

        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(null);
        Assertions.assertDoesNotThrow(() -> m3uaService.propagateStatusForM3ua(1, 1));
    }
}