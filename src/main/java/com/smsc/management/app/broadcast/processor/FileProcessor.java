package com.smsc.management.app.broadcast.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.service.BroadcastFileService;
import com.smsc.management.app.broadcast.utils.BroadcastMessageConverter;
import com.smsc.management.exception.SmscBackendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.LOAD_BROADCAST_FILE_QUERY;

@Slf4j
@RequiredArgsConstructor
public class FileProcessor {

    private static final Set<Integer> VALID_TON = Set.of(
            (int) TypeOfNumber.NATIONAL.value(),
            (int) TypeOfNumber.INTERNATIONAL.value(),
            (int) TypeOfNumber.UNKNOWN.value()
    );
    private static final Set<Integer> VALID_NPI = Set.of(
            (int) NumberingPlanIndicator.ISDN.value(),
            (int) NumberingPlanIndicator.UNKNOWN.value()
    );

    private static final String DESTINATION_ADDR = "destinationAddr";

    public final BroadcastFileService broadcastFileService;
    public final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a JSON string representing the mapped data from a CSV row based on the column
     * mapping defined in the provided {@link Broadcast} object.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Deserializes the column mapping from the broadcast configuration.</li>
     *     <li>Builds a map of logical keys to values extracted from the CSV row.</li>
     *     <li>Converts the mapped data to a JSON string.</li>
     *     <li>If the row is the first data row, stores the mapping in the broadcast entity and saves it.</li>
     * </ul>
     *
     * @param csvRow         An array of strings representing a row from the CSV file.
     * @param broadcast      The {@link Broadcast} containing the column mapping configuration.
     * @return A JSON string representation of the mapped CSV row data.
     */
    private Map<String, Object> createColumnMappingDataStr(String[] csvRow, Broadcast broadcast) {
        Map<String, String> columnMapping = Converter.stringToObject(broadcast.getColumnMapping(), new TypeReference<LinkedHashMap<String, String>>() {
        });
        Map<String, Integer> columnMappingIndex = this.createColumnMappingIndex(columnMapping);
        Map<String, Object> columnMappingData = new HashMap<>();
        for (Map.Entry<String, Integer> entry : columnMappingIndex.entrySet()) {
            columnMappingData.put(entry.getKey(), csvRow[entry.getValue()]);
        }

        this.validateFieldsRules(columnMapping, columnMappingData);
        return columnMappingData;
    }


    /**
     * Creates a mapping of column names to index values.
     * <p>
     * This method takes a map of column names (as keys) and their corresponding string values.
     * It returns a new map where the keys are the same column names, but the values are
     * their respective indices based on the input map's order.
     * The index starts from 0 and increments by 1 for each later entry.
     *
     * @param columnMapping A map containing column names as keys and string values associated with those columns.
     *                      This map is processed to create an index for each column.
     * @return A map where the keys are the same column names from the input map, but the values are their corresponding
     * indices starting from 0. The returned map's size will be equal to the input map's size.
     * @throws NullPointerException if the input map is null.
     * @see HashMap
     */
    private Map<String, Integer> createColumnMappingIndex(Map<String, String> columnMapping) {
        Map<String, Integer> columnMappingData = new LinkedHashMap<>();
        int i = 0;
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            columnMappingData.put(entry.getKey(), i);
            i += 1;
        }

        return columnMappingData;
    }



    /**
     * Validates if the provided Type of Number (TON) for the destination address is within the allowed range.
     * The Type of Number (TON) is used to indicate the type of number for the destination address.
     * The valid values for TON are:
     * <ul>
     *     <li>0 - Unknown</li>
     *     <li>1 - International</li>
     *     <li>2 - National</li>
     * </ul>
     * <p>
     * If the provided TON value is outside the valid range (i.e., not 0, 1, or 2), the method throws an
     * {@link SmscBackendException} with a descriptive error message indicating the invalid TON value.
     *
     * @param ton The Type of Number (TON) value to be validated. This value must be one of: 0, 1, or 2.
     * @throws SmscBackendException if the provided TON value is invalid (i.e., not 0, 1, or 2).
     */
    private void invalidDestinationAddressTon(int ton) {
        if (!VALID_TON.contains(ton)) {
            throw new SmscBackendException("Invalid destination address TON -> " + ton);
        }
    }

    /**
     * Validates the Numbering Plan Indicator (NPI) for the destination address and throws an exception if it is invalid.
     * The Numbering Plan Indicator (NPI) is used to define the numbering plan for the destination address.
     * The valid values for NPI are:
     * <ul>
     *     <li>0 - Unknown</li>
     *     <li>1 - ISDN/Telephone numbering plan</li>
     * </ul>
     * <p>
     * If the provided NPI value is outside the valid range (i.e., not 0 or 1), the method throws an
     * {@link SmscBackendException} with a descriptive error message indicating the invalid NPI value.
     *
     * @param npi The Numbering Plan Indicator (NPI) value to be validated. This value must be either 0 or 1.
     * @throws SmscBackendException if the provided NPI value is invalid (i.e., not 0 or 1).
     */
    private void invalidDestinationAddressNpi(int npi) {
        if (!VALID_NPI.contains(npi)) {
            throw new SmscBackendException("Invalid destination address NPI -> " + npi);
        }
    }

    private void validateFieldsRules(Map<String, String> columnMapping, Map<String, Object> columnMappingData) {
        Map<String, String> messageEventMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            messageEventMapping.put(entry.getValue(), entry.getKey());
        }

        if (!messageEventMapping.isEmpty()) {
            if (messageEventMapping.containsKey("destAddrNpi")) {
                String keyMapping = messageEventMapping.get("destAddrNpi");
                this.invalidDestinationAddressNpi(Integer.parseInt(columnMappingData.get(keyMapping).toString()));
            }

            if (messageEventMapping.containsKey("destAddrTon")) {
                String keyMapping = messageEventMapping.get("destAddrTon");
                this.invalidDestinationAddressTon(Integer.parseInt(columnMappingData.get(keyMapping).toString()));
            }
        }
    }


    public String getDestinationAddr(String columnMappingString, Map<String, Object> columnMappingData) {
        Map<String, String> columnMapping = Converter.stringToObject(columnMappingString, new TypeReference<LinkedHashMap<String, String>>() {
        });
        Map<String, String> messageEventMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            messageEventMapping.put(entry.getValue(), entry.getKey());
        }
        String keyMapping = messageEventMapping.getOrDefault(DESTINATION_ADDR, "");
        return columnMappingData.getOrDefault(keyMapping, "").toString();
    }


    public BroadcastDevices getBroadcastDevices(String[] rowData, Broadcast broadcast) {
        String messageId = System.currentTimeMillis() + "-" + System.nanoTime();
        Map<String, Object> columnMappingData = this.createColumnMappingDataStr(rowData, broadcast);
        String shortMessage = BroadcastMessageConverter.createShortMessage(broadcast.getMessageTemplate(), columnMappingData);
        BroadcastDevices broadcastDevices = new BroadcastDevices();
        broadcastDevices.setMessageId(messageId);
        broadcastDevices.setBroadcastId(broadcast.getId());
        broadcastDevices.setColumnMappingDataStr(Converter.valueAsString(columnMappingData));
        broadcastDevices.setDestinationAddr(getDestinationAddr(broadcast.getColumnMapping(), columnMappingData));
        broadcastDevices.setMessage(shortMessage.trim());
        broadcastDevices.setDestNetworkId(0);
        broadcastDevices.setDestName("");
        broadcastDevices.setDestNetworkType("");
        broadcastDevices.setDestProtocol("");
        if (broadcast.getSenderId() != null && !broadcast.getSenderId().isEmpty()) {
            broadcastDevices.setSourceAddr(broadcast.getSenderId());
        }
        return broadcastDevices;
    }

    public void insertBroadcastDevicesNative(List<BroadcastDevices> broadcastDevicesBatch) {
        SqlParameterSource[] batchParams = broadcastDevicesBatch.stream()
                .map(bd -> new MapSqlParameterSource()
                        .addValue("broadcastId", bd.getBroadcastId())
                        .addValue("messageId", bd.getMessageId())
                        .addValue("columnMappingDataStr", bd.getColumnMappingDataStr())
                        .addValue("comment", bd.getComment())
                        .addValue("destName", bd.getDestName())
                        .addValue("destNetworkId", bd.getDestNetworkId())
                        .addValue("destNetworkType", bd.getDestNetworkType())
                        .addValue("destProtocol", bd.getDestProtocol())
                        .addValue(DESTINATION_ADDR, bd.getDestinationAddr())
                        .addValue("enqueueAt", bd.getEnqueueAt())
                        .addValue("message", bd.getMessage())
                        .addValue("sentAt", bd.getSentAt())
                        .addValue("sourceAddr", bd.getSourceAddr())
                        .addValue("status", bd.getStatus())
                )
                .toArray(SqlParameterSource[]::new);

        log.debug("Inserting {} devices into DB for broadcastId {}", broadcastDevicesBatch.size(), broadcastDevicesBatch.get(0).getBroadcastId());
        long insertStart = System.currentTimeMillis();
        jdbcTemplate.batchUpdate(LOAD_BROADCAST_FILE_QUERY, batchParams);
        log.debug("Inserted {} devices for broadcastId {}, took {}ms", broadcastDevicesBatch.size(), broadcastDevicesBatch.get(0).getBroadcastId(), System.currentTimeMillis() - insertStart);
        broadcastDevicesBatch.clear();
    }
}
