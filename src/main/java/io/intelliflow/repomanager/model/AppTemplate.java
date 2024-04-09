package io.intelliflow.repomanager.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.UUID;

@MongoEntity(collection = "apptemplate")
public class AppTemplate {

  private ObjectId id;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    private String templateName;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AppTemplate() {
    }

    private String logoUrl;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    private List<TemplateTab> pages;

    public List<TemplateTab> getPages() {
        return pages;
    }

    public void setPages(List<TemplateTab> pages) {
        this.pages = pages;
    }

    public List<TemplateTab> getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(List<TemplateTab> workFlow) {
        this.workFlow = workFlow;
    }

    public List<TemplateTab> getDataModels() {
        return dataModels;
    }

    public void setDataModels(List<TemplateTab> dataModels) {
        this.dataModels = dataModels;
    }

    public List<TemplateTab> getUserProfiles() {
        return userProfiles;
    }

    public void setUserProfiles(List<TemplateTab> userProfiles) {
        this.userProfiles = userProfiles;
    }

    private List<TemplateTab> workFlow;

    public AppTemplate(String templateName, String description, String logoUrl, List<TemplateTab> pages, List<TemplateTab> workFlow, List<TemplateTab> dataModels, List<TemplateTab> userProfiles) {
        this.templateName = templateName;
        this.description = description;
        this.logoUrl = logoUrl;
        this.pages = pages;
        this.workFlow = workFlow;
        this.dataModels = dataModels;
        this.userProfiles = userProfiles;
    }

    private List<TemplateTab> dataModels;
    private List<TemplateTab> userProfiles;
}
