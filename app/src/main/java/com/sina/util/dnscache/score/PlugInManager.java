package com.sina.util.dnscache.score;

import com.sina.util.dnscache.model.IpModel;
import com.sina.util.dnscache.score.plugin.ErrNumPlugin;
import com.sina.util.dnscache.score.plugin.PriorityPlugin;
import com.sina.util.dnscache.score.plugin.SpeedTestPlugin;
import com.sina.util.dnscache.score.plugin.SuccessNumPlugin;
import com.sina.util.dnscache.score.plugin.SuccessTimePlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlugInManager {

    public ArrayList<IPlugIn> plugIn = new ArrayList<IPlugIn>();

    public static float SpeedTestPluginNum = 40;
    public static float PriorityPluginNum = 60;
    public static float SuccessNumPluginNum = 10;
    public static float ErrNumPluginNum = 10;
    public static float SuccessTimePluginNum = 10;

    public PlugInManager() {

        plugIn.add(new SpeedTestPlugin()); // 速度插件
        plugIn.add(new PriorityPlugin()); // 优先级推荐插件
        plugIn.add(new SuccessNumPlugin());
        plugIn.add(new ErrNumPlugin()); // 历史错误次数插件
        plugIn.add(new SuccessTimePlugin());
    }

    public void run(ArrayList<IpModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        //恢复得分为0
        for (int i = 0; i < list.size(); i++) {
            IpModel temp = list.get(i);
            if (null != temp) {
                temp.grade = 0;
            } else {
                return;
            }
        }
        for (int i = 0; i < plugIn.size(); i++) {
            IPlugIn plug = plugIn.get(i);
            if (plug.isActivated()) {
                plug.run(list);
            }
        }
        ipModelSort(list);
    }

    public void ipModelSort(ArrayList<IpModel> list) {
        Collections.sort(list, new IpModelSort());
    }

    class IpModelSort implements Comparator<IpModel> {
        @Override
        public int compare(IpModel lhs, IpModel rhs) {
            return (int) (rhs.grade - lhs.grade);
        }
    }
}
