package com.sina.util.dnscache.speedtest;


public abstract class BaseSpeedTest {

    public abstract int speedTest(String ip, String host);
    
    /**
     * 被执行的优先级
     * @return
     */
    public abstract int getPriority();
    
    /**
     * 是否是激活状态
     * @return
     */
    public abstract boolean isActivate();
    
}