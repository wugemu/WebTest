package lang.example.webtest.model;

import java.util.List;

/**
 * Created by lang on 16-12-14.
 */
public class VersionModel {
    private int openCache;
    private String downUrl;
    private VersionItemModel BaseLink;
    private List<VersionItemModel> ActivityView;

    public int getOpenCache() {
        return openCache;
    }

    public void setOpenCache(int openCache) {
        this.openCache = openCache;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public VersionItemModel getBaseLink() {
        return BaseLink;
    }

    public void setBaseLink(VersionItemModel baseLink) {
        BaseLink = baseLink;
    }

    public List<VersionItemModel> getActivityView() {
        return ActivityView;
    }

    public void setActivityView(List<VersionItemModel> activityView) {
        ActivityView = activityView;
    }
}
