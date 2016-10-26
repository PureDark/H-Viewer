package com.sina.util.dnscache;

import android.content.Context;
import android.content.SharedPreferences;

import com.sina.util.dnscache.cache.DnsCacheManager;
import com.sina.util.dnscache.dnsp.DnsConfig;
import com.sina.util.dnscache.log.HttpDnsLogManager;
import com.sina.util.dnscache.net.ApacheHttpClientNetworkRequests;
import com.sina.util.dnscache.net.INetworkRequests;
import com.sina.util.dnscache.score.PlugInManager;
import com.sina.util.dnscache.score.ScoreManager;
import com.sina.util.dnscache.speedtest.SpeedtestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;

/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DNSCacheConfig <br>
 * 类描述: Lib库全局 配置文件 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-4-21 下午5:26:04 <br>
 * 
 * 修改人: <br>
 * 修改时间: <br>
 * 修改备注: <br>
 * 
 * @version V1.0
 */
public class DNSCacheConfig {

    /**
     * 调试 开关
     */
    public static boolean DEBUG = false;
    
    /**
     * 是否启用云端更新配置策略
     */
    private static final boolean ENABLE_UPDATE_CONFIG = false;

    /**
     * 配置文件更新地址
     */
    private static String ConfigText_API = "http://202.108.7.153/config";
    
    public static ArrayList<String> domainSupportList = new ArrayList<String>();

    /**
     * 设置 动态更新配置参数服务器url连接
     */
    public static void SetConfigApiUrl(String url) {
        DNSCacheConfig.ConfigText_API = url;
    }

    /**
     * 初始化 配置文件。 步骤：1.获取本地缓存文件，若没有则创建默认配置
     * 
     * @param ctx
     */
    public static void InitCfg(final Context ctx) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("HttpDNSConstantsJson", Context.MODE_PRIVATE); // 私有数据
        Data data = null;
        try {
        	
            String text = sharedPreferences.getString("ConfigText", "" );
            
            if (text == null || text.equals("")) {
                Tools.log("TAG_NET", "text = " + text);
                data = Data.createDefault();
                saveLocalConfigAndSync(ctx, data);
            }else{
            	data = Data.fromJson(text);
                syncConfig(data);
            }
            
        	
        } catch (Exception e) {
            e.printStackTrace();
            // 上报错误 
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("ConfigText", "");
            boolean is = editor.commit();
            if (is)
                InitCfg(ctx);
        }
        pullConfigFromServer(ctx);
    }

    /**
     * 同步各模块配置项 
     * 
     * @param data
     */
    private static void syncConfig(Data data) {
    	
        if (null != data) {
            DNSCache.timer_interval = Integer.valueOf(data.SCHEDULE_TIMER_INTERVAL);
            SpeedtestManager.time_interval = Integer.valueOf(data.SCHEDULE_SPEED_INTERVAL);
            HttpDnsLogManager.time_interval = Integer.valueOf(data.SCHEDULE_LOG_INTERVAL);
            HttpDnsLogManager.sample_rate = Integer.valueOf(data.HTTPDNS_LOG_SAMPLE_RATE);
            DnsCacheManager.ip_overdue_delay = Integer.valueOf(data.IP_OVERDUE_DELAY);
            DNSCache.isEnable = data.HTTPDNS_SWITCH.equals("1") ? true : false;
            
            DnsConfig.enableSinaHttpDns = data.IS_MY_HTTP_SERVER.equals("1") == true;
            DnsConfig.enableDnsPod = data.IS_DNSPOD_SERVER.equals("1") == true;
            DnsConfig.enableUdpDns = data.IS_UDPDNS_SERVER.equals("1") == true;
            DnsConfig.DNSPOD_SERVER_API = data.DNSPOD_SERVER_API;
            DnsConfig.UDPDNS_SERVER_API = data.UDPDNS_SERVER_API;
            
            //
            ScoreManager.IS_SORT = data.IS_SORT.equals("1") == true;
            
            String SPEEDTEST_PLUGIN_NUM = data.SPEEDTEST_PLUGIN_NUM;
            if (isNum(SPEEDTEST_PLUGIN_NUM)) {
                PlugInManager.SpeedTestPluginNum = Float.valueOf(SPEEDTEST_PLUGIN_NUM);
            }
            String PRIORITY_PLUGIN_NUM = data.PRIORITY_PLUGIN_NUM;
            if (isNum(PRIORITY_PLUGIN_NUM)) {
                PlugInManager.PriorityPluginNum = Float.valueOf(PRIORITY_PLUGIN_NUM);
            }
            String SUCCESSNUM_PLUGIN_NUM = data.SUCCESSNUM_PLUGIN_NUM;
            if (isNum(SUCCESSNUM_PLUGIN_NUM)) {
                PlugInManager.SuccessNumPluginNum = Float.valueOf(SUCCESSNUM_PLUGIN_NUM);
            }
            String ERRNUM_PLUGIN_NUM = data.ERRNUM_PLUGIN_NUM;
            if (isNum(ERRNUM_PLUGIN_NUM)) {
                PlugInManager.ErrNumPluginNum = Float.valueOf(ERRNUM_PLUGIN_NUM);
            }
            String SUCCESSTIME_PLUGIN_NUM = data.SUCCESSTIME_PLUGIN_NUM;
            if (isNum(SUCCESSTIME_PLUGIN_NUM)) {
                PlugInManager.SuccessTimePluginNum = Float.valueOf(SUCCESSTIME_PLUGIN_NUM);
            }
            
            // arraylist
            domainSupportList.clear();
            DnsConfig.SINA_HTTPDNS_SERVER_API.clear();
            
            domainSupportList.addAll(data.DOMAIN_SUPPORT_LIST);
            DnsConfig.SINA_HTTPDNS_SERVER_API.addAll(data.HTTPDNS_SERVER_API);
        }
    }

    /**
     * 更新配置文件
     */
    private static void pullConfigFromServer(final Context ctx) {
        if (!ENABLE_UPDATE_CONFIG || ConfigText_API == null || ConfigText_API.equals("")){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    INetworkRequests netWork = new ApacheHttpClientNetworkRequests();
                    String url = ConfigText_API + "?k=" + AppConfigUtil.getAppKey() + "&v=" + AppConfigUtil.getVersionName();
                    String responseStr = netWork.requests(url);
//                    responseStr = createMockJsonStr();
                    Data data = Data.fromJson(responseStr);
                    if (null != data) {
                        saveLocalConfigAndSync(ctx, data);
                    }
                    HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_CONFIG, responseStr);
                } catch (Exception e) {
                    String message = e.toString();
                    JSONStringer stringer = new JSONStringer();
                    try {
                        stringer.object()//
                        .key("errorMsg").value(message)//
                        .endObject();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_INFO_CONFIG, stringer.toString());
                }
            }
        }).start();
    }

    /**
     * 保存本地配置信息。
     * 
     * @param ctx
     * @param configStr
     */
    public static void saveLocalConfigAndSync(Context ctx, Data model) {
        if (null != ctx && null != model) {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences("HttpDNSConstantsJson", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("ConfigText", model.toJson());
            editor.commit();
            syncConfig(model);
            Data.Instance = null ; 
        }
    }

    /**
     * 获取本地配置信息。
     * 
     * @param ctx
     * @param configStr
     */
    private static Data getLocalConfig() {
        SharedPreferences sharedPreferences = AppConfigUtil.getApplicationContext().getSharedPreferences("HttpDNSConstantsJson",
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("ConfigText", "");
        Data data = Data.fromJson(json);
        if (null == data) {
            data = Data.createDefault();
        }
        return data;
    }

    /**
     * 是否为数字
     * 
     * @param str
     * @return
     */
    private static boolean isNum(String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    public static class Data {
    	
    	public static Data Instance = null;
    	public static Data getInstance(){
    		if( Instance == null ){
    			Instance = DNSCacheConfig.getLocalConfig() ; 
    		}
    		return Instance ; 
    	}
    	
    	/**
         * 是否启用udpdns服务器 默认不启用 | 1启用 0不启用
         */
        public String IS_UDPDNS_SERVER = "";
        /**
         * udp dnsserver的地址
         */
        public String UDPDNS_SERVER_API = "";
    	/**
    	 * 日志采样率
    	 */
    	public String HTTPDNS_LOG_SAMPLE_RATE = "";
    	/**
    	 * lib库开关
    	 */
    	public String HTTPDNS_SWITCH = "";
    	/**
    	 * 测速间隔时间
    	 */
    	public String SCHEDULE_SPEED_INTERVAL = "";
    	/**
    	 * 日志上传的间隔时间
    	 */
    	public String SCHEDULE_LOG_INTERVAL = "";
    	/**
    	 * timer轮询器的间隔时间
    	 */
    	public String SCHEDULE_TIMER_INTERVAL = "";
    	/**
    	 * ip数据过期延迟差值
    	 */
    	public String IP_OVERDUE_DELAY = "";
    	/**************************************以上部分是新加配置，共8个********************************************/
        /**
         * 是否启用自己家的HTTP_DNS服务器 默认不启用 | 1启用 0不启用
         */
        public String IS_MY_HTTP_SERVER = null;
        /**
         * 自己家HTTP_DNS服务API地址 使用时直接在字符串后面拼接domain地址 |
         * 示例（http://202.108.7.153/dns?domain=）+ domain
         */
        public ArrayList<String> HTTPDNS_SERVER_API = new ArrayList<String>();
        /**
         * 是否启用dnspod服务器 默认不启用 | 1启用 0不启用
         */
        public String IS_DNSPOD_SERVER = "1";
        /**
         * DNSPOD HTTP_DNS 服务器API地址 | 默认（http://119.29.29.29/d?ttl=1&dn=）
         */
        public String DNSPOD_SERVER_API = null;
        /**
         * DNSPOD 企业级ID配置选项 
         */
        public String DNSPOD_ID = null;
        /**
         * DNSPOD 企业级KEY配置选项 
         */
        public String DNSPOD_KEY = null;

        /**
         * 是否开启 本地排序插件算法 默认开启 | 1开启 0不开启
         */
        public String IS_SORT = null;
        /**
         * 速度插件 比重分配值：默认40%
         */
        public String SPEEDTEST_PLUGIN_NUM = null;
        /**
         * 服务器推荐优先级插件 比重分配：默认30% （需要自家HTTP_DNS服务器支持）
         */
        public String PRIORITY_PLUGIN_NUM = null;
        /**
         * 历史成功次数计算插件 比重分配：默认10%
         */
        public String SUCCESSNUM_PLUGIN_NUM = null;
        /**
         * 历史错误次数计算插件 比重分配：默认10%
         */
        public String ERRNUM_PLUGIN_NUM = null;
        /**
         * 最后一次成功时间计算插件 比重分配：默认10%
         */
        public String SUCCESSTIME_PLUGIN_NUM = null;
        /**
         * 白名单
         */
        public ArrayList<String> DOMAIN_SUPPORT_LIST = new ArrayList<String>();

        /**
         * 返回配置文件 json
         */
        public String toJson() {
            // 为了节约lib库的大小直接拼接 json 字符串吧，就不适用第三方库了
            StringBuffer buffer = new StringBuffer();
            buffer.append("{");
            buffer.append("\"IS_UDPDNS_SERVER\":" + "\"" + IS_UDPDNS_SERVER + "\",");
            buffer.append("\"UDPDNS_SERVER_API\":" + "\"" + UDPDNS_SERVER_API + "\",");
            buffer.append("\"HTTPDNS_LOG_SAMPLE_RATE\":" + "\"" + HTTPDNS_LOG_SAMPLE_RATE + "\",");
            buffer.append("\"HTTPDNS_SWITCH\":" + "\"" + HTTPDNS_SWITCH + "\",");
            buffer.append("\"SCHEDULE_LOG_INTERVAL\":" + "\"" + SCHEDULE_LOG_INTERVAL + "\",");
            buffer.append("\"SCHEDULE_SPEED_INTERVAL\":" + "\"" + SCHEDULE_SPEED_INTERVAL + "\",");
            buffer.append("\"SCHEDULE_TIMER_INTERVAL\":" + "\"" + SCHEDULE_TIMER_INTERVAL + "\",");
            buffer.append("\"IP_OVERDUE_DELAY\":" + "\"" + IP_OVERDUE_DELAY + "\",");
            buffer.append("\"IS_MY_HTTP_SERVER\":" + "\"" + IS_MY_HTTP_SERVER + "\",");
            buffer.append("\"IS_DNSPOD_SERVER\":" + "\"" + IS_DNSPOD_SERVER + "\",");
            buffer.append("\"DNSPOD_SERVER_API\":" + "\"" + DNSPOD_SERVER_API + "\",");
            // 测试阶段暂时先把 dnspod的ID和KEY加上
            buffer.append("\"DNSPOD_ID\":" + "\"" + DNSPOD_ID + "\",");
            buffer.append("\"DNSPOD_KEY\":" + "\"" + DNSPOD_KEY + "\",");
            buffer.append("\"IS_SORT\":" + "\"" + IS_SORT + "\",");
            buffer.append("\"SPEEDTEST_PLUGIN_NUM\":" + "\"" + SPEEDTEST_PLUGIN_NUM + "\",");
            buffer.append("\"PRIORITY_PLUGIN_NUM\":" + "\"" + PRIORITY_PLUGIN_NUM + "\",");
            buffer.append("\"SUCCESSNUM_PLUGIN_NUM\":" + "\"" + SUCCESSNUM_PLUGIN_NUM + "\",");
            buffer.append("\"ERRNUM_PLUGIN_NUM\":" + "\"" + ERRNUM_PLUGIN_NUM + "\",");
            buffer.append("\"SUCCESSTIME_PLUGIN_NUM\":" + "\"" + SUCCESSTIME_PLUGIN_NUM + "\",");
            buffer.append("\"DOMAIN_SUPPORT_LIST\":" + "[");
            for (int i = 0; i < DOMAIN_SUPPORT_LIST.size(); i++) {
                buffer.append("\"" + DOMAIN_SUPPORT_LIST.get(i) + "\"" + (i != DOMAIN_SUPPORT_LIST.size() - 1 ? "," : ""));
            }
            buffer.append("]");
            buffer.append(",");
            buffer.append("\"HTTPDNS_SERVER_API\":" + "[");
            for (int i = 0; i < HTTPDNS_SERVER_API.size(); i++) {
                buffer.append("\"" + HTTPDNS_SERVER_API.get(i) + "\"" + (i != HTTPDNS_SERVER_API.size() - 1 ? "," : ""));
            }
            buffer.append("]");
            buffer.append("}");
            return buffer.toString();
        }

        public static Data createDefault() {
            Data model = new Data();
            model.HTTPDNS_LOG_SAMPLE_RATE = "50";
            model.HTTPDNS_SWITCH = "1";
            model.SCHEDULE_LOG_INTERVAL = "3600000";
            model.SCHEDULE_SPEED_INTERVAL = "60000";
            model.SCHEDULE_TIMER_INTERVAL = "60000";
            model.IP_OVERDUE_DELAY = "60";
            
            model.IS_MY_HTTP_SERVER = "0";
            model.HTTPDNS_SERVER_API.add("http://xxx/dns?domain=");

            model.IS_DNSPOD_SERVER = "1";
            model.DNSPOD_SERVER_API = "http://119.29.29.29/d?ttl=1&dn=";
            model.DNSPOD_ID = "";
            model.DNSPOD_KEY = "";

            model.IS_UDPDNS_SERVER = "1";
            model.UDPDNS_SERVER_API = "114.114.114.114";
            
            model.IS_SORT = "1";
            model.SPEEDTEST_PLUGIN_NUM = "50";
            model.PRIORITY_PLUGIN_NUM = "50";
            model.SUCCESSNUM_PLUGIN_NUM = "10";
            model.ERRNUM_PLUGIN_NUM = "10";
            model.SUCCESSTIME_PLUGIN_NUM = "10";

            return model;
        }

        public static Data fromJson(String json) {
            Data model = createDefault();
            try {
                JSONObject jsonObj = new JSONObject(json);
                //new
                if (jsonObj.isNull("HTTPDNS_LOG_SAMPLE_RATE") == false) {
                    model.HTTPDNS_LOG_SAMPLE_RATE = jsonObj.getString("HTTPDNS_LOG_SAMPLE_RATE");
                }
                if (jsonObj.isNull("HTTPDNS_SWITCH") == false) {
                    model.HTTPDNS_SWITCH = jsonObj.getString("HTTPDNS_SWITCH");
                }
                if (jsonObj.isNull("SCHEDULE_LOG_INTERVAL") == false) {
                    model.SCHEDULE_LOG_INTERVAL = jsonObj.getString("SCHEDULE_LOG_INTERVAL");
                }
                if (jsonObj.isNull("SCHEDULE_SPEED_INTERVAL") == false) {
                    model.SCHEDULE_SPEED_INTERVAL = jsonObj.getString("SCHEDULE_SPEED_INTERVAL");
                }
                if (jsonObj.isNull("SCHEDULE_TIMER_INTERVAL") == false) {
                    model.SCHEDULE_TIMER_INTERVAL = jsonObj.getString("SCHEDULE_TIMER_INTERVAL");
                }
                if (jsonObj.isNull("IP_OVERDUE_DELAY") == false) {
                    model.IP_OVERDUE_DELAY = jsonObj.getString("IP_OVERDUE_DELAY");
                }
                if (jsonObj.isNull("IS_UDPDNS_SERVER") == false) {
                    model.IS_UDPDNS_SERVER = jsonObj.getString("IS_UDPDNS_SERVER");
                }
                if (jsonObj.isNull("UDPDNS_SERVER_API") == false) {
                    model.UDPDNS_SERVER_API = jsonObj.getString("UDPDNS_SERVER_API");
                }
                //----------------------------------------
                // httpdns
                if (jsonObj.isNull("IS_MY_HTTP_SERVER") == false) {
                    model.IS_MY_HTTP_SERVER = jsonObj.getString("IS_MY_HTTP_SERVER");
                }
                // dnspod
                if (jsonObj.isNull("IS_DNSPOD_SERVER") == false) {
                    model.IS_DNSPOD_SERVER = jsonObj.getString("IS_DNSPOD_SERVER");
                }

                if (jsonObj.isNull("DNSPOD_SERVER_API") == false) {
                    model.DNSPOD_SERVER_API = jsonObj.getString("DNSPOD_SERVER_API");
                }
                if (jsonObj.isNull("DNSPOD_ID") == false) {
                    model.DNSPOD_ID = jsonObj.getString("DNSPOD_ID");
                }
                if (jsonObj.isNull("DNSPOD_KEY") == false) {
                    model.DNSPOD_KEY = jsonObj.getString("DNSPOD_KEY");
                }

                // sort
                if (jsonObj.isNull("IS_SORT") == false) {
                    String IS_SORT = jsonObj.getString("IS_SORT");
                    model.IS_SORT = IS_SORT;
                }
                if (jsonObj.isNull("SPEEDTEST_PLUGIN_NUM") == false) {
                    model.SPEEDTEST_PLUGIN_NUM = jsonObj.getString("SPEEDTEST_PLUGIN_NUM");
                }
                if (jsonObj.isNull("PRIORITY_PLUGIN_NUM") == false) {
                    model.PRIORITY_PLUGIN_NUM = jsonObj.getString("PRIORITY_PLUGIN_NUM");
                }
                if (jsonObj.isNull("SUCCESSNUM_PLUGIN_NUM") == false) {
                    model.SUCCESSNUM_PLUGIN_NUM = jsonObj.getString("SUCCESSNUM_PLUGIN_NUM");
                }
                if (jsonObj.isNull("ERRNUM_PLUGIN_NUM") == false) {
                    model.ERRNUM_PLUGIN_NUM = jsonObj.getString("ERRNUM_PLUGIN_NUM");
                }
                if (jsonObj.isNull("SUCCESSTIME_PLUGIN_NUM") == false) {
                    model.SUCCESSTIME_PLUGIN_NUM = jsonObj.getString("SUCCESSTIME_PLUGIN_NUM");
                }

                // 白名单
                model.DOMAIN_SUPPORT_LIST.clear();
                if (jsonObj.isNull("DOMAIN_SUPPORT_LIST") == false) {
                    JSONArray jsonArr = jsonObj.getJSONArray("DOMAIN_SUPPORT_LIST");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        String temp = jsonArr.getString(i);
                        model.DOMAIN_SUPPORT_LIST.add(temp);
                    }
                }
                
                // 新浪httpdns服务地址
                model.HTTPDNS_SERVER_API.clear();
                if (jsonObj.isNull("HTTPDNS_SERVER_API") == false) {
                    JSONArray jsonArr = jsonObj.getJSONArray("HTTPDNS_SERVER_API");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        String temp = jsonArr.getString(i);
                        model.HTTPDNS_SERVER_API.add(temp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                model = null;
            }
            return model;
        }
    }
    //FOR TEST ！！！
    static String createMockJsonStr() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"HTTPDNS_LOG_SAMPLE_RATE\":" + "\"" + 10 + "\",");
        buffer.append("\"HTTPDNS_SWITCH\":" + "\"" + 1 + "\",");
        buffer.append("\"SCHEDULE_LOG_INTERVAL\":" + "\"" + 60 * 60 * 1000 + "\",");
        buffer.append("\"SCHEDULE_SPEED_INTERVAL\":" + "\"" + 10 * 1000 + "\",");
        buffer.append("\"SCHEDULE_TIMER_INTERVAL\":" + "\"" + 10 * 1000 + "\",");
        buffer.append("\"IS_MY_HTTP_SERVER\":" + "\"" + 1 + "\",");
        buffer.append("\"IS_SORT\":" + "\"" + 1 + "\",");
        buffer.append("\"SPEEDTEST_PLUGIN_NUM\":" + "\"" + 50 + "\",");
        buffer.append("\"PRIORITY_PLUGIN_NUM\":" + "\"" + 50 + "\",");
        
        ArrayList<String> whiteList = new ArrayList<String>();
        whiteList.add("api.camera.weibo.com");
        whiteList.add("ww4.sinaimg.cn");
        whiteList.add("api.weibo.cn");
        whiteList.add("m.weibo.cn");
        buffer.append("\"DOMAIN_SUPPORT_LIST\":" + "[");
        for (int i = 0; i < whiteList.size(); i++) {
            buffer.append("\"" + whiteList.get(i) + "\"" + (i != whiteList.size() - 1 ? "," : ""));
        }
        buffer.append("]");
        buffer.append(",");
        buffer.append("\"HTTPDNS_SERVER_API\":" + "[");
        for (int i = 0; i < 1; i++) {
            buffer.append("\"" + "http://202.108.7.153/dns?domain=" + "\"");
        }
        buffer.append("]");
        buffer.append("}");
        return buffer.toString();
    }
}
