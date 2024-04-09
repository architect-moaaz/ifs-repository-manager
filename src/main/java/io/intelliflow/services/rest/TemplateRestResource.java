package io.intelliflow.services.rest;

import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.helper.TemplateRepositoryHelper;
import io.intelliflow.helper.db.TemplateService;
import io.intelliflow.repomanager.model.AppTemplate;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;

@Path("/template")
public class TemplateRestResource {

    @Inject
    TemplateService templateService;

    @Inject
    TemplateRepositoryHelper templateRepositoryHelper;

    @POST
    public Uni<EventResponseModel> addTemplate(AppTemplate appTemplate) throws CustomException {
        EventResponseModel responseModel = templateRepositoryHelper.addTemplate(appTemplate);
        return Uni.createFrom().item(() -> responseModel)
                .onFailure().invoke(t -> responseModel.setMessage(t.getMessage()));
    }

    @GET
    @Path("/{templateName}")
    public Uni<EventResponseModel> getTemplate(@PathParam("templateName") String templateName) {
        return templateRepositoryHelper.getTemplate(templateName);
    }

    @DELETE
    @Path("/{templateName}")
    public Uni<EventResponseModel> deleteTemplate(@PathParam("templateName") String templateName) {
        return Uni.createFrom().item(() -> templateRepositoryHelper.deleteTemplate(templateName));
    }

    @PUT
    @Path("/{templateName}")
    public Uni<EventResponseModel> updateTemplate(
            @PathParam("templateName") String templateName, AppTemplate updatedTemplate) {
        EventResponseModel responseModel = templateRepositoryHelper.updateTemplate(templateName, updatedTemplate);
        return Uni.createFrom().item(() -> responseModel)
                .onFailure().invoke(t -> responseModel.setMessage(t.getMessage()));
    }
}
