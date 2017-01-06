package lang.example.webtest.util.packcache;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by lang on 16-12-13.
 */
public class ArticleWebView extends WebView {
    private OnLoadFinishListener mOnLoadFinishListener;

    public interface OnLoadFinishListener{
        public void onLoadFinish();
    }

    private boolean isRendered = false;

    public ArticleWebView(Context context) {
        super(context);
        init();
    }

    public ArticleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isRendered){
            Log.d("0.0", "getContentHeight():" + getContentHeight());
            isRendered = getContentHeight() > 0;
            if(mOnLoadFinishListener!= null){
                mOnLoadFinishListener.onLoadFinish();
            }
        }
    }

    public void setOnLoadFinishListener(OnLoadFinishListener onLoadFinishListener){
        this.mOnLoadFinishListener = onLoadFinishListener;
    }
}
