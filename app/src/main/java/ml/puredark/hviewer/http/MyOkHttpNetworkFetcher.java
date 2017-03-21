package ml.puredark.hviewer.http;

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;

import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by PureDark on 2016/8/27.
 */
public class MyOkHttpNetworkFetcher extends
        BaseNetworkFetcher<MyOkHttpNetworkFetcher.OkHttpNetworkFetchState> {

    private static final String TAG = "OkHttpNetworkFetchProducer";
    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";
    //为了Fresco请求时可以根据不同Uri加不同的header，必须要得有一个全局变量（Fresco的硬伤）
    public static WeakHashMap<Uri, String> headers = new WeakHashMap<>();
    private final OkHttpClient mOkHttpClient;
    private Executor mCancellationExecutor;

    /**
     * @param okHttpClient client to use
     */
    public MyOkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
        mCancellationExecutor = okHttpClient.dispatcher().executorService();
    }

    @Override
    public OkHttpNetworkFetchState createFetchState(
            Consumer<EncodedImage> consumer,
            ProducerContext context) {
        return new OkHttpNetworkFetchState(consumer, context);
    }

    @Override
    public void fetch(final OkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();
        String headerStr = headers.get(uri);
        Request.Builder builder = new Request.Builder();
        try {
            if (headerStr != null && !"".equals(headerStr)) {
                JsonObject object = new JsonParser().parse(headerStr).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    String header = entry.getKey();
                    String value = entry.getValue().getAsString();
                    builder.addHeader(header, value);
                }
            }
        } catch (Exception e) {
        }
        final Request request = builder
                .cacheControl(new CacheControl.Builder().noStore().build())
                .header("User-Agent", HViewerApplication.mContext.getResources().getString(R.string.UA))
                .url(uri.toString())
                .get()
                .build();
        final Call call = mOkHttpClient.newCall(request);

        fetchState.getContext().addCallbacks(
                new BaseProducerContextCallbacks() {
                    @Override
                    public void onCancellationRequested() {
                        if (Looper.myLooper() != Looper.getMainLooper()) {
                            call.cancel();
                        } else {
                            mCancellationExecutor.execute(() -> call.cancel());
                        }
                    }
                });

        call.enqueue(
                new okhttp3.Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        fetchState.responseTime = SystemClock.elapsedRealtime();
                        final ResponseBody body = response.body();
                        try {
                            if (!response.isSuccessful()) {
                                handleException(
                                        call,
                                        new IOException("Unexpected HTTP code " + response),
                                        callback);
                                return;
                            }

                            long contentLength = body.contentLength();
                            if (contentLength < 0) {
                                contentLength = 0;
                            }
                            callback.onResponse(body.byteStream(), (int) contentLength);
                        } catch (Exception e) {
                            handleException(call, e, callback);
                        } finally {
                            try {
                                body.close();
                            } catch (Exception e) {
                                FLog.w(TAG, "Exception when closing response body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        handleException(call, e, callback);
                    }
                });
    }

    @Override
    public void onFetchCompletion(OkHttpNetworkFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override
    public Map<String, String> getExtraMap(OkHttpNetworkFetchState fetchState, int byteSize) {
        Map<String, String> extraMap = new HashMap<>(4);
        extraMap.put(QUEUE_TIME, Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put(FETCH_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put(TOTAL_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put(IMAGE_SIZE, Integer.toString(byteSize));
        return extraMap;
    }

    /**
     * Handles exceptions.
     * <p>
     * <p> OkHttp notifies callers of cancellations via an IOException. If IOException is caught
     * after request cancellation, then the exception is interpreted as successful cancellation
     * and onCancellation is called. Otherwise onFailure is called.
     */
    private void handleException(final Call call, final Exception e, final Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }

    public static class OkHttpNetworkFetchState extends FetchState {
        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public OkHttpNetworkFetchState(
                Consumer<EncodedImage> consumer,
                ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }
}

