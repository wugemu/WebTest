# WebTest
android html5页面加载缓存优化

Cache使用：
Application中初始化 代码如下：
								//初始化
        List<String> cacheList=new ArrayList<String>();//缓存的资源
        cacheList.add(".js");
        cacheList.add(".css");
        cacheList.add(".ico");
        cacheList.add(".png");//图片
        cacheList.add(".jpg");
        cacheList.add(".gif");
        cacheList.add(".action?");//页面请求
        CacheUtil.initOnlyCache(512 * 1024 * 1024, cacheList);//最大缓存512MB
注：讨论 哪些数据进行缓存

initOnlyCache(long cacheMax,List<String> cacheList) 
cacheMax最大缓存容量，只对cacheList中的文件进行缓存（字符串包含）
initAllCache(long cacheMax,List<String> noCacheList)
cacheMax最大缓存容量，只对noCacheList中的文件不进行缓存（字符串包含）

1首次加载优化
CacheUtil.loadUrl(WebView webview, Content content, String url, Map<String ,String> headpara);
该方法实现webview加载与 html内容请求异步，html内容获取后会立即展示，无需等待资源加载完成，并缓存到本地可同步更新，使用此方法可以实现离线模式(缓存文件完全)。
注： html5中有请求时，需要实现跨域设置；
2缓存机制优化
webView.setWebViewClien(new WebViewClientCache());
示例代码如下：
webView.setWebViewClient(new WebViewClientCache() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
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
            }   });
