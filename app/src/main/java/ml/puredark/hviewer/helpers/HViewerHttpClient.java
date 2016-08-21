package ml.puredark.hviewer.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.bitmap;

public class HViewerHttpClient {
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private static OkHttpClient mClient = new OkHttpClient.Builder()
        .cookieJar(new CookieJar() {
            private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url, cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url);
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        }).build();

    public static void post(String url, String paramsString, List<Cookie> cookies, final OnResponseListener callback) {
        String[] paramStrings = paramsString.split("&");
        FormBody.Builder formBody = new FormBody.Builder();
        for (String paramString : paramStrings) {
            String[] pram = paramString.split("=");
            if (pram.length != 2) continue;
            formBody.add(pram[0], pram[1]);
        }
        RequestBody requestBody = formBody.build();
        post(url, requestBody, cookies, callback);
    }

    public static void get(String url, List<Cookie> cookies, final OnResponseListener callback) {
        if(url==null||!url.startsWith("http")) {
            callback.onFailure(new HttpError(HttpError.ERROR_WRONG_URL));
            return;
        }
        if (HViewerApplication.isNetworkAvailable()) {
            HRequestBuilder builder = new HRequestBuilder();
            if(cookies!=null){
                String cookieString = "";
                for(Cookie cookie : cookies){
                    cookieString += cookie.name()+"="+cookie.value()+"; ";
                }
                builder.addHeader("cookie", cookieString);
            }
            Request request = builder
                    .url(url)
                    .build();
            mClient.newCall(request).enqueue(new HCallback() {
                @Override
                void onFailure(IOException e) {
                    callback.onFailure(new HttpError(HttpError.ERROR_NETWORK));
                }

                @Override
                void onResponse(String contentType, Object body) {
                    callback.onSuccess(contentType, body);
                }
            });
        } else {
            callback.onFailure(new HttpError(HttpError.ERROR_NETWORK));
        }
    }

    public static void post(String url, RequestBody body, List<Cookie> cookies, final OnResponseListener callback) {
        if(url==null||!url.startsWith("http")) {
            callback.onFailure(new HttpError(HttpError.ERROR_WRONG_URL));
            return;
        }
        if (HViewerApplication.isNetworkAvailable()) {
            HRequestBuilder builder = new HRequestBuilder();
            if(cookies!=null){
                String cookieString = "";
                for(Cookie cookie : cookies){
                    cookieString += cookie.name()+"="+cookie.value()+"; ";
                }
                builder.addHeader("cookie", cookieString);
            }
            Request request = builder
                    .url(url)
                    .post(body)
                    .build();
            mClient.newCall(request).enqueue(new HCallback() {
                @Override
                void onFailure(IOException e) {
                    callback.onFailure(new HttpError(HttpError.ERROR_NETWORK));
                }

                @Override
                void onResponse(String contentType, Object body) {
                    callback.onSuccess(contentType, body);
                }
            });
        } else {
            callback.onFailure(new HttpError(HttpError.ERROR_NETWORK));
        }
    }

    public interface OnResponseListener {
        void onSuccess(String contentType, Object result);

        void onFailure(HttpError error);
    }

    // UI Thread Handler
    public static abstract class HCallback implements Callback {
        abstract void onFailure(IOException e);

        abstract void onResponse(String contentType, Object body);

        @Override
        public void onFailure(Call call, final IOException e) {
            e.printStackTrace();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onFailure(e);
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String contentType = response.header("Content-Type");
            final Object body;
            if(contentType.contains("image")){
                body = BitmapFactory.decodeStream(response.body().byteStream());
            }else{
                byte[] b = response.body().bytes();
                String charset = getCharset(new String(b));
                body = new String(b, charset);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onResponse(contentType, body);
                }
            });
        }
    }

    public static String getCharset(String html){
        Document doc = Jsoup.parse(html);
        Element e = doc.getElementsByTag("meta").first();
        if(e != null){
            String content;
            String charset;
            if(e.attr("content") != null && e.attr("content") != ""){
                content = e.attr("content");
                charset = content.substring(content.indexOf("=")+1);
            }
            else if(e.attr("charset") != null && e.attr("charset") != "")
                charset = e.attr("charset");
            else
                charset = "utf-8";
            Log.d("RuleParser", charset);
            return charset;
        }else{
            return "utf-8";
        }

    }

    // Pre-define error code
    public static class HttpError {
        // Error code constants
        public static final int ERROR_UNKNOWN = 1000;  //未知错误
        public static final int ERROR_NETWORK = 1009;  //网络错误
        public static final int ERROR_WRONG_URL = 1011;  //URL格式错误

        private int errorCode;
        private String errorString = "";

        public HttpError(int errorCode) {
            this.errorCode = errorCode;
            switch (errorCode) {
                case ERROR_UNKNOWN:
                    errorString = "未知错误";
                    break;
                case ERROR_NETWORK:
                    errorString = "网络错误，请重试";
                    break;
                case ERROR_WRONG_URL:
                    errorString = "URL格式错误";
                    break;
                default:
                    errorString = "未定义的错误码";
                    break;
            }
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        public String getErrorString() {
            return this.errorString;
        }

        @Override
        public String toString() {
            return errorCode + " : " + errorString;
        }
    }
}
