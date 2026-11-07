package com.xiaofei.springbootbackendkafka.collector;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 极简 Modbus TCP 客户端，支持 FC1/FC2/FC3/FC4
 *
 * @author tuaofei
 */
public class ModbusTcpClient implements Closeable {

    private static final int FUNCTION_READ_COILS = 0x01;
    private static final int FUNCTION_READ_DISCRETE_INPUTS = 0x02;
    private static final int FUNCTION_READ_HOLDING_REGISTERS = 0x03;
    private static final int FUNCTION_READ_INPUT_REGISTERS = 0x04;

    private final Socket socket;

    private final DataInputStream input;

    private final DataOutputStream output;

    private int transactionId = 0;

    private ModbusTcpClient(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public static ModbusTcpClient open(String host, int port, int connectTimeoutMillis, int readTimeoutMillis)
            throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            socket.setSoTimeout(readTimeoutMillis);
            return new ModbusTcpClient(socket);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    public boolean[] readCoils(int unitId, int address, int quantity) throws IOException {
        return readBits(unitId, FUNCTION_READ_COILS, address, quantity);
    }

    public boolean[] readDiscreteInputs(int unitId, int address, int quantity) throws IOException {
        return readBits(unitId, FUNCTION_READ_DISCRETE_INPUTS, address, quantity);
    }

    public int[] readHoldingRegisters(int unitId, int address, int quantity) throws IOException {
        return readRegisters(unitId, FUNCTION_READ_HOLDING_REGISTERS, address, quantity);
    }

    public int[] readInputRegisters(int unitId, int address, int quantity) throws IOException {
        return readRegisters(unitId, FUNCTION_READ_INPUT_REGISTERS, address, quantity);
    }

    private int[] readRegisters(int unitId, int functionCode, int address, int quantity) throws IOException {
        byte[] response = execute(unitId, functionCode, address, quantity);
        int byteCount = response[1] & 0xFF;
        if (byteCount != quantity * 2) {
            throw new IOException("Modbus响应数据长度不匹配: 期望 " + (quantity * 2) + "，实际 " + byteCount);
        }
        int[] registers = new int[quantity];
        for (int i = 0; i < quantity; i++) {
            registers[i] = ((response[2 + i * 2] & 0xFF) << 8) | (response[3 + i * 2] & 0xFF);
        }
        return registers;
    }

    private boolean[] readBits(int unitId, int functionCode, int address, int quantity) throws IOException {
        byte[] response = execute(unitId, functionCode, address, quantity);
        int byteCount = response[1] & 0xFF;
        if (byteCount != (quantity + 7) / 8) {
            throw new IOException("Modbus响应数据长度不匹配: 期望 " + ((quantity + 7) / 8) + "，实际 " + byteCount);
        }
        boolean[] bits = new boolean[quantity];
        for (int i = 0; i < quantity; i++) {
            int value = response[2 + i / 8] & 0xFF;
            bits[i] = ((value >> (i % 8)) & 0x01) == 1;
        }
        return bits;
    }

    private synchronized byte[] execute(int unitId, int functionCode, int address, int quantity) throws IOException {
        transactionId = (transactionId + 1) & 0xFFFF;
        byte[] pdu = new byte[]{
                (byte) functionCode,
                (byte) ((address >> 8) & 0xFF),
                (byte) (address & 0xFF),
                (byte) ((quantity >> 8) & 0xFF),
                (byte) (quantity & 0xFF)
        };
        int length = pdu.length + 1;
        byte[] frame = new byte[6 + length];
        frame[0] = (byte) ((transactionId >> 8) & 0xFF);
        frame[1] = (byte) (transactionId & 0xFF);
        frame[2] = 0;
        frame[3] = 0;
        frame[4] = (byte) ((length >> 8) & 0xFF);
        frame[5] = (byte) (length & 0xFF);
        frame[6] = (byte) unitId;
        System.arraycopy(pdu, 0, frame, 7, pdu.length);
        output.write(frame);
        output.flush();

        byte[] header = new byte[7];
        input.readFully(header);
        int responseTransactionId = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);
        int responseLength = ((header[4] & 0xFF) << 8) | (header[5] & 0xFF);
        if (responseLength < 2) {
            throw new IOException("Modbus响应长度非法: " + responseLength);
        }
        byte[] responsePdu = new byte[responseLength - 1];
        input.readFully(responsePdu);
        int responseFunction = responsePdu[0] & 0xFF;
        if ((responseFunction & 0x80) != 0) {
            int exceptionCode = responsePdu.length > 1 ? responsePdu[1] & 0xFF : -1;
            throw new IOException("Modbus异常响应: function=" + (responseFunction & 0x7F)
                    + ", exceptionCode=" + exceptionCode);
        }
        if (responseTransactionId != transactionId) {
            throw new IOException("Modbus事务号不匹配: 期望 " + transactionId + "，实际 " + responseTransactionId);
        }
        if (responseFunction != functionCode) {
            throw new IOException("Modbus功能码不匹配: 期望 " + functionCode + "，实际 " + responseFunction);
        }
        return responsePdu;
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException ignored) {
            // ignore
        }
        try {
            output.close();
        } catch (IOException ignored) {
            // ignore
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // ignore
        }
    }
}
