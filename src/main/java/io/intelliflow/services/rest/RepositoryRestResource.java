package io.intelliflow.services.rest;

import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.centralCustomExceptionHandler.Status;
import io.intelliflow.helper.GITFileHelper;
import io.intelliflow.helper.RepositoryHelper;
import io.intelliflow.helper.db.AssetDataService;
import io.intelliflow.repomanager.model.AppupdateInformation;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.FileInformation;
import io.intelliflow.repomanager.model.TemplateInformation;
import io.intelliflow.services.repo.ParentRepositorySingleton;
import io.smallrye.mutiny.Uni;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/workspace/repository")
public class RepositoryRestResource {

	@Inject
	AssetDataService assetDataService;

	@Inject
	ParentRepositorySingleton parentRepositorySingleton;

	@GET
	public Uni<EventResponseModel> listMiniApps(FileInformation fileInformation, @QueryParam("content") String type) {
		EventResponseModel responseModel = new EventResponseModel();
		responseModel.setMessage("List of MiniApps");
		responseModel.setData(RepositoryHelper.listMiniApps(fileInformation.getWorkspaceName()));
		return Uni.createFrom().item(() -> responseModel);
	}

	@POST
	public Uni<EventResponseModel> createRepository(FileInformation fileInformation) throws CustomException {
		if (fileInformation.getWorkspaceName() == null || fileInformation.getWorkspaceName().isEmpty()) {
			throw new CustomException("Workspace name cannot be null or empty", Status.BAD_REQUEST);
		}
		if (fileInformation.getMiniApp() == null || fileInformation.getMiniApp().isEmpty()) {
			throw new CustomException("Mini app name cannot be null or empty", Status.BAD_REQUEST);
		}

		if (fileInformation.getDescription() == null || fileInformation.getDescription().isEmpty()) {
			throw new CustomException("App Description cannot be null or empty", Status.BAD_REQUEST);
		}
		EventResponseModel responseModel;
		try {

			responseModel = RepositoryHelper.createMiniApp(parentRepositorySingleton, fileInformation);

		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
			throw new CustomException("App Creation Failed", Status.INTERNAL_SERVER_ERROR);
		}
		assetDataService.updateTimeForData(fileInformation);
		return Uni.createFrom().item(() -> responseModel);
	}

	@DELETE
	public Uni<EventResponseModel> deleteRepository(FileInformation fileInformation) throws CustomException {
		if (fileInformation.getWorkspaceName() == null || fileInformation.getWorkspaceName().isEmpty()) {
			throw new CustomException("Workspace name cannot be null or empty", Status.BAD_REQUEST);
		}
		if (fileInformation.getMiniApp() == null || fileInformation.getMiniApp().isEmpty()) {
			throw new CustomException("Mini app name cannot be null or empty", Status.BAD_REQUEST);
		}
		EventResponseModel responseModel = new EventResponseModel();
		try {
			RepositoryHelper.deleteMiniApp(parentRepositorySingleton, fileInformation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		responseModel.setData(RepositoryHelper.listMiniApps(fileInformation.getWorkspaceName()));
		//assetDataService.updateTimeForData(fileInformation);
		return Uni.createFrom().item(() -> responseModel);
	}

	@GET
	@Path("/{workspacename}/{appname}/data")
	public Uni<EventResponseModel> getFileDataForApp(
			@PathParam("workspacename") String workspaceName,
			@PathParam("appname") String appName,
			@QueryParam("status") String status
	) {
		EventResponseModel responseModel = new EventResponseModel();
		responseModel.setMessage("Info for Files in " + appName + " in workspace " + workspaceName);
		responseModel.setData(assetDataService.fileDataInApp(workspaceName, appName, status));
		return Uni.createFrom().item(() -> responseModel);
	}

	@POST
	@Path("/cloneApplication")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> cloneApplicationn(TemplateInformation templateInformation) throws IOException, CustomException, GitAPIException {
		EventResponseModel responseModel = new EventResponseModel();

		responseModel.setMessage(templateInformation.getSourceminiApp() + " cloned into " + templateInformation.getDestminiApp());
		responseModel.setData(RepositoryHelper.cloneApplication(parentRepositorySingleton, templateInformation));
		return Uni.createFrom().item(() -> responseModel);
	}

	@POST
	@Path("/cloneFile")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> cloneFile(TemplateInformation templateInformation) throws IOException, CustomException, GitAPIException {
		EventResponseModel responseModel = new EventResponseModel();
		FileInformation fileInformation = new FileInformation();
		fileInformation.setUserId(templateInformation.getUserId());
		responseModel.setMessage(templateInformation.getSourceminiApp() + " cloned into " + templateInformation.getDestminiApp());
		responseModel.setData(GITFileHelper.cloneFile(parentRepositorySingleton, templateInformation,fileInformation));
		return Uni.createFrom().item(() -> responseModel);
	}

	@POST
	@Path("/updateApp")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> updateApplication(AppupdateInformation appupdateInformation) throws CustomException {
		EventResponseModel responseModel = new EventResponseModel();
		try {
			responseModel.setData(RepositoryHelper.updateApplication(appupdateInformation,parentRepositorySingleton));
			responseModel.setMessage("app updation was successful");

		} catch (GitAPIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Uni.createFrom().item(() -> responseModel);
	}

}