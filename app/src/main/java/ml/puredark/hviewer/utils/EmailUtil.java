package ml.puredark.hviewer.utils;

import android.util.Log;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by PureDark on 2016/8/26.
 */

public class EmailUtil {

    public final static String smtpHost = "smtp.qq.com";

    public final static String fromEmail = "PureDark@qq.com";

    public final static String username = "PureDark";

    public final static String password = "fgtddvptuwdbbiaf";

    public static void sendEmail(String to, String title, String content) {
        try {
            sendMailByApache(to, title, content);
            SharedPreferencesUtil.saveData(mContext, "unupload_log", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMailByApache(String to, String title, String content) {

        try {
            HtmlEmail email = new HtmlEmail();
            // 这里是发送服务器的名字
            email.setHostName(smtpHost);
            // 编码集的设置
            email.setTLS(true);
            email.setSSL(true);

            email.setCharset("utf-8");
            // 收件人的邮箱
            email.addTo(to);
            // 发送人的邮箱
            email.setFrom(fromEmail);
            // 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和密码
            email.setAuthentication(username, password);
            email.setSubject(title);
            // 要发送的信息
            email.setMsg(content);
            // 发送
            email.send();
        } catch (EmailException e) {
            Log.i("EmailUtil", e.getMessage());
        }
    }

}
