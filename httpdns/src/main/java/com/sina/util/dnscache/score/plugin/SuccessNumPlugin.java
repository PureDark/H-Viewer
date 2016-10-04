package com.sina.util.dnscache.score.plugin;

import com.sina.util.dnscache.model.IpModel;
import com.sina.util.dnscache.score.IPlugIn;
import com.sina.util.dnscache.score.PlugInManager;

import java.util.ArrayList;

public class SuccessNumPlugin implements IPlugIn {

    @Override
    public void run(ArrayList<IpModel> list) {
        // 查找到最大历史成功次数
        float MAX_SUCCESSNUM = 0;
        for (int i = 0; i < list.size(); i++) {
            IpModel temp = list.get(i);
            if (temp.success_num == null || temp.success_num.equals(""))
                continue;
            float successNum = Float.parseFloat(temp.success_num);
            MAX_SUCCESSNUM = Math.max(MAX_SUCCESSNUM, successNum);
        }
        // 计算比值
        if (MAX_SUCCESSNUM == 0) {
            return;
        }
        float bi = getWeight() / MAX_SUCCESSNUM;
        // 计算得分
        for (int i = 0; i < list.size(); i++) {
            IpModel temp = list.get(i);
            if (temp.success_num == null || temp.success_num.equals("")){
                continue;
            }
            float successNum = Float.parseFloat(temp.success_num);
            temp.grade += (successNum * bi);
        }

    }

    @Override
    public float getWeight() {
        return PlugInManager.SuccessNumPluginNum;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

}
