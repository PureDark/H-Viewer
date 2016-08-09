package ml.puredark.hviewer.helpers;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;



import cz.msebera.android.httpclient.Header;
import ml.puredark.hviewer.HViewerApplication;

public class HViewerHttpClient {

    public static void get(String url, String paramsString, OnResponseListener callback){
        String[] paramStrings = paramsString.split("&");
        RequestParams params = new RequestParams();
        for(String paramString : paramStrings){
            String[] pram = paramString.split("=");
            if(pram.length!=2)continue;
            params.put(pram[0], pram[1]);
        }
        getReturnText(url, params, callback);
    }

    public static void get(String url, OnResponseListener callback){
        getReturnText(url, null, callback);
    }
    public static void post(String url, String paramsString, OnResponseListener callback){
        String[] paramStrings = paramsString.split("&");
        RequestParams params = new RequestParams();
        for(String paramString : paramStrings){
            String[] pram = paramString.split("=");
            if(pram.length!=2)continue;
            params.put(pram[0], pram[1]);
        }
        postReturnText(url, params, callback);
    }

    public static void post(String url, RequestParams params, OnResponseListener callback){
        postReturnText(url, params, callback);
    }

    private static void getReturnText(String url, final RequestParams params, final OnResponseListener callBack){
        if(HViewerApplication.isNetworkAvailable())
            HttpClient.get(url, params, new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    callBack.onSuccess(responseString);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    callBack.onFailure(new HttpError(1009));
                }

            });
        else
            callBack.onFailure(new HttpError(1009));
    }

    private static void postReturnText(String url, final RequestParams params, final OnResponseListener callBack){
        if(HViewerApplication.isNetworkAvailable())
            HttpClient.post(url, params, new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    callBack.onSuccess(responseString);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    callBack.onFailure(new HttpError(1009));
                    Log.d("HViewerHttpClient", responseString);
                }

            });
        else
            callBack.onFailure(new HttpError(1009));
    }


    public static class HttpClient {
        private static AsyncHttpClient client = new AsyncHttpClient();

        public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(url, params, responseHandler);
        }

        public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.post(url, params, responseHandler);
        }

    }

    public interface OnResponseListener {
        void onSuccess(String result);
        void onFailure(HttpError error);
    }


    //对错误码的预定义
    public static class HttpError{
        //错误码常量定义
        public static final int ERROR_UNKNOWN                = 1000;  //未知错误
        public static final int ERROR_NETWORK                = 1009;  //网络错误

        private int errorCode;
        private String errorString = "";

        public HttpError(int errorCode){
            this.errorCode = errorCode;
            switch(errorCode){
                case ERROR_UNKNOWN:errorString="未知错误";break;
                case ERROR_NETWORK:errorString="网络错误，请重试";break;
                default:errorString="未定义的错误码";break;
            }
        }
        public int getErrorCode(){
            return this.errorCode;
        }
        public String getErrorString(){
            return this.errorString;
        }
        @Override
        public String toString(){
            return errorCode + " : " + errorString;
        }
    }
}
