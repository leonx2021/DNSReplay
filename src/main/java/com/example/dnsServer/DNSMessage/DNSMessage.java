package com.example.dnsServer.DNSMessage;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DNSMessage {
    // Header fields
    private int transactionID;
    private int flags;
    private int qdCount;
    private int anCount;
    private int nsCount;
    private int arCount;

    // Question section
    private String questionName;
    private int questionType;
    private int questionClass;

    // Answer section
    private List<ResourceRecord> answers = new ArrayList<>();

    // ResourceRecord inner class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceRecord {
        private String name;
        private int type;
        private int rClass;
        private int ttl;
        private byte[] rData;
    }

    // Parsing method
    public static DNSMessage parseMessage(byte[] data, int length) throws Exception {
        DNSMessage message = new DNSMessage();
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

        // Parse Header
        message.setTransactionID(buffer.getShort() & 0xFFFF);
        message.setFlags(buffer.getShort() & 0xFFFF);
        message.setQdCount(buffer.getShort() & 0xFFFF);
        message.setAnCount(buffer.getShort() & 0xFFFF);
        message.setNsCount(buffer.getShort() & 0xFFFF);
        message.setArCount(buffer.getShort() & 0xFFFF);

        // Parse Question
        message.setQuestionName(parseQName(buffer));
        message.setQuestionType(buffer.getShort() & 0xFFFF);
        message.setQuestionClass(buffer.getShort() & 0xFFFF);

        // (可选) 解析更多部分，如回答、授权、附加记录

        return message;
    }

    private static String parseQName(ByteBuffer buffer) {
        StringBuilder name = new StringBuilder();
        while (true) {
            int length = buffer.get() & 0xFF;
            if (length == 0) break;
            byte[] labelBytes = new byte[length];
            buffer.get(labelBytes);
            String label = new String(labelBytes, StandardCharsets.UTF_8);
            name.append(label).append(".");
        }
        // 去掉最后一个点
        if (name.length() > 0) {
            name.setLength(name.length() - 1);
        }
        return name.toString();
    }

    // Building response method
    public byte[] buildResponse() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(512);

        // Transaction ID
        buffer.putShort((short) transactionID);

        // Flags: QR=1 (response), AA=1 (authoritative), RCODE=0 (no error)
        buffer.putShort((short) flags);

        // QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT
        buffer.putShort((short) qdCount);
        buffer.putShort((short) anCount);
        buffer.putShort((short) nsCount);
        buffer.putShort((short) arCount);

        // Question Section
        writeQName(buffer, questionName);
        buffer.putShort((short) questionType);
        buffer.putShort((short) questionClass);

        // Answer Section
        for (ResourceRecord answer : answers) {
            writeQName(buffer, answer.getName());
            buffer.putShort((short) answer.getType());
            buffer.putShort((short) answer.getRClass());
            buffer.putInt(answer.getTtl());
            buffer.putShort((short) answer.getRData().length);
            buffer.put(answer.getRData());
        }

        // Return the built response
        byte[] response = new byte[buffer.position()];
        buffer.flip();
        buffer.get(response);
        return response;
    }

    private static void writeQName(ByteBuffer buffer, String name) {
        String[] labels = name.split("\\.");
        for (String label : labels) {
            byte[] labelBytes = label.getBytes(StandardCharsets.UTF_8);
            buffer.put((byte) labelBytes.length);
            buffer.put(labelBytes);
        }
        buffer.put((byte) 0); // Terminate with zero length
    }
}
