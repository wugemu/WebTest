package lang.example.webtest.util.packcache;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import lang.example.webtest.model.VersionItemModel;

/**
 * Created by lang on 16-12-1.
 */
public class WebUtils {

    public static String getImgFileDir()
    {
        String imageDir=null;
        if(imageDir==null)
        {
            if (android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED)){
                imageDir =Environment.getExternalStorageDirectory().getPath();
            }else {
                imageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
        return imageDir;
    }

    /**
     * 解压缩一个文件
     *
     * @param zipFile 要解压的压缩文件
     * @param folderPath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static void upZipFile(File zipFile, String folderPath) throws ZipException, IOException
    {
        ZipFile zfile=new ZipFile(zipFile);
        Enumeration zList=zfile.entries();
        ZipEntry ze=null;
        byte[] buf=new byte[1024];
        while(zList.hasMoreElements()){
            ze=(ZipEntry)zList.nextElement();
            if(ze.isDirectory()){
                Log.d("0.0", "dir.getName() = " + ze.getName());
                String dirstr = folderPath + ze.getName();
                //dirstr.trim();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                Log.d("0.0", "str = "+dirstr);
                File f=new File(dirstr);
                f.mkdir();
                continue;
            }
            Log.d("0.0", "file.getName() = "+ze.getName());
            OutputStream os=new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is=new BufferedInputStream(zfile.getInputStream(ze));
            int readLen=0;
            while ((readLen=is.read(buf, 0, 1024))!=-1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        zipFile.delete();
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     * @param baseDir 指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs=absFileName.split("/");
        File ret=new File(baseDir);
        String substr = null;
        if(dirs.length>1){
            for (int i = 0; i < dirs.length-1;i++) {
                substr = dirs[i];
                try {
                    //substr.trim();
                    substr = new String(substr.getBytes("8859_1"), "GB2312");

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ret=new File(ret, substr);

            }
            Log.d("upZipFile", "1ret = "+ret);
            if(!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length-1];
            try {
                //substr.trim();
                substr = new String(substr.getBytes("8859_1"), "GB2312");
                Log.d("upZipFile", "substr = "+substr);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(TextUtils.isEmpty(substr)){
            substr=absFileName;
        }
        ret=new File(ret, substr);
        Log.d("upZipFile", "2ret = " + ret);
        if(!ret.exists()){
            try {
                ret.createNewFile();
            }catch (Exception e){
                Log.d("0.0", e + "");
            }
        }
        return ret;
    }

    //检测包更新
    public static boolean isUpdatePackage(String packageName,float serVersion,String cliVersion){
        Gson gson=new Gson();
        boolean isUpdate=false;
        if(!TextUtils.isEmpty(cliVersion)){
            String mh = JsonParseUtil.getStringValue(cliVersion, "MH");
            if(!TextUtils.isEmpty(mh)){
                String packageStr=JsonParseUtil.getStringValue(mh, packageName);
                if(!TextUtils.isEmpty(packageStr)){
                    VersionItemModel itemModel=gson.fromJson(packageStr,VersionItemModel.class);
                    if(itemModel!=null){
                        if(itemModel.getVersion()<serVersion){
                            isUpdate=true;
                        }
                    }else{
                        isUpdate=true;
                    }
                }
            }
        }
        return isUpdate;
    }

    //获取html包信息
    public  static  VersionItemModel getItemModel(String packageNmae,String cliVersion){
        Gson gson=new Gson();
        VersionItemModel itemModel=null;
        if(!TextUtils.isEmpty(packageNmae)){
            String mh = JsonParseUtil.getStringValue(cliVersion, "MH");
            if(!TextUtils.isEmpty(mh)){
                String packageStr=JsonParseUtil.getStringValue(mh, packageNmae+".zip");
                if(!TextUtils.isEmpty(packageStr)){
                    itemModel=gson.fromJson(packageStr,VersionItemModel.class);
                }
            }
        }
        return itemModel;
    }


    public static Map<String, String> getClientHeader() {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Client-Type", "MihuiAndroid");
        return header;
    }
}
