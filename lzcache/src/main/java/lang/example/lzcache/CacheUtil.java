package lang.example.lzcache;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.MalformedInputException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;


/**
 * Created by lang on 17-1-3.
 */
public class CacheUtil {
    public static String appCacheDir;
    public static long cacheMax;
    public static List<String> noCacheList;
    public static List<String> cacheList;
    public static boolean readCache;
    //默认所有页面做缓存处理
    public static void initAllCache(long cacheMax,List<String> noCacheList){
        CacheUtil.noCacheList=noCacheList;

        init(cacheMax,true);
    }
    //默认所有页面不做缓存处理
    public static void initOnlyCache(long cacheMax,List<String> cacheList){
        CacheUtil.cacheList=cacheList;

        init(cacheMax, false);
    }
    public static void init(long cacheMax, boolean readCache){
        CacheUtil.cacheMax=cacheMax;
        CacheUtil.readCache=readCache;
        if(CacheUtil.noCacheList==null){
            CacheUtil.noCacheList=new ArrayList<String>();
        }
        if(CacheUtil.cacheList==null){
            CacheUtil.cacheList=new ArrayList<String>();
        }
        appCacheDir = getCacheDir() + "/webtest/cache";
        if(CacheUtil.noCacheList==null){
            CacheUtil.noCacheList=new ArrayList<String>();
        }
        File fileSD = new File(appCacheDir);
        if (!fileSD.exists()) {
            fileSD.mkdirs();
        }

        long nowCarche=getFolderSize(fileSD);
        Log.e("0.0", "缓存大小:" + getFormatSize(nowCarche));
        if(nowCarche>cacheMax){
            clearCache(appCacheDir, true);
        }
    }

    public static String getCacheDir()
    {
        String imageDir=null;
        if(imageDir==null)
        {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)){
                imageDir = Environment.getExternalStorageDirectory().getPath();
            }else {
                imageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
        return imageDir;
    }

    /**
     * 删除指定目录下文件及目录
     * @param deleteThisPath
     * @return
     */
    public static void clearCache(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        clearCache(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    /**
     * 加密解密算法 执行一次加密，两次解密
     */
    public static String convertMD5(String inStr){

        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++){
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;

    }

    //这里面读写操作比较多，还有截取那两个属性的字符串稍微有点麻烦
    /** * int转byte * by黄海杰 at：2015-10-29 16:15:06 * @param iSource * @param iArrayLen * @return */
    public static byte[] toByteArray(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }
        return bLocalArr;
    }

    /** * byte转int * by黄海杰 at：2015-10-29 16:14:37 * @param bRefArr * @return */
    // 将byte数组bRefArr转为一个整数,字节数组的低位是整型的低字节位
    public static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    /** * 写入JS相关文件 * by黄海杰 at:2015-10-29 16:14:01 * @param output * @param str */
    public static void writeBlock(OutputStream output, String str) {
        try {
            byte[] buffer = str.getBytes("utf-8");
            int len = buffer.length;
            byte[] len_buffer = toByteArray(len, 4);
            output.write(len_buffer);
            output.write(buffer);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /** * 读取JS相关文件 * by黄海杰 at:2015-10-29 16:14:19 * @param input * @return */
    public static String readBlock(InputStream input) {
        try {
            byte[] len_buffer = new byte[4];
            input.read(len_buffer);
            int len = toInt(len_buffer);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int read_len = 0;
            byte[] buffer = new byte[len];
            while ((read_len = input.read(buffer)) > 0) {
                len -= read_len;
                output.write(buffer, 0, read_len);
                if (len <= 0) {
                    break;
                }
            }
            buffer = output.toByteArray();
            output.close();
            return new String(buffer,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WebResourceResponse getRes(String url){
        boolean readCache=CacheUtil.readCache;//是否读取缓存
        for(String extraStr:noCacheList){
            if(url.contains(extraStr)){
                readCache=false;
                break;
            }
        }
        for (String cacheStr:cacheList){
            if(url.contains(cacheStr)){
                readCache=true;
                break;
            }
        }

        //todo:计算url的hash
        String md5URL = CacheUtil.string2MD5(url);
        if(url.contains("sttype")||url.contains("stkey")){
            //ajax 请求时间戳每次不同去除掉
            int end=url.indexOf("sttype");
            md5URL = CacheUtil.string2MD5(url.substring(0,end));
        }
        //读取缓存的html页面
        File file = new File(appCacheDir + File.separator + md5URL);
        if(readCache) {
            if (file.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    Log.e(">>>>>>>>>", "读缓存-----"+url);
                    return new WebResourceResponse(CacheUtil.readBlock(fileInputStream), CacheUtil.readBlock(fileInputStream), fileInputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        try {
            if (readCache) {
                URL uri = new URL(url);
                URLConnection connection = uri.openConnection();
                InputStream uristream = connection.getInputStream();
                //String cache = "1";//connection.getHeaderField("Ddbuild-Cache");   //是否读缓存
                String contentType = connection.getContentType();
                //text/html; charset=utf-8
                String mimeType = "";
                String encoding = "";
                if (contentType != null && !"".equals(contentType)) {
                    if (contentType.indexOf(";") != -1) {
                        String[] args = contentType.split(";");
                        mimeType = args[0];
                        String[] args2 = args[1].trim().split("=");
                        if (args.length == 2 && args2[0].trim().toLowerCase().equals("charset")) {
                            encoding = args2[1].trim();
                        } else {

                            encoding = "utf-8";
                        }
                    } else {
                        mimeType = contentType;
                        encoding = "utf-8";
                    }
                }
                //todo:缓存uristream
                FileOutputStream output = new FileOutputStream(file);
                int read_len;
                byte[] buffer = new byte[1024];


                CacheUtil.writeBlock(output, mimeType);
                CacheUtil.writeBlock(output, encoding);
                while ((read_len = uristream.read(buffer)) > 0) {
                    output.write(buffer, 0, read_len);
                    Log.e(">>>>>>>>>", "写缓存"+url);
                }
                output.close();
                uristream.close();

                FileInputStream fileInputStream = new FileInputStream(file);
                CacheUtil.readBlock(fileInputStream);
                CacheUtil.readBlock(fileInputStream);
                Log.e(">>>>>>>>>", "网络加载:"+url);
                return new WebResourceResponse(mimeType, encoding, fileInputStream);
            } else {
                Log.e(">>>>>>>>>", "网络加载:"+url);
                return null;
            }

        } catch (MalformedInputException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取文件夹大小
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file){

        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    size = size + getFolderSize(fileList[i]);

                }else{
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size/1024;
        if(kiloByte < 1) {
            return size + "Byte(s)";
        }

        double megaByte = kiloByte/1024;
        if(megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte/1024;
        if(gigaByte < 1) {
            BigDecimal result2  = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte/1024;
        if(teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }


    public  static  void loadUrl(final WebView webView,Context context,final String url,Map<String, String> headMap){
        final String content=readHtml(url);
        HttpU.getInstance().get(context, url,headMap, new HttpCallback() {
            @Override
            public void onBefore(Request request) {
                if(!TextUtils.isEmpty(content)){
                    webView.loadDataWithBaseURL(appCacheDir, content, "text/html;charset=utf-8",null,null);
                    Log.e("0.0", "缓存html加载"+url);
                }
                super.onBefore(request);
            }

            @Override
            public void onAfter() {
                super.onAfter();
            }

            @Override
            public void onResponse(final String response) {
                webView.loadDataWithBaseURL(appCacheDir, response, "text/html;charset=utf-8",null,null);
                Log.e("0.0", "网络html加载"+url);
                saveHtml(url,response);
            }
        });
    }

    public static void saveHtml(String url,String content){
        //todo:计算url的hash
        String md5URL = CacheUtil.string2MD5(url);
        //读取缓存的html页面
        File file = new File(appCacheDir + File.separator + md5URL);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            OutputStream os=new BufferedOutputStream(new FileOutputStream(file));
            os.write(content.getBytes());
            os.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String readHtml(String url){
        String content="";
        String md5URL = CacheUtil.string2MD5(url);
        //读取缓存的html页面
        File file = new File(appCacheDir + File.separator + md5URL);
        if(file.exists()){
            try {
                String versionStr =  appCacheDir + File.separator + md5URL;
                FileInputStream fin = new FileInputStream(versionStr);
                int length = fin.available();

                byte [] buffer = new byte[length];

                fin.read(buffer);

                content =new String(buffer,"utf-8");
                fin.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return content;
    }
}
