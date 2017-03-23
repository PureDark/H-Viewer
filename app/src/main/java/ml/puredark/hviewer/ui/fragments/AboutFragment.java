package ml.puredark.hviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.content.Context.CLIPBOARD_SERVICE;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_PREF_MODE_R18_ENABLED;

/**
 * Created by PureDark on 2016/9/25.
 */
public class AboutFragment extends PreferenceFragment {
    public static final String KEY_PREF_ABOUT_AUTHOR = "pref_about_author";
    public static final String KEY_PREF_ABOUT_GITHUB = "pref_about_github";
    public static final String KEY_PREF_ABOUT_CHANGELOG = "pref_about_changelog";
    public static final String KEY_PREF_ABOUT_VERSION = "pref_about_version";
    public static final String KEY_PREF_ABOUT_QQ_GROUP = "pref_about_qq_group";
    public static final String KEY_PREF_ABOUT_DONATE = "pref_about_donate";

    private int pressCount = 0;

    private BaseActivity activity;

    public AboutFragment() {
    }

    @SuppressLint("ValidFragment")
    public AboutFragment(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
        addPreferencesFromResource(R.xml.about);
        Preference preference = getPreferenceScreen().findPreference(KEY_PREF_ABOUT_VERSION);
        preference.setSummary(HViewerApplication.getVersionName());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_PREF_ABOUT_AUTHOR)) {
            String message = "";

            boolean r18enabled = (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext, KEY_PREF_MODE_R18_ENABLED, false);
            if (r18enabled) {
                message = "已开启R18模式！无法关闭！哈哈哈后悔吧！";
            } else {
                switch ((pressCount)) {
                    case 0:
                        message = "为什么按我，觉得我帅吗";
                        break;
                    case 1:
                        message = "快住手！还有5步，我体内的洪荒之力就要爆发了！";
                        break;
                    case 2:
                        message = "别这样！还有4步，我控几不住我记几啊！";
                        break;
                    case 3:
                        message = "还有3步！啊，我的右手！我的王の几旮旯！";
                        break;
                    case 4:
                        message = "还有2步！现在停手还来得及！";
                        break;
                    case 5:
                        message = "还有1步！你一定会后悔的！";
                        break;
                    case 6:
                        message = "已开启R18模式";
                        SharedPreferencesUtil.saveData(HViewerApplication.mContext, KEY_PREF_MODE_R18_ENABLED, true);
                        break;
                }
            }
            activity.showSnackBar(message);
            pressCount++;
        } else if (preference.getKey().equals(KEY_PREF_ABOUT_GITHUB)
                || preference.getKey().equals(KEY_PREF_ABOUT_CHANGELOG)) {
            String url = preference.getSummary().toString();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                ClipboardManager myClipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                myClipboard.setPrimaryClip(ClipData.newPlainText("github_url", preference.getSummary().toString()));
                activity.showSnackBar("没有可调用的浏览器，网址已复制到剪贴板");
            }
        } else if (preference.getKey().equals(KEY_PREF_ABOUT_QQ_GROUP)) {
            if (!joinQQGroup("xphJHB9LFttGePEHhca0-RiSvdmBINdC")) {
                ClipboardManager myClipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                myClipboard.setPrimaryClip(ClipData.newPlainText("qq_group", preference.getSummary().toString()));
                activity.showSnackBar("群号已复制到剪贴板");
            }
        } else if (preference.getKey().equals(KEY_PREF_ABOUT_DONATE)) {
            final String url = "https://qr.alipay.com/a6x076685fa21yei9s5zbd5";
            Uri content_url = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                ClipboardManager myClipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                myClipboard.setPrimaryClip(ClipData.newPlainText("donate_url", preference.getSummary().toString()));
                activity.showSnackBar("没有可调用的浏览器，网址已复制到剪贴板");
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /****************
     * 发起添加群流程。群号：H-Viewer内测群(590668841) 的 key 为： xphJHB9LFttGePEHhca0-RiSvdmBINdC
     * 调用 joinQQGroup(xphJHB9LFttGePEHhca0-RiSvdmBINdC) 即可发起手Q客户端申请加群 H-Viewer内测群(590668841)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

}
