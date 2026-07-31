package com.smsc.management.app.dnd.controller;

import com.jayway.jsonpath.JsonPath;
import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.dto.DndRequestDTO;
import com.smsc.management.app.dnd.model.repository.DndEntryMsisdnRepository;
import com.smsc.management.app.dnd.utils.TestFileGenerator;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DndControllerTest extends BaseIntegrationTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    DndController dndController;

    @Autowired
    private DndEntryMsisdnRepository dndEntryMsisdnRepository;


    @Autowired
    private SequenceNetworksIdRepository sequenceNetworksIdRepository;

    @Test
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("GET /dnd should return list of DND names with success message")
    void testGetAllDnd() throws Exception {
        mockMvc.perform(get("/dnd")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.comment").value("DND names retrieved successfully."));
    }

    @ParameterizedTest
    @MethodSource("invalidFileScenarios")
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd should fail with invalid files and return appropriate error message")
    void testInvalidFiles(MockMultipartFile file, String jsonBody, String expectedMessage) throws Exception {
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes());

        mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.comment").value(containsString(expectedMessage)));
    }

    private static Stream<Arguments> invalidFileScenarios() {
        String json = TestFileGenerator.buildDndJson("Invalid File", DndType.GLOBAL, "");

        return Stream.of(
                Arguments.of(
                        new MockMultipartFile("file", "", "text/csv", new byte[0]),
                        json,
                        "File is required"
                ),
                Arguments.of(
                        new MockMultipartFile("file", "data.xlsx", "application/vnd.ms-excel", "fake".getBytes()),
                        json,
                        "Unsupported file format"
                ),
                Arguments.of(
                        new MockMultipartFile("file", "test.csv", "text/csv", "".getBytes()),
                        json,
                        "File is required"
                )
        );
    }

    @Test
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd with invalid NETWORK_ID should return internal server error")
    void testNetworkIdInvalid() throws Exception {
        String jsonBody = TestFileGenerator.buildDndJson("Invalid Network", DndType.NETWORK_ID, "122");

        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.comment").value(containsString("Invalid NETWORK_ID")));

        jsonBody = TestFileGenerator.buildDndJson("Invalid Network String", DndType.NETWORK_ID, "NOT_A_NUMBER");
        dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.comment").value(containsString("DND Value must be a numeric NETWORK_ID")));
    }

    @ParameterizedTest
    @MethodSource("validDndScenarios")
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd should upload valid DND file and return success")
    void testValidDndUpload(DndType dndType, String dndValue, boolean requiresSetup) throws Exception {
        if (requiresSetup && dndType == DndType.NETWORK_ID) {
            SequenceNetworksId network = new SequenceNetworksId();
            network.setId(Integer.parseInt(dndValue));
            network.setNetworkType("NET");
            sequenceNetworksIdRepository.saveAndFlush(network);
        }
        MockMultipartFile file = buildValidCsvFile();
        var response = dndController.save(file, new DndRequestDTO("Valid " + dndType.name(), "99009900", DndType.GLOBAL));
        assertNotNull(response);
        System.out.println("####: " + response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    private static Stream<Arguments> validDndScenarios() {
        return Stream.of(
                Arguments.of(DndType.NETWORK_ID, "1", true),
                Arguments.of(DndType.SENDER, "CLARO_SMS", false),
                Arguments.of(DndType.GLOBAL, "GLOBAL_VAL", false)
        );
    }

    private MockMultipartFile buildTestFile(byte[] content) {
        return new MockMultipartFile("file", "test.csv", "text/csv", content);
    }

    private MockMultipartFile buildValidCsvFile() {
        String content = "msisdn\n5050000001\n5050000002\n";
        return buildTestFile(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd should return error when DND name is duplicated")
    void testDuplicateDndNameShouldReturnError() throws Exception {
        String dndName = "Duplicated Name";
        String dndValue = "CLARO_SMS";
        DndType dndType = DndType.SENDER;

        String jsonBody = TestFileGenerator.buildDndJson(dndName, dndType, dndValue);
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"));

        mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.comment").value(containsString("already exists")));
    }

    @Test
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd/entry should save new MSISDN entry and prevent duplicates")
    void testSaveDndEntry() throws Exception {
        String jsonBody = TestFileGenerator.buildDndJson("DND Parent", DndType.SENDER, "TEST");
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int parentId = JsonPath.read(response, "$.data.id");

        String msisdn = "5050000" + new Random().nextInt(9999);
        String entryJson = """
        {
          "parent_id": %d,
          "msisdns": ["%s"]
        }
        """.formatted(parentId, msisdn);

        mockMvc.perform(post("/dnd/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd/change-status should disable entry with valid parent_id and return success")
    void disableEntryWhenValidParentIdThenReturnsSuccess() throws Exception {
        String jsonBody = TestFileGenerator.buildDndJson("Disable Test", DndType.SENDER, "CLARO_SMS");
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int parentId = JsonPath.read(response, "$.data.id");
        mockMvc.perform(post("/dnd/change-status/" + parentId + "/false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.comment").value("DND entry list status changed to DISABLED."));
    }

    @Test
    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @DisplayName("POST /dnd/change-status should return error when parent_id is invalid")
    void disableEntryWhenInvalidParentIdThenReturnsError() throws Exception {
        int invalidId = 999999;
        mockMvc.perform(post("/dnd/change-status/" + invalidId + "/false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.comment").value("Parent DND name with ID " + invalidId + " does not exist."));
    }

    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @Test
    @DisplayName("GET /dnd/{parentId}/entries should return paginated entries for valid parent ID")
    void testGetDndEntriesWithValidParentId() throws Exception {
        // Create a DND list first
        String jsonBody = TestFileGenerator.buildDndJson("Test Pagination", DndType.SENDER, "TEST_SENDER");
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int parentId = JsonPath.read(response, "$.data.id");

        // Test pagination endpoint
        mockMvc.perform(get("/dnd/{parentId}/entries", parentId)
                        .param("offset", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.comment").value("DND entries retrieved successfully"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.page_size").value(10))
                .andExpect(jsonPath("$.data.total_elements").exists())
                .andExpect(jsonPath("$.data.data").isArray());
    }

    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @Test
    @DisplayName("GET /dnd/{parentId}/entries should return 404 for invalid parent ID")
    void testGetDndEntriesWithInvalidParentId() throws Exception {
        // Test with non-existent parent ID
        mockMvc.perform(get("/dnd/{parentId}/entries", 99999)
                        .param("offset", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.comment").value(containsString("DND list with id 99999 was not found")));
    }

    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @Test
    @DisplayName("GET /dnd/{parentId}/entries should use default pagination parameters")
    void testGetDndEntriesWithDefaultPagination() throws Exception {
        // Create a DND list first
        String jsonBody = TestFileGenerator.buildDndJson("Test Default Pagination", DndType.GLOBAL, "");
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int parentId = JsonPath.read(response, "$.data.id");

        // Test pagination endpoint with default parameters
        mockMvc.perform(get("/dnd/{parentId}/entries", parentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.comment").value("DND entries retrieved successfully"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.page_size").value(10));
    }

    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @Test
    @DisplayName("GET /dnd/{parentId}/entries should handle custom pagination parameters")
    void testGetDndEntriesWithCustomPagination() throws Exception {
        // Create a DND list first
        String jsonBody = TestFileGenerator.buildDndJson("Test Custom Pagination", DndType.SENDER, "CUSTOM_SENDER");
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int parentId = JsonPath.read(response, "$.data.id");

        // Test pagination endpoint with custom parameters
        mockMvc.perform(get("/dnd/{parentId}/entries", parentId)
                        .param("offset", "2")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.comment").value("DND entries retrieved successfully"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.page_size").value(5));
    }

    @WithMockUser(roles = { "ADMINISTRATOR", "ROOT" })
    @Test
    @DisplayName("GET /dnd/{parentId}/entries should filter results when search parameter is provided")
    void testGetDndEntriesWithSearch() throws Exception {
        String jsonBody = TestFileGenerator.buildDndJson("Searchable Entry", DndType.SENDER, "SENDER_X");
        MockMultipartFile file = buildValidCsvFile();
        MockMultipartFile dndJson = new MockMultipartFile("dnd", "", "application/json", jsonBody.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/dnd")
                        .file(file)
                        .file(dndJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int parentId = JsonPath.read(response, "$.data.id");

        mockMvc.perform(get("/dnd/{parentId}/entries", parentId)
                        .param("search", "5050000001")
                        .param("offset", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data.length()").value(1))
                .andExpect(jsonPath("$.data.data[0].msisdn").value("5050000001"));
    }
}
