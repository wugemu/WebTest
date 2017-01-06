package lang.example.webtest.util.packcache;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import lang.example.webtest.model.CheckFileModel;
import lang.example.webtest.model.VersionItemModel;
import lang.example.webtest.model.VersionModel;
import okhttp3.Request;

/**
 * Created by lang on 16-12-19.
 */
public class PackageManageService extends Service {
    //文件保存地址
    public static String dirName = "/webtest/";
    public static String fileDir = "/sdcard/webtest/";
    public static String versionFile="version.json";

    //获取版本信息
    public static String serverVersion;
    public static String clientVersionStr;
    private static VersionModel serverVersionModel;
    //本地缓存机制是否开启
    public static boolean isOpenCache=true;

    //下载管理
    private static DownloadManager downManager;
    private static DownLoadCompleteReceiver receiver;

    public  static  Handler handler;

    public  static Handler myhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    //下载完成解压
                    unZip();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        initData();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
//        if(receiver!=null) {
//            unregisterReceiver(receiver);
//        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    public static void initData(){
        if(!TextUtils.isEmpty(serverVersion)){
            //获取服务器版本json
            String mh = JsonParseUtil.getStringValue(serverVersion, "MH");
            serverVersionModel=JsonParseUtil.getVersionModel(mh);
            readVersion();
        }
        //检测包
        if(serverVersionModel!=null){
            isOpenCache=serverVersionModel.getOpenCache()==1?true:false;
            if(isOpenCache){
                //检测基础包
                VersionItemModel baseLink=serverVersionModel.getBaseLink();
                checkPackage(baseLink);
                //检测活动包
                for(int i=0;i<serverVersionModel.getActivityView().size();i++){
                    VersionItemModel activityModel=serverVersionModel.getActivityView().get(i);
                    checkPackage(activityModel);
                }
            }
        }else{
            isOpenCache=false;
        }
    }

    //检测包
    public static boolean checkPackage(VersionItemModel itemModel){
        if(itemModel!=null){

            String packageName=itemModel.getPackageName();
            String host=serverVersionModel.getDownUrl();

            for (CheckFileModel checkfiles :
                    itemModel.getCheckFiles()) {
                String path = fileDir +itemModel.getDirName()+checkfiles.getFileUrl();
                File dir = new File(path);
                if (!dir.exists()) {
                    //包文件缺失
                    downHtml(host + packageName, packageName);
                    return false;
                }
            }
            //包完整 检测更新
            if(WebUtils.isUpdatePackage(packageName, itemModel.getVersion(), clientVersionStr)){
                //有更新
                downHtml(host + packageName, packageName);
            }
            return true;
        }
        return true;
    }
    //获取本地版本文本信息
    public static  void readVersion(){
        String path = fileDir +versionFile;
        File file = new File(path);
        clientVersionStr="";
        if (file.exists()) {
            try {
                String versionStr = "/mnt"  + fileDir + versionFile;
                FileInputStream fin = new FileInputStream(versionStr);
                int length = fin.available();

                byte [] buffer = new byte[length];

                fin.read(buffer);

                clientVersionStr =new String(buffer,"GB2312");

                fin.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void downHtml(String downUrl,String fileName){
        // 获取SD卡路径
        String path = WebUtils.getImgFileDir()
                + dirName;
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        int threadNum = 5;
        DownloadTask task = new DownloadTask(downUrl, threadNum, path+fileName,myhandler);
        task.start();
    }

    private static void unZip(){
        String path = fileDir;
        File dir = new File(path);
        final File[] files = dir.listFiles();
        for (File file:files
                ) {
            String fileName=file.getName();
            if(fileName.contains(".zip")){
                try {
                    WebUtils.upZipFile(file, fileDir);
                    handler.sendEmptyMessage(0);//发送解压完成消息
                }catch (Exception e){
                    Log.d("0.0", e + "");
                }
            }
        }
    }

    private class DownLoadCompleteReceiver extends BroadcastReceiver {
        private Handler handler;
        public  DownLoadCompleteReceiver(Handler handler){
            this.handler=handler;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Toast.makeText(context, "下载任务已完成！", Toast.LENGTH_SHORT).show();
                String path = fileDir;
                File dir = new File(path);
                final File[] files = dir.listFiles();
                for (File file:files
                        ) {
                    String fileName=file.getName();
                    if(fileName.contains(".zip")){
                        try {
                            WebUtils.upZipFile(file, fileDir);
                            if(handler!=null){
                                handler.sendEmptyMessage(0);//发送解压完成消息
                            }
                        }catch (Exception e){
                            Log.d("0.0", e + "");
                        }
                    }
                }
            } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                Toast.makeText(context, "别瞎点！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void getHtmlUrl(final WebView web_view,String loadUrl,Context context){
        if(!PackageManageService.isOpenCache){
            //本地缓存机制未开启
            web_view.loadUrl(loadUrl);
            return;
        }
        /*
          1. 如果是.html的返回，则直接加载，无须寻找对应资源包
          2. 如果是.shtml并且有content参数的话，取参数后的内容作为对应活动页面路径
          3. 如果是.shtml且没有content参数的话，直接取.shtml的文件名作为对应活动页面路径
        */
        String indexName="";
        if(loadUrl.contains(".html")){
            int index=loadUrl.lastIndexOf("/");
            int lastindex=loadUrl.indexOf(".html");
            indexName=loadUrl.substring(index+1,lastindex);
        }
        if(loadUrl.contains(".shtml")){
            if(loadUrl.contains("content=")){
                int index=loadUrl.indexOf("content=")+8;
                indexName=loadUrl.substring(index);
            }else {
                int index=loadUrl.lastIndexOf("/");
                int lastindex=loadUrl.indexOf(".shtml");
                indexName=loadUrl.substring(index+1,lastindex);
            }
        }

        VersionItemModel itemModel=WebUtils.getItemModel(indexName,PackageManageService.clientVersionStr);
        if(itemModel==null){
            web_view.loadUrl(loadUrl);
            return ;
        }
        if(!PackageManageService.checkPackage(itemModel)){
            //包不完整
            web_view.loadUrl(loadUrl);
            return ;
        }
        String path = PackageManageService.fileDir +itemModel.getDirName()+itemModel.getMainUrl();
        File dir = new File(path);
        if (dir.exists()) {
            //本地活动页面存在
            Toast.makeText(context, "加载本地html", Toast.LENGTH_LONG).show();
            final String indexStr="file:///mnt"+path;
            HttpU.getInstance().get(context, "http://test.mihui365.com/Farm_Mobile/activeAction!getProductList46.action?classid=1,9,10,3,8,4,7,5&productTypes=133,134,135,136,137,138,139,140",null, new HttpCallback() {

                @Override
                public void onBefore(Request request) {
                    web_view.loadUrl(indexStr);
                }

                @Override
                public void onResponse(String response) {
                    String htmldate="{\"showFlag\":4,\"now_time\":1482309397610,\"killList1\":[],\"killList0\":[],\"killList3\":[],\"killList2\":[],\"stockFlag\":4,\"killList5\":[],\"killList4\":[],\"killList7\":[],\"killList6\":[],\"message\":\"成功\",\"list4\":[{\"PRODUCT_TITLE\":\"KATE|遮瑕BB霜 30g\",\"PRICE\":159,\"PRODUCT_ID\":791,\"couponPolicy_desc\":\"\",\"PRODUCT_NAME\":\"KATE/凯朵  均润矿物完美遮瑕粉底乳BB霜 30g\",\"seckillStock\":0,\"brandName\":\"\",\"sale_tag_id\":0,\"SALE_PRICE\":99,\"postage\":0,\"SALETIME_START\":\"2016-11-18 10:00:00.0\",\"SALETIME_END\":\"2016-11-18 15:00:00.0\",\"brandAlias\":\"\",\"sku_id\":0,\"LOGISTICMODE_ID\":\"002\",\"TYPE\":\"\",\"IMAGEURL_BOUTIQUE\":\"http://image.mihui365.com/boutiqueImg/19116171673748357.jpg\",\"NEW_PRODUCT\":\"\",\"sku_ids\":\"\",\"charges\":0,\"IMAGEURL_SMALL\":\"http://image.mihui365.com/smallImg/5733111280888929.jpg\",\"sale_tag_name\":\"\",\"CLASSID\":\"\",\"product_oneWord\":\"日本殿堂级BB霜\",\"sumSkuNum\":239,\"key_point\":\"\",\"SELLINGPOINTDESC\":\"\",\"REAL_PRICE\":99,\"IMAGEURL_BIG\":\"http://image.mihui365.com/bigImg/19115617384669134.jpg\",\"PRODUCT_DESC\":\"这款BB霜较以往的BB增加了矿物成分和胶原蛋白成分，使其具有更强力的修复和保护毛孔能力，覆盖性更强，更薄。而且它是一款集乳液、美容液、乳霜、防晒、粉饼功效为一体，能够打造立体时尚肌肤的BB霜！\\n亮肤色OC-B适合想要提亮皮肤色的MM；\\n自然色OC-C适合追求自然的肤色的MM 。\",\"clas\":\"\",\"introduce_text\":\"\",\"ifSale\":\"\",\"COUPON_DESC\":\"\",\"show_type\":\"\",\"totalMoney\":0},{\"PRODUCT_TITLE\":\"kiss me|睫毛膏 2选1\",\"PRICE\":138,\"PRODUCT_ID\":167,\"couponPolicy_desc\":\"\",\"PRODUCT_NAME\":\"Kiss me 花漾美姬 浓密/纤长 睫毛膏2选1\",\"seckillStock\":0,\"brandName\":\"\",\"sale_tag_id\":0,\"SALE_PRICE\":68,\"postage\":0,\"SALETIME_START\":\"2016-12-01 15:00:00.0\",\"SALETIME_END\":\"2016-12-01 20:00:00.0\",\"brandAlias\":\"\",\"sku_id\":0,\"LOGISTICMODE_ID\":\"002\",\"TYPE\":\"\",\"IMAGEURL_BOUTIQUE\":\"http://image.mihui365.com/boutiqueImg/9878137071753111.jpg\",\"NEW_PRODUCT\":\"\",\"sku_ids\":\"\",\"charges\":0,\"IMAGEURL_SMALL\":\"http://image.mihui365.com/smallImg/10615966946221851.jpg\",\"sale_tag_name\":\"\",\"CLASSID\":\"\",\"product_oneWord\":\"在睫毛上荡秋千\",\"sumSkuNum\":46,\"key_point\":\"\",\"SELLINGPOINTDESC\":\"\",\"REAL_PRICE\":68,\"IMAGEURL_BIG\":\"http://image.mihui365.com/bigImg/9876547382323281.jpg\",\"PRODUCT_DESC\":\"熟悉日系彩妆的亲肯定爱死这款睫毛膏，膏体不是很水，但是却能刷出根根分明的睫毛，重要的是持妆效果很好。定型能力很好，并且超级防水。此款分为纤长和浓密两款。\\n纤长款能提升睫毛膏纤长度。\\n浓密款能提升睫毛膏浓密度。\",\"clas\":\"\",\"introduce_text\":\"\",\"ifSale\":\"\",\"COUPON_DESC\":\"\",\"show_type\":\"\",\"totalMoney\":0},{\"PRODUCT_TITLE\":\"DHC|睫毛增长液 6.5ml\",\"PRICE\":158,\"PRODUCT_ID\":61,\"couponPolicy_desc\":\"\",\"PRODUCT_NAME\":\"DHC 睫毛增长液/修护液 6.5ml \",\"seckillStock\":0,\"brandName\":\"\",\"sale_tag_id\":0,\"SALE_PRICE\":78,\"postage\":0,\"SALETIME_START\":\"2016-12-15 20:00:00.0\",\"SALETIME_END\":\"2016-12-16 08:00:00.0\",\"brandAlias\":\"\",\"sku_id\":0,\"LOGISTICMODE_ID\":\"002\",\"TYPE\":\"\",\"IMAGEURL_BOUTIQUE\":\"http://image.mihui365.com/skuImg/7939425079219197.png\",\"NEW_PRODUCT\":\"\",\"sku_ids\":\"\",\"charges\":0,\"IMAGEURL_SMALL\":\"http://image.mihui365.com/smallImg/9151178970725394.jpg\",\"sale_tag_name\":\"\",\"CLASSID\":\"\",\"product_oneWord\":\"让假睫毛无地自容\",\"sumSkuNum\":184,\"key_point\":\"\",\"SELLINGPOINTDESC\":\"\",\"REAL_PRICE\":78,\"IMAGEURL_BIG\":\"http://image.mihui365.com/bigImg/19437594215565074.jpg\",\"PRODUCT_DESC\":\"连续3年获得日本睫毛保养品票投冠军！可用于睫毛膏底液、夜间睫毛护理，睫毛烫后修护等多种用途，弱酸性质地，温和不刺激眼周。作为睫毛膏底液使用，可使睫毛更加浓密。贴合眼睑弧度的弧线型刷头能刷遍每一根睫毛，演绎出令人印象深刻的双眸~\",\"clas\":\"\",\"introduce_text\":\"\",\"ifSale\":\"\",\"COUPON_DESC\":\"\",\"show_type\":\"\",\"totalMoney\":0}]}";
//                    Gson gson=new Gson();
//                    htmldate=gson.toJson(htmldate);
                    Log.d("0.0","json---"+htmldate);
                    web_view.loadUrl("javascript:initJsonPFromApp("+htmldate+")");
                }

                @Override
                public void onAfter() {
                    super.onAfter();
                }
            });
            return  ;
        }else {
            web_view.loadUrl(loadUrl);
            return ;
        }
    }
}
