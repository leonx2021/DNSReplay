package com.example.dnsServer.DNSServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) {
        int port = 8053;
        byte[] buffer = new byte[512];
        try(DatagramSocket socket = new DatagramSocket(port)) {


            System.out.println(port);

            //接受数据
            DatagramPacket receivedPacket = new DatagramPacket(buffer,buffer.length);
            socket.receive(receivedPacket);
            byte[] receivedData = receivedPacket.getData();
            int length = receivedPacket.getLength();

            //解析数据
            for (int i = 0; i < length; i++) {
                System.out.printf("%02x ",receivedData[i]);
            }
            System.out.println();
            String s = "hello Hello from server hello Hello from server hello Hello from server";

            //返回数据
            byte[] responseData = s.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(
                    responseData,
                    responseData.length,
                    receivedPacket.getAddress(),
                    receivedPacket.getPort()
            );
            socket.send(responsePacket);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
