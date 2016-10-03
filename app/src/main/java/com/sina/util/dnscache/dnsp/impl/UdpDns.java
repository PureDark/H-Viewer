package com.sina.util.dnscache.dnsp.impl;

import com.sina.util.dnscache.dnsp.DnsConfig;
import com.sina.util.dnscache.dnsp.IDnsProvider;
import com.sina.util.dnscache.dnsp.impl.UdpDns.UdnDnsClient.UdpDnsInfo;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.net.networktype.NetworkManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UdpDns implements IDnsProvider {

    @Override
    public HttpDnsPack requestDns(String domain) {
        try {
            UdpDnsInfo info = UdnDnsClient.query(DnsConfig.UDPDNS_SERVER_API, domain);
            if (null != info && info.ips.length > 0) {
                HttpDnsPack dnsPack = new HttpDnsPack();
                String IPArr[] = info.ips;
                String TTL = String.valueOf(info.ttl);
                dnsPack.rawResult = "domain : " + domain + "\n" + info.toString();
                dnsPack.domain = domain;
                dnsPack.device_ip = NetworkManager.Util.getLocalIpAddress();
                dnsPack.device_sp = NetworkManager.getInstance().getSPID();

                dnsPack.dns = new HttpDnsPack.IP[IPArr.length];
                for (int i = 0; i < IPArr.length; i++) {
                    dnsPack.dns[i] = new HttpDnsPack.IP();
                    dnsPack.dns[i].ip = IPArr[i];
                    dnsPack.dns[i].ttl = TTL;
                    dnsPack.dns[i].priority = "0";
                }
                return dnsPack;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static class UdnDnsClient {
        private final static int TIME_OUT = 2000;
        private final static int PORT = 53;
        private final static int BUF_SIZE = 1024;

        static class UdpDnsInfo {
            public int ttl;
            public String[] ips;

            @Override
            public String toString() {
                StringBuilder info = new StringBuilder();
                info.append("ttl : " + ttl + "\n");
                info.append("ipArray : ");
                if (null != ips) {
                    for (String ip : ips) {
                        info.append(ip + ",");
                    }
                } else {
                    info.append("null   ");
                }
                return info.toString();
            }
        }

        private static UdpDnsInfo query(String dnsServerIP, String domainName) throws SocketTimeoutException, IOException {
            UdpDnsInfo info = new UdpDnsInfo();
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(TIME_OUT);

            ByteArrayOutputStream outBuf = new ByteArrayOutputStream(BUF_SIZE);
            DataOutputStream output = new DataOutputStream(outBuf);
            encodeDNSMessage(output, domainName);

            InetAddress host = InetAddress.getByName(dnsServerIP);
            DatagramPacket request = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), host, PORT);

            socket.send(request);

            byte[] inBuf = new byte[BUF_SIZE];
            ByteArrayInputStream inBufArray = new ByteArrayInputStream(inBuf);
            DataInputStream input = new DataInputStream(inBufArray);
            DatagramPacket response = new DatagramPacket(inBuf, inBuf.length);

            socket.receive(response);

            decodeDNSMessage(input, info);

            socket.close();
            return info;
        }

        private static void encodeDNSMessage(DataOutputStream output, String domainName) throws IOException {
            // transaction id
            output.writeShort(1);
            // flags
            output.writeShort(0x100);
            // number of queries
            output.writeShort(1);
            // answer, auth, other
            output.writeShort(0);
            output.writeShort(0);
            output.writeShort(0);

            encodeDomainName(output, domainName);

            // query type
            output.writeShort(1);
            // query class
            output.writeShort(1);

            output.flush();
        }

        private static void encodeDomainName(DataOutputStream output, String domainName) throws IOException {
            for (String label : domainName.split("\\.")) {
                output.writeByte((byte) label.length());
                output.write(label.getBytes());
            }
            output.writeByte(0);
        }

        private static void decodeDNSMessage(DataInputStream input, UdpDnsInfo info) throws IOException {
            // header
            // transaction id
            input.skip(2);
            // flags
            input.skip(2);
            // number of queries
            input.skip(2);
            // answer, auth, other
            short numberOfAnswer = input.readShort();
            input.skip(2);
            input.skip(2);

            // question record
            skipDomainName(input);
            // query type
            input.skip(2);
            // query class
            input.skip(2);

            // answer records
            for (int i = 0; i < numberOfAnswer; i++) {
                input.mark(1);
                byte ahead = input.readByte();
                input.reset();
                if ((ahead & 0xc0) == 0xc0) {
                    // compressed name
                    input.skip(2);
                } else {
                    skipDomainName(input);
                }

                // query type
                short type = input.readShort();
                // query class
                input.skip(2);
                // ttl
                int ttl = input.readInt();
                info.ttl = ttl;
                short addrLen = input.readShort();
                info.ips = new String[1];
                if (type == 1 && addrLen == 4) {
                    int addr = input.readInt();
                    info.ips[0] = (longToIp(addr));
                } else {
                    input.skip(addrLen);
                }
            }
        }

        private static void skipDomainName(DataInputStream input) throws IOException {
            byte labelLength = 0;
            do {
                labelLength = input.readByte();
                input.skip(labelLength);
            } while (labelLength != 0);
        }

        private static String longToIp(long ip) {
            return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
        }
    }

    @Override
    public boolean isActivate() {
        return DnsConfig.enableUdpDns;
    }

    @Override
    public String getServerApi() {
        return DnsConfig.UDPDNS_SERVER_API;
    }

    @Override
    public int getPriority() {
        return 7;
    }
}
