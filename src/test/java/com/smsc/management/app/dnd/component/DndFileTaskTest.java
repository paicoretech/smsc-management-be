package com.smsc.management.app.dnd.component;

import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.processor.DndFileProcessor;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static com.smsc.management.app.dnd.utils.TestFileGenerator.createDndName;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DndFileTaskTest {
    @Mock
    private DndFileProcessor processor;

    @Mock
    private DndEntryListRepository dndEntryListRepository;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private DndFileTask task;

    @BeforeEach
    void setUp() {
        task = new DndFileTask(List.of(processor), dndEntryListRepository, appProperties);
    }

    @Test
    @DisplayName("Registry Network ID DND when all ok")
    void testStartProcessingWhenFileExistsThenDoSuccessfully() throws IOException {
        File tempFile = File.createTempFile("data", ".csv");
        String destinationDir = System.getProperty("file.upload.dir", "/tmp/dnd/uploads/");
        File finalPath = this.createFileData(destinationDir, tempFile);
        String filename = tempFile.getName();
        DndEntryList dndEntryList = createDndName(DndType.NETWORK_ID, "value", "test");

        when(appProperties.getUploadFileDir()).thenReturn(destinationDir);
        when(processor.supports(filename)).thenReturn(true);
        task.startProcessing(filename, dndEntryList);
        toSleep();

        verify(processor).process(any(InputStream.class), eq(dndEntryList));
        assertEquals(DndStatus.CREATING, dndEntryList.getStatus());
        finalPath.deleteOnExit();
    }

    @Test
    @DisplayName("Start processing dnd file when extension is invalid")
    void testStartProcessingWhenExtensionIsInvalidThenStatusDNDIsFailed() throws IOException {
        File tempFile = File.createTempFile("data", ".csv");
        String destinationDir = System.getProperty("file.upload.dir", "/tmp/dnd/uploads/");
        File finalPath = this.createFileData(destinationDir, tempFile);
        String filename = tempFile.getName();
        DndEntryList dndEntryList = createDndName(DndType.SENDER, "alpha", "fail-file");

        when(appProperties.getUploadFileDir()).thenReturn(destinationDir);
        when(processor.supports(filename)).thenReturn(false);

        task.startProcessing(filename, dndEntryList);
        toSleep();

        assertEquals(DndStatus.FAILED, dndEntryList.getStatus());
        verify(dndEntryListRepository).save(dndEntryList);
        finalPath.deleteOnExit();
    }

    private static void toSleep() {
        await().atMost(ONE_SECOND).until(() -> true);
    }

    private File createFileData(String destinationDir, File tempFile) throws IOException {
        Files.write(tempFile.toPath(), "1322888089\n1976606028\n".getBytes());
        File targetDir = new File(destinationDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        File finalPath = new File(destinationDir + tempFile.getName());
        Files.move(tempFile.toPath(), finalPath.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return finalPath;
    }
}

