package io.intelliflow.repomanager.model;

public class TemplateInformation {
    private String sourceworkspaceName;
    private String destworkspaceName;
    private String sourceminiApp;
    private String destminiApp;

    private String deviceSupport;

    private String colorScheme;
    private String description;

    private CloneInfo filedesc;

    private String userId;

    private String templateName;

    private String logoURL;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CloneInfo getFiledesc() {
        return filedesc;
    }

    public String getDeviceSupport() {
        return deviceSupport;
    }

    public void setDeviceSupport(String deviceSupport) {
        this.deviceSupport = deviceSupport;
    }

    public void setFiledesc(CloneInfo filedesc) {
        this.filedesc = filedesc;
    }

    public String getSourceworkspaceName() {
        return sourceworkspaceName;
    }

    public void setSourceworkspaceName(String sourceworkspaceName) {
        this.sourceworkspaceName = sourceworkspaceName;
    }

    public String getDestworkspaceName() {
        return destworkspaceName;
    }

    public void setDestworkspaceName(String destworkspaceName) {
        this.destworkspaceName = destworkspaceName;
    }

    public String getSourceminiApp() {
        return sourceminiApp;
    }

    public void setSourceminiApp(String sourceminiApp) {
        this.sourceminiApp = sourceminiApp;
    }

    public String getDestminiApp() {
        return destminiApp;
    }

    public void setDestminiApp(String destminiApp) {
        this.destminiApp = destminiApp;
    }

    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }
}
