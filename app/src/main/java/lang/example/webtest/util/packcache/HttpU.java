package lang.example.webtest.util.packcache;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 1 on 2016/1/18.
 */
public class HttpU {
    private static HttpU mInstance;
    private ExecutorService executorService = Executors.newFixedThreadPool(5); // 固定五个线程来执行任务
    private final Handler handler;
    private OkHttpClient mOkHttpClient;
    private HttpU() {
        mOkHttpClient = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
    }


    public static HttpU getInstance() {
        if (mInstance == null) {
            synchronized (HttpU.class) {
                if (mInstance == null) {
                    mInstance = new HttpU();
                }
            }
        }
        return mInstance;
    }

    /**
     * 网络请求方法
     *
     * @param context
     * @param url
     * @param callback
     */
    public void get(final Context context, final String url, Map<String, String> headMap,final HttpCallback callback) {


        Request.Builder requestBuilder = new Request.Builder();

        if (headMap != null && headMap.size() > 0) {
            Iterator iter = headMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                if (entry != null) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        requestBuilder.addHeader(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        final Request request = requestBuilder.url(url).build();

        Log.d("0.0", "请求报文Host：" + url);

        callback.onBefore(request);

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(request, e, context);
                        callback.onAfter();
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    final String result = response.body().string();
                    Log.d("0.0", "返回报文Host：" + url);
                    Log.d("0.0", "返回报文body：" + result);
                    Log.d("0.0", "返回报文code：" + response.code());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(result);
                            callback.onAfter();
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(request, e, context);
                            callback.onAfter();
                        }
                    });
                }
            }
        });
    }
}
