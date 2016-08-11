package ml.puredark.hviewer.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用正则表达式验证输入格式
 *
 * @author liuxing
 */
public class RegexValidateUtil {
    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码
     *
     * @param mobileNumber 手机号码
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = false;
        try {
            Pattern regex = Pattern.compile("^(((13[0-9])|(14[0-9])|(15([0-3]|[5-9]))|(17[0-9])|(18[0-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    public static String getHostFromUrl(String url) {
        Pattern p = Pattern.compile("https?://[^/]*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        if (matcher.find())
            return matcher.group();
        else
            return "";
    }

    public static String geCurrDirFromUrl(String url) {
        Pattern p = Pattern.compile("https?://[\\w./]*/", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        if (matcher.find())
            return matcher.group();
        else
            return "";
    }

    public static String getAbsoluteUrlFromRelative(String url, String host) {
        if (url.startsWith("/"))
            return getHostFromUrl(host) + url;
        else if (url.startsWith("./"))
            return geCurrDirFromUrl(host) + url;
        else if (url.startsWith("../")){
            Pattern p = Pattern.compile("(https?://[\\w./]*/).*/", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(url);
            String prefix = (matcher.find())?matcher.group(1):"";
            return prefix + url.substring(3);
        }
        else if (url.startsWith("../../")){
            Pattern p = Pattern.compile("(https?://[\\w./]*/).*/.*/", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(url);
            String prefix = (matcher.find())?matcher.group(1):"";
            return prefix + url.substring(6);
        }else
            return url;
    }
}
