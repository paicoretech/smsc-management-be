package com.smsc.management.app.dnd.processor;

import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.model.repository.DndEntryMsisdnRepository;
import com.smsc.management.app.dnd.service.DndScyllaService;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.smsc.management.app.dnd.utils.TestFileGenerator.createDndName;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

class CsvDndFileProcessorTest extends BaseIntegrationTest {
    @Mock
    private DndEntryMsisdnRepository dndEntryMsisdnRepository;

    @Mock
    private DndScyllaService dndScyllaService;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private CsvDndFileProcessor processor;

    @BeforeEach
    void setUp() {
        DndEntryListRepository dndEntryListRepository = mock(DndEntryListRepository.class);
        when(appProperties.getLoadCsvBatchSize()).thenReturn(2);
        processor = new CsvDndFileProcessor(dndEntryListRepository, dndEntryMsisdnRepository, appProperties, dndScyllaService);
    }

    @Test
    @DisplayName("process when blank or empty lines exist then skips them and processes valid lines")
    void processWhenBlankOrEmptyLinesExistThenSkipsThemAndProcessesValidLines() {
        when(appProperties.getMsisdnRegex()).thenReturn("\\d+");
        String content = "msisdn\n\n \n123456\n";
        DndEntryList dndEntryList = createDndName(DndType.NETWORK_ID, "value", "test");

        processor.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), dndEntryList);

        verify(dndEntryMsisdnRepository).saveAllAndFlush(anyList());
        verify(dndScyllaService).insertDndEntry(anyInt(), anyString(), anyString(), eq("123456"));
    }

    @Test
    @DisplayName("process when saveAll throws exception then handles gracefully")
    void processWhenSaveAllThrowsExceptionThenHandlesGracefully() {
        String content = "msisdn\n789456\n";
        DndEntryList dndEntryList = createDndName(DndType.NETWORK_ID, "value", "test");

        doThrow(new RuntimeException("save fail")).when(dndEntryMsisdnRepository).saveAll(anyList());

        processor.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), dndEntryList);
    }

    @Test
    @DisplayName("process when CSV read fails then does not attempt save")
    void processWhenCsvReadFailsThenDoesNotAttemptSave() {
        InputStream invalidStream = new InputStream() {
            @Override
            public int read() {
                throw new RuntimeException("simulated read failure");
            }
        };

        DndEntryList dndEntryList = createDndName(DndType.NETWORK_ID, "value", "test");
        processor.process(invalidStream, dndEntryList);

        verify(dndEntryMsisdnRepository, org.mockito.Mockito.never()).saveAll(anyList());
    }
}
