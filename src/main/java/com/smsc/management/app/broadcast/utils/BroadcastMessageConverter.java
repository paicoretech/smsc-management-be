package com.smsc.management.app.broadcast.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.interpreter.PayloadFormat;
import com.paicbd.smsc.interpreter.PayloadInterpreter;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.dto.BroadcastParamsDTO;
import com.smsc.management.app.broadcast.dto.BroadcastTestDTO;
import com.smsc.management.app.provider.model.entity.ServiceProvider;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class BroadcastMessageConverter {
    private BroadcastMessageConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static MessageEvent createMessageEventTest(BroadcastTestDTO broadcastTest, String messageId, ServiceProvider serviceProvider) throws JsonProcessingException {
        String shortMessage = createShortMessage(broadcastTest.getMessageTemplate(), broadcastTest.getColumnMappingData());
        String data = Converter.valueAsString(broadcastTest.getColumnMappingData());
        String interpreter = createPayloadInterpreter(broadcastTest.getColumnMapping());

        BroadcastParamsDTO paramsDTO = new BroadcastParamsDTO(
                broadcastTest.getSourceAddrTon(),
                broadcastTest.getSourceAddrNpi(),
                broadcastTest.getDestAddrTon(),
                broadcastTest.getDestAddrNpi(),
                broadcastTest.getDataCoding()
        );

        MessageEvent messageEvent = new MessageEvent();
        PayloadInterpreter.interpreterPayloadForReceive(data, interpreter, messageEvent, PayloadFormat.JSON);
        completeMessageEvent(messageEvent, broadcastTest.getSourceAddr(), serviceProvider, broadcastTest.isRequestDlr(), messageId, paramsDTO);
        messageEvent.setDestinationAddr(broadcastTest.getDestinationAddr());
        messageEvent.setShortMessage(shortMessage);
        messageEvent.setBroadcastId(-1); // -1 means sms is from a broadcast test

        return messageEvent;
    }

    public static void completeMessageEvent(MessageEvent event, String sourceAddr, ServiceProvider serviceProvider, boolean requestDlr, String messageId, BroadcastParamsDTO broadcast) {
        if (Objects.isNull(event.getSourceAddr())) {
            event.setSourceAddr(sourceAddr);
        }
        if (Objects.isNull(event.getSourceAddrNpi())) {
            event.setSourceAddrNpi(broadcast.getSourceAddrNpi());
        }
        if (Objects.isNull(event.getSourceAddrTon())) {
            event.setSourceAddrTon(broadcast.getSourceAddrTon());
        }
        if (Objects.isNull(event.getDestAddrNpi())) {
            event.setDestAddrNpi(broadcast.getDestAddrNpi());
        }
        if (Objects.isNull(event.getDestAddrTon())) {
            event.setDestAddrTon(broadcast.getDestAddrTon());
        }
        if (Objects.isNull(event.getDataCoding())) {
            event.setDataCoding(broadcast.getDataCoding());
        }
        event.setId(System.currentTimeMillis() + "-" + System.nanoTime());
        event.setMessageId(messageId);
        event.setParentId(messageId);
        event.setSystemId(serviceProvider.getSystemId());
        event.setOriginNetworkId(serviceProvider.getNetworkId());
        event.setOriginProtocol(serviceProvider.getProtocol());
        event.setOriginNetworkType("SP");
        event.setCommandStatus(0);
        event.setRegisteredDelivery(requestDlr ? 1 : 0);
        event.setSequenceNumber(SequenceNumber.getNextSequence());
        event.setRetry(false);
        event.setRetryDestNetworkId("");
        event.setSmscMessagePriority(serviceProvider.getMessagePriority());
    }

    public static String createShortMessage(String messageTemplate, Map<String, Object> columnMapping) {
        String result = messageTemplate;
        for (Map.Entry<String, Object> entry : columnMapping.entrySet()) {
            String placeholder = String.format("{{%s}}", entry.getKey());
            result = result.replace(placeholder, entry.getValue().toString());
        }
        return result;
    }

    public static String createPayloadInterpreter(Map<String, String> columnMapping) {
        StringJoiner payloadInterpreter = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            String format = String.format("\"%s\":\"{{%s:STRING}}\"", entry.getKey(), entry.getValue());
            payloadInterpreter.add(format);
        }
        return payloadInterpreter.toString();
    }
}
