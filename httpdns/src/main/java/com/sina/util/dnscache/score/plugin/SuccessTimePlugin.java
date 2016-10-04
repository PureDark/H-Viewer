package com.sina.util.dnscache.score.plugin;

import com.sina.util.dnscache.model.IpModel;
import com.sina.util.dnscache.score.IPlugIn;
import com.sina.util.dnscache.score.PlugInManager;

import java.util.ArrayList;

public class SuccessTimePlugin implements IPlugIn {

    @Override
    public void run(ArrayList<IpModel> list) {
        final float dayTime = 24 * 60;// 单位是minute
        final float bi = getWeight() / dayTime;
        for (int i = 0; i < list.size(); i++) {
            IpModel temp = list.get(i);
            if (temp.finally_success_time == null || temp.finally_success_time.equals(""))
                continue;
            long lastSuccTime = Long.parseLong(temp.finally_success_time);
            long now = System.currentTimeMillis();
            long offTime = (now - lastSuccTime) / 1000 / 60; // 单位是minute
            if (offTime > dayTime) {
                continue;
            } else {
                temp.grade += (getWeight() - (offTime * bi));
            }
        }
    }

    @Override
    public float getWeight() {
        return PlugInManager.SuccessTimePluginNum;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

}
