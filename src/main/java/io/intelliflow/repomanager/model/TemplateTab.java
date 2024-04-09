package io.intelliflow.repomanager.model;

import io.quarkus.mongodb.panache.common.MongoEntity;

import java.util.List;
@MongoEntity(collection = "templatetab")
public class TemplateTab {

    public TemplateTab(String description, List<String> screenShotsUrls) {
        this.description = description;
        this.screenShotsUrls = screenShotsUrls;
    }

    public TemplateTab() {
    }

    private String description;

    private List<String> screenShotsUrls;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getScreenShotsUrls() {
        return screenShotsUrls;
    }

    public void setScreenShotsUrls(List<String> screenShotsUrls) {
        this.screenShotsUrls = screenShotsUrls;
    }
}
