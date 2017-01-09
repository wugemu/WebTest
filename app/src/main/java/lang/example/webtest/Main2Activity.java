package lang.example.webtest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

import lang.example.lzcache.CacheUtil;
import lang.example.lzcache.WebViewClientCache;
import lang.example.webtest.util.packcache.ArticleWebView;

/**
 * 缓存机制优化
 */
public class Main2Activity extends AppCompatActivity {
    private ArticleWebView web_view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        web_view2=(ArticleWebView)findViewById(R.id.web_view2);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        WebSettings setting = web_view2.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setDefaultTextEncodingName("UTF-8");

        List<String> cacheList=new ArrayList<String>();//不缓存的资源
        cacheList.add(".js");
        cacheList.add(".css");
        cacheList.add(".ico");
        cacheList.add(".png");//图片
        cacheList.add(".jpg");
        //cacheList.add("/activitAll.shtml");//活动页 变动频繁的不进行缓存
        cacheList.add(".action?");//页面请求
        //初始化
        CacheUtil.initOnlyCache("/webtest/cache2",512 * 1024 * 1024, cacheList);//最大缓存512MB


        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        web_view2.setWebChromeClient(new WebChromeClient());

        web_view2.setWebViewClient(new WebViewClientCache() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            //final String url = "http://192.168.1.168:8080/WebTest/activityV2.html";
            final String url = "http://m.jd.com/";
            @Override
            public void onClick(View view) {
                //使用此方法需要跨域 优化首次加载方式
                //CacheUtil.loadUrl(web_view2,Main2Activity.this,url, null);
                web_view2.loadUrl(url);
            }
        });
    }

}
