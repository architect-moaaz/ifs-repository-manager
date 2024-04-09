package io.intelliflow.services.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.centralCustomExceptionHandler.Status;
import io.intelliflow.helper.RepositoryHelper;
import io.intelliflow.helper.db.AssetDataService;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.FileInformation;
import io.intelliflow.services.repo.ParentRepositorySingleton;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.Arrays;

@Path("/workspace")
public class WorkspaceRestResource {

    @Inject
    AssetDataService assetDataService;

    @Inject
    ParentRepositorySingleton parentRepositorySingleton;

    @GET
    public Uni<EventResponseModel> fetchWorkspaces() {
        EventResponseModel responseModel = new EventResponseModel();

        String[] workspaces = RepositoryHelper.listWorkspaces();

        responseModel.setMessage(String.format("Found %d workspaces", workspaces.length));
        responseModel.setData(Arrays.asList(workspaces));


        return Uni.createFrom().item(() -> responseModel);
    }

    @POST
    public Uni<EventResponseModel> createWorkspace(FileInformation fileInformation) throws CustomException {

        if(fileInformation.getWorkspaceName() == null || fileInformation.getWorkspaceName().isEmpty()){
            throw new CustomException("Workspace name cannot be null be empty.",Status.BAD_REQUEST);
        }
        try {
            EventResponseModel responseModel = RepositoryHelper.createWorkspace(parentRepositorySingleton,fileInformation.getWorkspaceName(),fileInformation);
            return Uni.createFrom().item(() -> responseModel)
                    .onFailure().invoke(t -> responseModel.setMessage(t.getMessage()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO : Need to handle this and exception
        return null;
    }

    @DELETE
    public Uni<EventResponseModel> deleteWorkspace(FileInformation fileInformation) throws CustomException {
        if (fileInformation.getWorkspaceName() == null || fileInformation.getWorkspaceName().isEmpty()) {
            throw new CustomException("Workspace name cannot be null or empty", Status.BAD_REQUEST);
        }
        //TODO: Need to soft delete of the workspace
        EventResponseModel responseModel = RepositoryHelper.deleteWorkspace(parentRepositorySingleton, fileInformation.getWorkspaceName(),fileInformation);
        String[] workspaces = RepositoryHelper.listWorkspaces();
        responseModel.setData(Arrays.asList(workspaces));
        return Uni.createFrom().item(() -> responseModel);

    }

    @GET
    @Path("/{workspacename}/data")
    public Uni<EventResponseModel> getAppDataForWorkspace(
            @PathParam("workspacename") String workspaceName,
            @QueryParam("status") String status,@QueryParam("page") int pageNumber,@QueryParam("size") int pageSize,@QueryParam("sort") String sortCriteria,@QueryParam("filter") String filters) throws JsonProcessingException {
        if(pageNumber >= 1){
            pageNumber--;
        }
        EventResponseModel responseModel = new EventResponseModel();
        responseModel.setMessage("App information within " + workspaceName);
        responseModel.setData(assetDataService.appDataInWorkspace(workspaceName, status, pageNumber, pageSize, sortCriteria, filters));
        return Uni.createFrom().item(() -> responseModel);
    }

}
