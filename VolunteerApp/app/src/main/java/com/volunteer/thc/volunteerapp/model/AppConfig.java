package com.volunteer.thc.volunteerapp.model;

/**
 * Created by Vlad on 25.03.2018.
 */
public class AppConfig {

    private String id;
    private String appVersion;
    private String errorMsg;
    private String successMsg;
    private boolean backwardsCompatible;
    private long releaseDate;

    public AppConfig() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getSuccessMsg() {
        return successMsg;
    }

    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

    public boolean isBackwardsCompatible() {
        return backwardsCompatible;
    }

    public void setBackwardsCompatible(boolean backwardsCompatible) {
        this.backwardsCompatible = backwardsCompatible;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }
}
