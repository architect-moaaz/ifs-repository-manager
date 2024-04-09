package io.intelliflow.handler;

import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.helper.GITFileHelper;
import io.intelliflow.helper.db.AssetDataService;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.FileInformation;
import io.intelliflow.repomanager.model.GITResponseModel;
import io.quarkus.logging.Log;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@ApplicationScoped
public class GITHandler {

	@Inject
	AssetDataService dataService;

	@Inject
	GITFileHelper gitFileHelper;

	Set<String> processSet = new HashSet<>();

	Function<String, Boolean> lock = e -> { Log.info("Lock function called"); return processSet.add(e);};
	Function<String, Boolean> release = e -> { Log.info("Release function called"); return processSet.remove(e);};
	Function<String, Boolean> isLocked = e -> processSet.contains(e);

	public EventResponseModel performFileOperation(FileInformation fileInformation) {
		
		System.out.println(isLocked.apply(fileInformation.getMiniApp()));
		
		if(!isLocked.apply(fileInformation.getMiniApp())) {

			EventResponseModel eventResponseModel = new EventResponseModel();

			String fileOperation = fileInformation.getOperation();

			GITResponseModel gitResponseModel;

			try {
				switch (fileOperation) {

					case "CREATE":

						gitResponseModel = gitFileHelper.createFileInRepository(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						dataService.updateTimeForData(fileInformation);
						break;

					case "RETRIEVE":

						gitResponseModel = gitFileHelper.findResourceFiles(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						System.out.println("REtRIEVE process completed");
						Log.info("Logged Data::: REtRIEVE process completed");
						break;

					case "UPDATE":
					case "SAVE":

						gitResponseModel = gitFileHelper.updateFileInRepository(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						dataService.updateTimeForData(fileInformation);
						break;

					case "DELETE":

						gitResponseModel = gitFileHelper.deleteFileInRepository(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						dataService.updateTimeForData(fileInformation);
						break;

					case "FETCH":

					case "FETCH-META":
						gitResponseModel = gitFileHelper.fetchFileContentFromRepo(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						break;

					case "CREATE-META":
						gitResponseModel = gitFileHelper.createMetaFileInWorkspace(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						break;

					case "DRAFT":
						gitResponseModel = gitFileHelper.saveFileAsDraft(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						dataService.updateTimeForData(fileInformation);
						break;

					case "DRAFT_FETCH":
						gitResponseModel = gitFileHelper.getSavedDrafts(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						break;

					case "DRAFT_LOAD":
						gitResponseModel = gitFileHelper.loadDraft(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						break;

					case "DRAFT_DROP":
						gitFileHelper.deleteDraft(fileInformation);
						break;

					case "RENAME-FILE":
						gitResponseModel = gitFileHelper.renameFile(fileInformation);
						eventResponseModel.setData(gitResponseModel.getData());
						eventResponseModel.setMessage(gitResponseModel.getMessage());
						break;

					case "REVISIONS_FETCH":
						break;

					case "RESTORE":
						break;

					case "MERGE2MASTER":
						lock.apply(fileInformation.getMiniApp());
						gitFileHelper.mergeToMaster(fileInformation);
						
						break;

					default:
						break;

				}
			} catch (IOException | GitAPIException e) {

				e.printStackTrace();
				return eventResponseModel;
			} catch (CustomException e) {
				throw new RuntimeException(e);
			} finally {
				release.apply(fileInformation.getMiniApp());
			}
			Log.info("Ready to send data");
			return eventResponseModel;
			
		}else {
			
			EventResponseModel eventResponseModel = new EventResponseModel();
			eventResponseModel.setMessage(fileInformation.getMiniApp() +" is being published right now. Please try after sometime");
			
			return eventResponseModel;
		}


	}

}
