package lang.example.lzcache;

import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by lang on 17-1-5.
 */
public class WebViewClientCache extends WebViewClient {
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            WebResourceResponse webResourceResponse = CacheUtil.getRes(url);
            if (webResourceResponse != null) {
                return webResourceResponse;
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String url = request.getUrl().toString();
            WebResourceResponse webResourceResponse = CacheUtil.getRes(url);
            if (webResourceResponse != null) {
                return webResourceResponse;
            }
        }
        return super.shouldInterceptRequest(view, request);
    }
}
