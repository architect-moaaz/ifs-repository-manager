package io.intelliflow.helper;

import io.intelliflow.repomanager.model.AppTemplate;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppTemplateRepository implements PanacheMongoRepository<AppTemplate> {
    public AppTemplate findByAppTemplateName(String templateName) {
        return find("templateName",templateName).firstResult();
    }

    public long deleteByAppTemplateName(String templateName) {
        return delete("templateName",templateName);
    }
}
