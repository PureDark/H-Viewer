package com.sina.util.dnscache.dnsp.impl;

import com.sina.util.dnscache.DNSCacheConfig;
import com.sina.util.dnscache.Tools;
import com.sina.util.dnscache.dnsp.DnsConfig;
import com.sina.util.dnscache.dnsp.IDnsProvider;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.net.ApacheHttpClientNetworkRequests;
import com.sina.util.dnscache.net.networktype.NetworkManager;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HttpPodDns implements IDnsProvider {

    private ApacheHttpClientNetworkRequests netWork;

    public HttpPodDns() {
        netWork = new ApacheHttpClientNetworkRequests();
    }

    @Override
    public HttpDnsPack requestDns(String domain) {
        // 如果新浪自家的服务器没有拿到数据，或者数据有问题，则使用 dnspod 提供的接口获取数据
        String jsonDataStr = null;
        HttpDnsPack dnsPack = null;

        String dnspod_httpdns_api_url = DnsConfig.DNSPOD_SERVER_API + DNSPodCipher.Encryption(domain);
        jsonDataStr = netWork.requests(dnspod_httpdns_api_url);
        if (jsonDataStr == null || jsonDataStr.equals(""))
            return null; // 如果dnspod 也没提取到数据 则返回空

        jsonDataStr = DNSPodCipher.Decryption(jsonDataStr);

        dnsPack = new HttpDnsPack();
        try {
            String IP_TTL[] = jsonDataStr.split(",");
            String IPArr[] = IP_TTL[0].split(";");
            String TTL = IP_TTL[1];
            dnsPack.rawResult = jsonDataStr;
            dnsPack.domain = domain;
            dnsPack.device_ip = NetworkManager.Util.getLocalIpAddress();
            dnsPack.device_sp = NetworkManager.getInstance().getSPID() ; 

            dnsPack.dns = new HttpDnsPack.IP[IPArr.length];
            for (int i = 0; i < IPArr.length; i++) {
                dnsPack.dns[i] = new HttpDnsPack.IP();
                dnsPack.dns[i].ip = IPArr[i];
                dnsPack.dns[i].ttl = TTL;
                dnsPack.dns[i].priority = "0";
            }
        } catch (Exception e) {
            dnsPack = null;
        }
        return dnsPack;
    }

    @Override
    public boolean isActivate() {
        return DnsConfig.enableDnsPod;
    }

    static class DNSPodCipher {

        public static String Encryption(String domain) {

            if (DNSCacheConfig.Data.getInstance().DNSPOD_ID == null || DNSCacheConfig.Data.getInstance().DNSPOD_ID.equals(""))
                return domain;

            if (DNSCacheConfig.Data.getInstance().DNSPOD_KEY == null || DNSCacheConfig.Data.getInstance().DNSPOD_KEY.equals(""))
                return domain;

            try {
                // 初始化密钥
                SecretKeySpec keySpec = new SecretKeySpec(DNSCacheConfig.Data.getInstance().DNSPOD_KEY.getBytes("utf-8"), "DES");
                // 选择使用 DES 算法，ECB 方式，填充方式为 PKCS5Padding
                Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
                // 初始化
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                // 获取加密后的字符串
                byte[] encryptedString = cipher.doFinal(domain.getBytes("utf-8"));

                Tools.log("TAG_NET", bytesToHexString(encryptedString));

                return bytesToHexString(encryptedString) + "&id=" + DNSCacheConfig.Data.getInstance().DNSPOD_ID;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        public static String Decryption(String data) {

            if (DNSCacheConfig.Data.getInstance().DNSPOD_ID == null || DNSCacheConfig.Data.getInstance().DNSPOD_ID.equals(""))
                return data;

            if (DNSCacheConfig.Data.getInstance().DNSPOD_KEY == null || DNSCacheConfig.Data.getInstance().DNSPOD_KEY.equals(""))
                return data;

            try {
                // 初始化密钥
                SecretKeySpec keySpec = new SecretKeySpec(DNSCacheConfig.Data.getInstance().DNSPOD_KEY.getBytes("utf-8"), "DES");
                // 选择使用 DES 算法，ECB 方式，填充方式为 PKCS5Padding
                Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
                // 初始化
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                // 获取解密后的字符串
                byte[] decryptedString = cipher.doFinal(hexStringToBytes(data));

                Tools.log("TAG_NET", new String(decryptedString));

                return new String(decryptedString);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        public static String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0) {
                return null;
            }
            for (int i = 0; i < src.length; i++) {
                int v = src[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        }

        public static byte[] hexStringToBytes(String hexString) {
            if (hexString == null || hexString.equals("")) {
                return null;
            }
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
            return d;
        }

        /**
         * Convert char to byte
         * 
         * @param c
         *            char
         * @return byte
         */
        private static byte charToByte(char c) {
            return (byte) "0123456789ABCDEF".indexOf(c);
        }

    }

    @Override
    public String getServerApi() {
        return DnsConfig.DNSPOD_SERVER_API;
    }

    @Override
    public int getPriority() {
        return 8;
    }
}
