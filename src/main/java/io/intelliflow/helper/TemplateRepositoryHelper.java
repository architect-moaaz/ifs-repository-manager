package io.intelliflow.helper;

import io.intelliflow.repomanager.model.AppTemplate;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.TemplateTab;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TemplateRepositoryHelper {

    private boolean flagForCountFive = false;

    @Inject
    private AppTemplateRepository appTemplateRepository;
    public EventResponseModel addTemplate(AppTemplate appTemplate) {
        flagForCountFive = false;
        EventResponseModel response = new EventResponseModel();
        try
        {
            if(appTemplateRepository.findByAppTemplateName(appTemplate.getTemplateName())!=null){
                response.setData("");
                response.setMessage("template already present.");
                return response;
            }
            boolean flagForCountFive = false;

            if (appTemplate.getPages().stream().anyMatch(templateTabList -> templateTabList.getScreenShotsUrls().size() > 5)
                    || appTemplate.getWorkFlow().stream().anyMatch(templateTabList -> templateTabList.getScreenShotsUrls().size() > 5)
                    || appTemplate.getDataModels().stream().anyMatch(templateTabList -> templateTabList.getScreenShotsUrls().size() > 5)
                    || appTemplate.getUserProfiles().stream().anyMatch(templateTabList -> templateTabList.getScreenShotsUrls().size() > 5)) {
                flagForCountFive = true;
            }

            appTemplate = filterAppTemplate(appTemplate);
//            appTemplate.setId(new ObjectId());
            appTemplateRepository.persist(appTemplate);
            response.setData(appTemplate);

            if(flagForCountFive) {
                response.setMessage("Template Added Successfully but only first 5 screenshots added.");
            }else{
                response.setMessage("Template Added Successfully.");
            }
        }catch (Exception exception){
            response.setData("");
            response.setMessage("Something went wrong due to : "+exception.getMessage());
        }
        return response;
    }

    private AppTemplate filterAppTemplate(AppTemplate appTemplate) {
        List<TemplateTab> filteredPages = filterTemplateTabs(appTemplate.getPages());
        List<TemplateTab> filteredWorkFlow = filterTemplateTabs(appTemplate.getWorkFlow());
        List<TemplateTab> filteredDataModels = filterTemplateTabs(appTemplate.getDataModels());
        List<TemplateTab> filteredUserProfiles = filterTemplateTabs(appTemplate.getUserProfiles());

        return new AppTemplate(appTemplate.getTemplateName(), appTemplate.getDescription(), appTemplate.getLogoUrl(),
                filteredPages, filteredWorkFlow, filteredDataModels, filteredUserProfiles);
    }

    private List<TemplateTab> filterTemplateTabs(List<TemplateTab> templateTabs) {
        return templateTabs.stream()
                .map(templateTab -> {
                    List<String> screenShotsUrls = templateTab.getScreenShotsUrls();
                    if (screenShotsUrls.size() > 5) {
                        return new TemplateTab(templateTab.getDescription(), screenShotsUrls.subList(0, 5));
                    } else {
                        return templateTab;
                    }
                })
                .collect(Collectors.toList());
    }

    public Uni<EventResponseModel> getTemplate(String templateName) {
        JSONObject jsonObject = new JSONObject();
        EventResponseModel eventResponseModel = new EventResponseModel();
        try {
            AppTemplate appTemplate = appTemplateRepository.findByAppTemplateName(templateName);
            if(appTemplate==null){
                eventResponseModel.setData(jsonObject);
                eventResponseModel.setMessage("No Template found with name : " + templateName);
            }else{
                //help(jsonObject, appTemplate);
                eventResponseModel.setData(appTemplate);
                eventResponseModel.setMessage("found Template " + templateName);
            }

        } catch (Exception exception) {
            eventResponseModel.setData("");
            eventResponseModel.setMessage("Something went wrong due to: " + exception.getMessage());
        }
        return Uni.createFrom().item(() -> eventResponseModel);
    }

    private void help(JSONObject jsonObject, AppTemplate byAppTemplateName) {
        JSONObject template = new JSONObject();

        template.put("logo", byAppTemplateName.getLogoUrl());
        template.put("pages", convertTemplateTabsToJsonArray(byAppTemplateName.getPages()));
        template.put("workFlow", convertTemplateTabsToJsonArray(byAppTemplateName.getWorkFlow()));
        template.put("dataModels", convertTemplateTabsToJsonArray(byAppTemplateName.getDataModels()));
        template.put("userProfiles", convertTemplateTabsToJsonArray(byAppTemplateName.getUserProfiles()));
        template.put("description",byAppTemplateName.getDescription());

        jsonObject.put(byAppTemplateName.getTemplateName(), template);
    }

    private List<JSONObject> convertTemplateTabsToJsonArray(List<TemplateTab> templateTabs) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (TemplateTab templateTab : templateTabs) {
            JSONObject jsonTemplateTab = new JSONObject();
            jsonTemplateTab.put("description", templateTab.getDescription());
            jsonTemplateTab.put("screenShotsUrls", List.copyOf(templateTab.getScreenShotsUrls()));
            jsonObjects.add(jsonTemplateTab);
        }
        return jsonObjects;
    }

    public EventResponseModel deleteTemplate(String templateName) {
        EventResponseModel eventResponseModel = new EventResponseModel();
        try {
            AppTemplate byAppTemplateName = appTemplateRepository.findByAppTemplateName(templateName);
            if(byAppTemplateName==null){
                eventResponseModel.setMessage("No Template Found with Name = "+templateName);
                return eventResponseModel;
            }
            if(appTemplateRepository.deleteByAppTemplateName(templateName)>0)
            eventResponseModel.setMessage("Template "+templateName+" deleted successfully.");
            else
                eventResponseModel.setMessage("Template "+templateName+" deletion failed. try again");
        }catch (Exception exception){
            eventResponseModel.setData("");
            eventResponseModel.setMessage("Something went wrong due to : "+exception.getMessage());
        }
        return  eventResponseModel;
    }

    public EventResponseModel updateTemplate(String templateName, AppTemplate updatedTemplate) {
        EventResponseModel eventResponseModel = new EventResponseModel();
        try {
            AppTemplate byAppTemplateName = appTemplateRepository.findByAppTemplateName(templateName);
            if(byAppTemplateName==null){
                eventResponseModel.setMessage("No Template Found with Name = "+templateName);
                return eventResponseModel;
            }
            if(updatedTemplate.getTemplateName()!=null && !updatedTemplate.getTemplateName().isEmpty()){
                if(appTemplateRepository.findByAppTemplateName(updatedTemplate.getTemplateName())!=null){
                    eventResponseModel.setMessage("AppTemplate with Name = "+updatedTemplate.getTemplateName() + " is Already present. Please use some other Name");
                    return eventResponseModel;
                }
            }
            updatedTemplate.setId(byAppTemplateName.getId());
            appTemplateRepository.update(updatedTemplate);
           eventResponseModel.setMessage("AppTemplate Updated Successfully.");
        }catch (Exception exception){
            eventResponseModel.setData("");
            eventResponseModel.setMessage("Something went wrong due to : "+exception.getMessage());
        }
        return  eventResponseModel;
    }
}
