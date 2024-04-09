package io.intelliflow.helper;

import io.intelliflow.repomanager.model.TemplateTab;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemplateTabRepository implements PanacheMongoRepository<TemplateTab> {
}
