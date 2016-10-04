package com.sina.util.dnscache.speedtest;

import com.sina.util.dnscache.Tools;
import com.sina.util.dnscache.speedtest.impl.PingTest;
import com.sina.util.dnscache.speedtest.impl.Socket80Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 测速类
 *
 * Created by fenglei on 15/4/22.
 */
public class SpeedtestManager implements ISpeedtest {

    /**
     * 请求server过程中发生错误
     */
    public static final int OCUR_ERROR = -1;
    
    public static final int MAX_OVERTIME_RTT = 9999;

    /**测速的轮询间隔*/
    public static long time_interval = 1 * 60 * 1000;

    private ArrayList<BaseSpeedTest> mSpeedTests = new ArrayList<BaseSpeedTest>();;

    public SpeedtestManager() {
        mSpeedTests.add(new Socket80Test());
        mSpeedTests.add(new PingTest());
    }

    /**
     * 
     * @param ip
     * @param host
     * @return
     */
    @Override
    public int speedTest(String ip, String host) {
        Collections.sort(mSpeedTests, new Comparator<BaseSpeedTest>() {
            @Override
            public int compare(BaseSpeedTest lhs, BaseSpeedTest rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                } else {
                    // 按照降序排序
                    return rhs.getPriority() - lhs.getPriority();
                }
            }
        });

        for (int i = 0; i < mSpeedTests.size(); i++) {
            BaseSpeedTest st = mSpeedTests.get(i);
            Tools.log("TAG", "测速模块" + st.getClass().getSimpleName() + "启动," + "\n优先级是：" + st.getPriority() + "\n该模块是否开启：" + st.isActivate());
            if (st.isActivate()) {
                int rtt = st.speedTest(ip, host);
                Tools.log("TAG", "测速模块" + st.getClass().getSimpleName() + "结束," + "\n测速的结果是（RTT）：" + rtt);
                if (rtt > OCUR_ERROR) {
                    return rtt;
                }
            }
        }
        return OCUR_ERROR;
    }
}
