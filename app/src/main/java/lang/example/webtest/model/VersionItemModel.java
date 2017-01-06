package lang.example.webtest.model;

import java.util.List;
import java.util.Map;

/**
 * Created by lang on 16-12-14.
 */
public class VersionItemModel {
    private float version;
    private String packageName;
    private String dirName;
    private String mainUrl;
    private List<CheckFileModel> checkFiles;

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getMainUrl() {
        return mainUrl;
    }

    public void setMainUrl(String mainUrl) {
        this.mainUrl = mainUrl;
    }

    public List<CheckFileModel> getCheckFiles() {
        return checkFiles;
    }

    public void setCheckFiles(List<CheckFileModel> checkFiles) {
        this.checkFiles = checkFiles;
    }
}
