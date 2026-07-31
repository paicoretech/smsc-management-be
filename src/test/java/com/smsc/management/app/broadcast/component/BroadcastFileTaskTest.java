package com.smsc.management.app.broadcast.component;

import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastFileRepository;
import com.smsc.management.app.broadcast.processor.CsvBroadcastFileProcessor;
import com.smsc.management.app.broadcast.processor.ExcelBroadcastFileProcessor;
import com.smsc.management.app.broadcast.service.BroadcastFileService;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.broadcast.utils.CopyManagerFactory;
import com.smsc.management.app.broadcast.utilsTest.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastFileTaskTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private CopyManager copyManager;

    @Mock
    private CopyManagerFactory copyManagerFactory;

    @InjectMocks
    private BroadcastFileTask broadcastFileTask;

    @Mock
    private BroadcastFileService broadcastFileService;

    @Mock
    private BroadcastFileRepository broadcastFileRepository;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        CsvBroadcastFileProcessor csvBroadcastFileProcessor = new CsvBroadcastFileProcessor(broadcastFileService, jdbcTemplate);
        ExcelBroadcastFileProcessor excelBroadcastFileProcessor = new ExcelBroadcastFileProcessor(broadcastFileService, jdbcTemplate);
        ReflectionTestUtils.setField(broadcastFileTask, "fileProcessors", List.of(csvBroadcastFileProcessor, excelBroadcastFileProcessor));
    }

    static Stream<Arguments> csvTestProvider() {
        return Stream.of(
                Arguments.of(
                        Utils.getFileStream(true),
                        "test.csv",
                        BroadcastStatus.CREATED,
                        1,
                        true,
                        ","
                ),
                Arguments.of(
                        Utils.getFileStream(false),
                        "test.csv",
                        BroadcastStatus.CREATED,
                        1,
                        false,
                        ","
                ),
                Arguments.of(
                        new ByteArrayInputStream("sourceAddress,sourceTon,sourceNpi,destinationAddress,destinationTon,destinationNpi,Message\n123".getBytes(StandardCharsets.UTF_8)),
                        "test.csv",
                        BroadcastStatus.FAILED,
                        0,
                        true,
                        ","
                ),
                Arguments.of(
                        new ByteArrayInputStream("sourceAddress,sourceTon,sourceNpi,destinationAddress,destinationTon,destinationNpi,Message\n739769082,1,0,destinationError,0,0,test".getBytes(StandardCharsets.UTF_8)),
                        "test.csv",
                        BroadcastStatus.FAILED,
                        0,
                        true,
                        ","
                ),
                Arguments.of(
                        new ByteArrayInputStream("sender,ston,snpi,destination,dton,dnpi,message,name,cost\n739769082,1,0,9739769082,0,0,test,John Doe,50".getBytes(StandardCharsets.UTF_8)),
                        "broadcast_comma_separated.csv",
                        BroadcastStatus.CREATED,
                        1,
                        true,
                        ","
                )
        );
    }

    @ParameterizedTest
    @MethodSource("csvTestProvider")
    void processFileAsyncTestForCsv(InputStream fileStream, String fileName,
                                    BroadcastStatus expectedStatus, int expectedCount, boolean hasHeaders, String delimiter) {

        assertDoesNotThrow(() ->
                broadcastFileTask.processFileAsync(fileStream, fileName, 1, Utils.getBroadcast(true), 100, hasHeaders, delimiter)
        );

        verify(broadcastFileService).changeStatus(
                eq(1),
                eq(1),
                eq(expectedStatus),
                eq(expectedCount),
                anyString()
        );
    }

    static Stream<Arguments> txtTestProvider() {
        return Stream.of(
                Arguments.of(
                        new ByteArrayInputStream("sender,ston,snpi,destination,dton,dnpi,message,name,cost\n739769082,1,0,9739769082,0,0,test,John Doe,50".getBytes(StandardCharsets.UTF_8)),
                        "broadcast_comma_separated.txt",
                        ",",
                        BroadcastStatus.CREATED,
                        1
                ),
                Arguments.of(
                        new ByteArrayInputStream("sender;ston;snpi;destination;dton;dnpi;message;name;cost\n739769082;1;0;9739769082;0;0;test;John Doe;50".getBytes(StandardCharsets.UTF_8)),
                        "broadcast_semicolon_separated.txt",
                        ";",
                        BroadcastStatus.CREATED,
                        1
                ),
                Arguments.of(
                        new ByteArrayInputStream("sender|ston|snpi|destination|dton|dnpi|message|name|cost\n739769082|1|0|9739769082|0|0|test|John Doe|50".getBytes(StandardCharsets.UTF_8)),
                        "broadcast_pipe_separated.txt",
                        "|",
                        BroadcastStatus.CREATED,
                        1
                ),
                Arguments.of(
                        new ByteArrayInputStream("sender|ston|snpi|destination|dton|dnpi|message|name|cost\n739769082|1|0|9739769082|0|0|test|John Doe|50".getBytes(StandardCharsets.UTF_8)),
                        "broadcast_pipe_wrong_delimiter.txt",
                        ",", // wrong delimiter intentionally
                        BroadcastStatus.FAILED,
                        0
                )
        );
    }

    @ParameterizedTest
    @MethodSource("txtTestProvider")
    void processFileAsyncTestForTxt(InputStream fileStream, String fileName, String delimiter,
                                    BroadcastStatus expectedStatus, int expectedCount) {

        assertDoesNotThrow(() ->
                broadcastFileTask.processFileAsync(fileStream, fileName, 1, Utils.getBroadcast(true), 100, true, delimiter)
        );

        verify(broadcastFileService).changeStatus(
                eq(1),
                eq(1),
                eq(expectedStatus),
                eq(expectedCount),
                anyString()
        );
    }

    @Test
    void processFileAsyncTestForExcel() {
        int totalRow = 100000;
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("broadcast_test_100000.xlsx");
        Broadcast broadcast = new Broadcast();
        broadcast.setId(10);
        broadcast.setColumnMapping("{\"destination\":\"destinationAddr\",\"totalAmount\":\"\",\"message\":\"\",\"date\":\"\"}");
        broadcast.setMessageTemplate("Hello {{destination}} for {{date}} your balance is  {{totalAmount}}");
        broadcast.setFirstRecordMapping("{\"destination\":\"+10698899044\",\"totalAmount\":\"73319.0\",\"message\":\"using is balance our balance your is thanks\",\"date\":\"2024-08-21\"}");
        broadcastFileTask.processFileAsync(inputStream, "broadcast_test_100000.xlsx", 1, broadcast, 2525, true, "");
        ArgumentCaptor<BroadcastStatus> broadcastStatusArgumentCaptor = ArgumentCaptor.forClass(BroadcastStatus.class);
        ArgumentCaptor<Integer> totalMessageArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(broadcastFileService).changeStatus(anyInt(), anyInt(), broadcastStatusArgumentCaptor.capture(), totalMessageArgumentCaptor.capture(), anyString());
        int totalProcessed = totalMessageArgumentCaptor.getValue();
        assertEquals(totalRow, totalProcessed);
        assertEquals(BroadcastStatus.CREATED, broadcastStatusArgumentCaptor.getValue());
    }

    @Test
    @DisplayName("Generating CSV file when file not exists")
    void generateCSVFileWhenFileNotFoundThenDoNothingAndFileStatusIsFailed() {
        BroadcastFile broadcastFile = Utils.getBroadcastFile();
        // before execution
        assertEquals(BroadcastStatus.CREATING, broadcastFile.getStatus());

        broadcastFileTask.generateCSVFile(new File("", ""), 1, broadcastFile);

        verify(broadcastFileRepository).save(broadcastFile);
        // after execution
        assertEquals(BroadcastStatus.FAILED, broadcastFile.getStatus());
    }

    @Test
    void processFileAsyncWhenDestinationAddrTONIsInvalidThenFileStatusIsFailed() {
        broadcastFileTask.processFileAsync(new ByteArrayInputStream("sourceAddress,sourceTon,sourceNpi,destinationAddress,destinationTon,destinationNpi,Message,name,cost\n739769082,1,0,50558499393,4,0,test, John Doe, 30".getBytes()), "test.csv", 1, Utils.getBroadcast(true), 100, true, ",");

        verify(broadcastFileService).changeStatus(eq(1), eq(1), eq(BroadcastStatus.FAILED), eq(0), anyString());
        verify(broadcastFileService).deleteLastLoad(1);
    }

    @Test
    void processFileAsyncWhenDestinationAddrNPIIsInvalidThenFileStatusIsFailed() {
        broadcastFileTask.processFileAsync(new ByteArrayInputStream("sourceAddress,sourceTon,sourceNpi,destinationAddress,destinationTon,destinationNpi,Message,name,cost\n\n739769082,1,0,50558499393,0,3,test, John Doe, 30".getBytes()), "test.csv", 1, Utils.getBroadcast(true), 100, true, ",");
        verify(broadcastFileService).changeStatus(eq(1), eq(1), eq(BroadcastStatus.FAILED), eq(0), anyString());
        verify(broadcastFileService).deleteLastLoad(1);
    }

    @Test
    @DisplayName("generateCSVFile writes data from DB into the CSV file")
    void generateCSVFileWritesDataCorrectly() throws Exception {
        File tempFile = File.createTempFile("broadcast_test", ".csv");
        tempFile.deleteOnExit();

        BroadcastFile broadcastFile = new BroadcastFile();
        broadcastFile.setId(999);
        broadcastFile.setStatus(BroadcastStatus.CREATING);

        when(dataSource.getConnection()).thenReturn(connection);
        when(copyManagerFactory.create(connection)).thenReturn(copyManager);

        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(1);
            writer.write("broadcast_id|message_id\n");
            writer.write("101|msg-001\n");
            writer.flush();
            return null;
        }).when(copyManager).copyOut(anyString(), any(Writer.class));

        broadcastFileTask.generateCSVFile(tempFile, 101, broadcastFile);

        List<String> lines = java.nio.file.Files.readAllLines(tempFile.toPath());
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("broadcast_id"));
        assertEquals(BroadcastStatus.CREATED, broadcastFile.getStatus());

        verify(broadcastFileRepository).save(broadcastFile);
    }

}