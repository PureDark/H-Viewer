package ml.puredark.hviewer.http;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import q.rorbin.fastimagesize.net.InputStreamProvider;

/**
 * Created by PureDark on 2017/3/14.
 */

public class ImageSizeInputStreamProvider implements InputStreamProvider {
    private String referer, cookie;

    public ImageSizeInputStreamProvider(String referer, String cookie) {
        this.referer = referer;
        this.cookie = cookie;
    }

    @Override
    public InputStream getInputStream(String imagePath) {
        InputStream stream = null;
        try {
            if (imagePath.startsWith("http")) {
                URLConnection connection = new URL(imagePath).openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                if (!TextUtils.isEmpty(referer))
                    connection.setRequestProperty("Referer", referer);
                if (!TextUtils.isEmpty(cookie))
                    connection.setRequestProperty("Cookie", cookie);
                connection.connect();
                stream = connection.getInputStream();
            } else {
                File file = new File(imagePath);
                if (file.exists()) {
                    stream = new FileInputStream(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return stream;
        }
        return stream;
    }
}
