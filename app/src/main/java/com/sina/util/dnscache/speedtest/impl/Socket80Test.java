package com.sina.util.dnscache.speedtest.impl;

import com.sina.util.dnscache.speedtest.BaseSpeedTest;
import com.sina.util.dnscache.speedtest.SpeedtestManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Socket80Test extends BaseSpeedTest {

    static final int TIMEOUT = 5 * 1000;

    @Override
    public int speedTest(String ip, String host) {
        Socket socket = null;
        try {
            long begin = System.currentTimeMillis();
            Socket s1 = new Socket();
            s1.connect(new InetSocketAddress(ip, 80), TIMEOUT);
            long end = System.currentTimeMillis();
            int rtt = (int) (end - begin);
            return rtt;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != socket) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SpeedtestManager.OCUR_ERROR;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean isActivate() {
        return true;
    }
}
