package io.intelliflow.services.rest;

import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.helper.PlatformHelper;
import io.intelliflow.helper.FileHelper;
import io.intelliflow.helper.FileLocker;
import io.intelliflow.helper.GITFileHelper;
import io.intelliflow.helper.RepositoryHelper;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.FileInformation;
import io.intelliflow.repomanager.model.GITResponseModel;
import io.intelliflow.repomanager.model.MessageEventModel;
import io.intelliflow.repomanager.model.validator.ValidationResponse;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ServiceUnavailableException;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Path("/workspace/repository/resource")
public class AssetRestResource {
	

	@Inject
	EventBus eventBus;

	@Inject
	FileHelper helper;

	@Inject
	GITFileHelper githelper;

	@Inject
	PlatformHelper plateformHelper;
	
	@Path("/file/saveMeta")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> createMetaFileInWorkspace(FileInformation fileInformation) {
		fileInformation.setOperation("CREATE-META");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message :: body);
	}

	@Path("/file/fetchMeta")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> fetchMetaContentFromRepository(FileInformation fileInformation) {
		fileInformation.setOperation("FETCH-META");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> createFileInRepository(FileInformation fileInformation) {
		fileInformation.setOperation("CREATE");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);
	}
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> fetchFileContentFromRepository(FileInformation fileInformation) {
		fileInformation.setOperation("FETCH");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}


	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> deleteFileInRepository(FileInformation fileInformation) {
		fileInformation.setOperation("DELETE");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}

	@PATCH
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> updateFileInRepository(FileInformation fileInformation) {
		fileInformation.setOperation("UPDATE");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}

	@Path("/asset/list")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> fetchFileListFromRepository(FileInformation fileInformation) {
		fileInformation.setOperation("RETRIEVE");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}

	@Path("/asset/content")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> fetchFileFromRepository(FileInformation fileInformation) {
		fileInformation.setOperation("FETCH");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}
	
	@Path("/asset/draft")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> draftSaveInRepository(FileInformation fileInformation) {
		fileInformation.setOperation("DRAFT");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}
	
	@Path("/asset/draft/FETCH")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> getDraftList(FileInformation fileInformation) {
		fileInformation.setOperation("DRAFT_FETCH");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}
	
	@Path("/asset/draft/LOAD")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> loadDraft(FileInformation fileInformation) {
		fileInformation.setOperation("DRAFT_LOAD");
		return eventBus.<EventResponseModel>request("repository", fileInformation).onItem().transform(Message::body);

	}

	@Path("/asset/lock")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> lockAsset(FileInformation fileInformation) {
		EventResponseModel responseModel = new EventResponseModel();
		responseModel.setData(FileLocker.lockFile(fileInformation));
		return Uni.createFrom().item(() -> responseModel);
	}

	@Path("/asset/release")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<EventResponseModel> releaseAsset(FileInformation fileInformation) {
		EventResponseModel responseModel = new EventResponseModel();
		responseModel.setMessage(FileLocker.removeLockFile(fileInformation));
		return Uni.createFrom().item(() -> responseModel);
	}

	@Path("/bpmn/validate")
	@POST
	public Uni<EventResponseModel> validateBpmn(FileInformation fileInformation){
		EventResponseModel responseModel = new EventResponseModel();
		ValidationResponse response = helper.validateBPMN(fileInformation);
		if(response.isErrors()) {
			responseModel.setMessage("BPMN Validation Failed");
			responseModel.setData(response);
		} else {
			responseModel.setMessage("BPMN Validation Success");
		}
		return Uni.createFrom().item(() -> responseModel);
	}
	@Path("/dmn/validate")
	@POST
	public Uni<EventResponseModel> validateDmn(FileInformation fileInformation){
		EventResponseModel responseModel = new EventResponseModel();
		ValidationResponse response = helper.validateDMN(fileInformation);
		if(response.isErrors()) {
			responseModel.setMessage("dmn Validation Failed");
			responseModel.setData(response);
		} else {
			responseModel.setMessage("dmn Validation Success");
		}
		return Uni.createFrom().item(() -> responseModel);
	}

	@POST
	@Path("/rename")
	public Uni<EventResponseModel> renameFile(FileInformation fileInformation) throws GitAPIException, CustomException, IOException {
		fileInformation.setOperation("RENAME-FILE");
		EventResponseModel response = new EventResponseModel();
		GITResponseModel model= githelper.renameFile(fileInformation);
		response.setData(model);
		response.setMessage("File renamed successfully");
		return Uni.createFrom().item(() -> response);
	}

	@POST
	@Path("/{miniAppName}")
	public String moveFiles(@PathParam("miniAppName") String miniAppName) throws IOException, CustomException {
		FileInformation fileInformation = new FileInformation();
		fileInformation.setWorkspaceName("Intelliflow");
		fileInformation.setMiniApp(miniAppName);
		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {
			try (Git git = new Git(repository)) {
				File modelFile = new File(git.getRepository().getDirectory().getParent() +
						"/src/main/resources/io/intelliflow/generated/models/");
				if(modelFile.listFiles() == null){
					return "There is no models package present.";
				}
				for (File file : Objects.requireNonNull(modelFile.listFiles())) {
					String resourcePath = git.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/";
					java.nio.file.Path modelPath = java.nio.file.Path.of(resourcePath);
					if (Files.notExists(modelPath)) {
						Files.createDirectories(modelPath);
					}
					fileInformation.setContent(Files.readString(file.toPath()));
					File readmeFile = new File(resourcePath + file.getName());
					readmeFile.createNewFile();
					if (fileInformation.getContent() != null) {
						FileWriter fileWriter = new FileWriter(readmeFile);
						fileWriter.write(fileInformation.getContent());
						fileWriter.close();
					}
					git.add()
							.addFilepattern(
									"src/main/java/io/intelliflow/generated/models/" + file.getName())
							.call();
					if (file.delete()) {
						git.rm().addFilepattern(
								"src/main/resources/io/intelliflow/generated/models/" + file.getName()).call();
					}
					git.commit().setMessage("File moved from resource to java folder").call();
				}
				File deleteFolder = new File(git.getRepository().getDirectory().getParent() +
						"/src/main/resources/io/");
				FileUtils.deleteDirectory(deleteFolder);
				deleteFolder.delete();
			} catch (NoHeadException e) {
				throw new RuntimeException(e);
			} catch (UnmergedPathsException e) {
				throw new RuntimeException(e);
			} catch (NoFilepatternException e) {
				throw new RuntimeException(e);
			} catch (WrongRepositoryStateException e) {
				throw new RuntimeException(e);
			} catch (ServiceUnavailableException e) {
				throw new RuntimeException(e);
			} catch (ConcurrentRefUpdateException e) {
				throw new RuntimeException(e);
			} catch (AbortedByHookException e) {
				throw new RuntimeException(e);
			} catch (NoMessageException e) {
				throw new RuntimeException(e);
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			}
		}
		return "Files moved to java folder from Resource folder.";
	}

	@POST
	@Path("/messageEvent")
	public Uni<EventResponseModel> updatePropertiesForMessage(MessageEventModel model) {
		EventResponseModel response = githelper.updateMessageProperties(model);
		return Uni.createFrom().item(() -> response);
	}

	@GET
	@Path("/buildHistory")
	public Uni<EventResponseModel> getDeploymentHistory(@HeaderParam("workspace") String workspace,@HeaderParam("appName") String app, @HeaderParam("Timezone") String timeZone,@QueryParam("page") int pageNumber,@QueryParam("size") int pageSize){
      EventResponseModel eventResponseModel = plateformHelper.getBuildHistory(workspace,app,timeZone,pageSize,pageNumber);
	  return Uni.createFrom().item(() -> eventResponseModel);
	}

}
