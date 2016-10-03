package com.sina.util.dnscache.speedtest.impl;

import android.text.TextUtils;

import com.sina.util.dnscache.speedtest.BaseSpeedTest;
import com.sina.util.dnscache.speedtest.SpeedtestManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PingTest extends BaseSpeedTest{

    @Override
    public int speedTest(String ip, String host) {
        try {
            return Ping.runcmd("ping -c1 -s1 -w1 " + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SpeedtestManager.OCUR_ERROR;
    }

    public static class Ping {
        // ping -c1 -s1 -w1 www.baidu.com //-w 超时单位是s
        private static final String TAG_BYTES_FROM = "bytes from ";

        public static int runcmd(String cmd) throws Exception {
            Runtime runtime = Runtime.getRuntime();
            Process proc = null;

            final String command = cmd.trim();
            long startTime = System.currentTimeMillis();
            proc = runtime.exec(command);
            proc.waitFor();
            long endTime = System.currentTimeMillis();
            InputStream inputStream = proc.getInputStream();
            String result = "unknown ip";

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while (null != (line = reader.readLine())) {
                resultBuilder.append(line);
            }
            reader.close();
            String responseStr = resultBuilder.toString();
            result = responseStr.toLowerCase().trim();
            if (isValidResult(result)) {
                return (int) (endTime - startTime);
            }
            return SpeedtestManager.OCUR_ERROR;
        }

        private static boolean isValidResult(String result) {
            if (!TextUtils.isEmpty(result)) {
                if (result.indexOf(TAG_BYTES_FROM) > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isActivate() {
        return false;
    }
}
