package lang.example.webtest.util.packcache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import lang.example.webtest.model.VersionModel;

/**
 * Created by lang on 16-12-14.
 */
public class JsonParseUtil {
    public static Gson gson=new Gson();
    /**
     * 解析服务器返回的message字段信息
     *
     * @throws Exception
     */
    public static String getStringValue(String jsonStr,String key)  {
        String value = null;
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            value = jsonObj.getString(key);
        } catch (JSONException e) {
            //e.printStackTrace();
            value = null;
        }catch (Exception e) {
            e.printStackTrace();
            value = null;
        }
        return value;
    }

    /**
     * 返回订单详情数据集合
     */
    public static VersionModel getVersionModel(String jsonStr) {
        VersionModel versionModel = gson.fromJson(jsonStr,new TypeToken<VersionModel>(){}.getType());
        return versionModel;
    }
}
