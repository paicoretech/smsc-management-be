package com.smsc.management.app.dnd.controller;

import com.smsc.management.app.dnd.dto.DndEntryMsisdnDTO;
import com.smsc.management.app.dnd.dto.DndRequestDTO;
import com.smsc.management.app.dnd.service.DndService;
import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dnd")
public class DndController {

    private final DndService dndService;
    private final ControllerSecurity security;

    @GetMapping
    public ResponseEntity<ApiResponse> get() {
        security.checkCommonRoles();
        ApiResponse result = dndService.getAll();
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> save(
            @RequestPart("file") MultipartFile file,
            @RequestPart("dnd") DndRequestDTO dndInfo
    ) {
        security.checkCommonRoles();
        ApiResponse result = dndService.saveDndFile(dndInfo, file);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/entry")
    public ResponseEntity<ApiResponse> saveEntry(@Valid @RequestBody DndEntryMsisdnDTO dto) {
        security.checkCommonRoles();
        ApiResponse result = dndService.saveDndEntry(dto);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/{parentId}/entries")
    public ResponseEntity<ApiResponse> getDndEntries(
            @PathVariable Integer parentId,
            @RequestParam(value = "offset", defaultValue = "1") Integer offset,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "search", required = false) String search) {
        security.checkCommonRoles();

        Map<String, Object> filters = new HashMap<>();
        filters.put("parent_id", parentId);
        filters.put("offset", offset);
        filters.put("limit", limit);
        if (search != null && !search.trim().isEmpty()) {
            filters.put("search", search.trim());
        }

        ApiResponse result = dndService.getDndEntries(filters);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/change-status/{parentId}/{enabled}")
    public ResponseEntity<ApiResponse> changeStatus(
            @PathVariable int parentId,
            @PathVariable boolean enabled) {
        security.checkCommonRoles();
        ApiResponse result = dndService.changeStatus(parentId, enabled);
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/{parentId}")
    public ResponseEntity<ApiResponse> deleteDndEntry(@PathVariable int parentId) {
        security.checkCommonRoles();
        ApiResponse result = dndService.deleteDndEntryList(parentId);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/{parentId}/name")
    public ResponseEntity<ApiResponse> rename(
            @PathVariable int parentId,
            @RequestBody Map<String, String> body) {
        security.checkCommonRoles();
        ApiResponse result = dndService.renameList(parentId, body.get("name"));
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/{parentId}/msisdns/{msisdn}")
    public ResponseEntity<ApiResponse> deleteMsisdn(
            @PathVariable int parentId,
            @PathVariable String msisdn) {
        security.checkCommonRoles();
        ApiResponse result = dndService.deleteSingleMsisdn(parentId, msisdn);
        return ResponseEntity.status(result.status()).body(result);
    }
}
