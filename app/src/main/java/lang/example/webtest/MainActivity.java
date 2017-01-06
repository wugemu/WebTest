package lang.example.webtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;


import lang.example.webtest.util.packcache.ArticleWebView;
import lang.example.webtest.util.packcache.PackageManageService;
import lang.example.webtest.util.packcache.TextData;

/**
 * html5离线包加载优化
 */
public class MainActivity extends AppCompatActivity {

    private ArticleWebView web_view;


    public  Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    //解压完成 将版本信息读取到缓存中
                    PackageManageService.readVersion();
                    web_view.reload();
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button fab1 = (Button) findViewById(R.id.fab1);
        Button fab2 = (Button) findViewById(R.id.fab2);
        Button fab3 = (Button) findViewById(R.id.fab3);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //点击banner
                PackageManageService.getHtmlUrl(web_view, TextData.bannerUrl, MainActivity.this);
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //此处模拟版本升级
                //web_view.loadUrl("javascript:initJsonPFromApp('sdfs')");
                web_view.loadUrl("javascript: initJsonP()");
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //加载网页
                web_view.loadUrl(TextData.bannerUrl);
            }
        });

        web_view=(ArticleWebView)findViewById(R.id.web_view);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        web_view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        WebSettings setting = web_view.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setDefaultTextEncodingName("UTF-8");
        web_view.setWebChromeClient(new WebChromeClient());
        web_view.setWebViewClient(new WebViewClient(){
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

        initData();
    }

    public void initData(){
        //开启服务
        Intent i = new Intent(MainActivity.this, PackageManageService.class);
        PackageManageService.handler=handler;
        PackageManageService.serverVersion=TextData.hostServer;
        MainActivity.this.startService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, PackageManageService.class);
        stopService(intent);
    }
}
