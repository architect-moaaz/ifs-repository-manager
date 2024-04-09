package io.intelliflow.helper;

import com.jayway.jsonpath.JsonPath;
import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.centralCustomExceptionHandler.Status;
import io.intelliflow.repomanager.model.*;
import io.intelliflow.repomanager.model.db.operational.DataByUser;
import io.intelliflow.repomanager.model.db.operational.AppsByName;
import io.intelliflow.repomanager.model.db.operational.FilesByApp;
import io.intelliflow.services.repo.ParentRepository;
import io.intelliflow.services.repo.ParentRepositorySingleton;
import io.intelliflow.services.utils.AppValidation;
import io.quarkus.logging.Log;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.json.JSONException;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@ApplicationScoped
public class GITFileHelper {

	@Inject
	ParentRepositorySingleton parentRepositorySingleton;

	private static String repositoryFolderPath = ConfigProvider.getConfig().getValue("repo.path", String.class);


	// Create File in Repository
	public GITResponseModel createFileInRepository(FileInformation fileInformation)
			throws IOException, GitAPIException, CustomException {

		GITResponseModel response = new GITResponseModel();
		// add file to the repository

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			try (Git git = new Git(repository)) {

				if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					String resourcePath = git.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/";
					Path modelPath = Path.of(resourcePath);
					if (Files.notExists(modelPath)) {
						Files.createDirectories(modelPath);
					}
					File readmeFile = new File(resourcePath + fileInformation.getFileName());
					readmeFile.createNewFile();
					if (fileInformation.getContent() != null) {
						FileWriter fileWriter = new FileWriter(readmeFile);
						fileWriter.write(fileInformation.getContent());
						fileWriter.close();
					}
					git.add()
							.addFilepattern(
									"src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName())
							.call();

					git.commit().setMessage(fileInformation.getComment()).call();
				} 
				else {
					if (fileInformation.getFileType().equalsIgnoreCase("form")) {
						File f = new File(git.getRepository().getDirectory().getParent() + "/src/main/resources/form");
						File[] totalForms = f.listFiles(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return name.endsWith(".frm");
							}
							
						});
						if (totalForms.length >= AppValidation.limitAllowed(fileInformation,"form")){
							response.setMessage("Maximum limit reached for Forms");
							return response;
						}
					}
						String resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/"
								+ fileInformation.getFileType() + "/" + fileInformation.getFileName();

						File readmeFile = new File(resourcePath);
						readmeFile.createNewFile();

						if (fileInformation.getContent() != null) {
							// Validating BPMN before save
							/*
							 * if(fileInformation.getFileType().equalsIgnoreCase("bpmn")) {
							 * ValidationResponse validateResponse = helper.validateBPMN(fileInformation);
							 * if(validateResponse.isErrors()) {
							 * response.setMessage("BPMN Validation Failed");
							 * response.setData(validateResponse);
							 * return response;
							 * }
							 * }
							 */
							FileWriter fileWriter = new FileWriter(readmeFile);
							fileWriter.write(fileInformation.getContent());
							fileWriter.close();
						}

						// run the add-call
						git.add()
								.addFilepattern(
										".")
								.call();

						git.commit().setMessage(fileInformation.getComment()).call();
				}

				ParentRepository<FilesByApp> filesByAppParentRepository = (ParentRepository<FilesByApp>) parentRepositorySingleton.getRepository("filesByApp");
                FilesByApp filesByApp = new FilesByApp(fileInformation.getWorkspaceName(), fileInformation.getMiniApp(), fileInformation.getFileName(),
						new Date(), new Date(), "Active", fileInformation.getUserId());
				filesByAppParentRepository.saveModel(filesByApp);

				ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
                AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename", filesByApp.getWorkspacename(),"appname", fileInformation.getMiniApp()));
				if(appsByName != null) {
					Set<String> files = appsByName.getFiles();
					files.add(filesByApp.getFilename());
					appsByName.setFiles(files);
					appsByNameParentRepository.updateModel(appsByName);
				}

				ParentRepository<DataByUser> dataByUserParentRepository = (ParentRepository<DataByUser>) parentRepositorySingleton.getRepository("dataByUser");
                DataByUser result = dataByUserParentRepository.modelWithFilters(Map.of("userid", fileInformation.getUserId()));

				if (result != null) {
					Set<String> filesDataByUser = result.getFiles();
					filesDataByUser.add(filesByApp.getFilename());
					result.setFiles(filesDataByUser);
					dataByUserParentRepository.updateModel(result);
				} else {
					DataByUser data = new DataByUser(fileInformation.getUserId(), null,Set.of(fileInformation.getFileName()),null);
					dataByUserParentRepository.saveModel(data);
				}

				System.out.println(
						"Added file " + fileInformation.getFileName() + " to repository at "
								+ repository.getDirectory().getName());

				response.setMessage(
						"Added file " + fileInformation.getFileName() + " to repository at "
								+ repository.getDirectory().getName());
				response.setStatus("SUCCESS");

			}
		}

		return response;

	}

	public GITResponseModel updateFileInRepository(FileInformation fileInformation)
			throws IOException, GitAPIException {

		GITResponseModel response = new GITResponseModel();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {
			try (Git git = new Git(repository)) {

				if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					String resourcePath = git.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName();
					File readmeFile = new File(resourcePath);
					if (fileInformation.getContent() != null) {
						FileWriter fileWriter = new FileWriter(readmeFile);
						fileWriter.write(fileInformation.getContent());
						fileWriter.close();
					}
					git.add()
							.addFilepattern(
									"src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName())
							.call();

					git.commit().setMessage(fileInformation.getComment()).call();
				} else {
					String resourcePath;
					if (fileInformation.getFileType().equalsIgnoreCase("workflow")) {
						resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/workflow/"
								+ (fileInformation.getWorkspaceName() + "-" + fileInformation.getMiniApp())
										.toLowerCase()
								+ ".wid";
					} else {
						resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/"
								+ fileInformation.getFileType() + "/" + fileInformation.getFileName();
					}

					File resourceFile = new File(resourcePath);

					if (fileInformation.getContent() != null) {
						FileWriter fileWriter = new FileWriter(resourceFile);
						fileWriter.write(fileInformation.getContent());
						fileWriter.close();
					}

					System.out.println(resourceFile.getName());

					if (fileInformation.getFileType().equalsIgnoreCase("workflow")) {
						git.add()
								.addFilepattern(
										"src/main/resources/workflow/" + (fileInformation.getWorkspaceName() + "-"
												+ fileInformation.getMiniApp()).toLowerCase() + ".wid")
								.call();
					} else {
						git.add()
								.addFilepattern(
										"src/main/resources/" + fileInformation.getFileType() + "/"
												+ fileInformation.getFileName())
								.call();
					}
					git.commit().setMessage(fileInformation.getComment()).call();
				}
				response.setMessage("File " + fileInformation.getFileName() + " has been updated successfully");
			}
		} catch (CustomException e) {
			throw new RuntimeException(e);
		}

		return response;

	}

	public GITResponseModel deleteFileInRepository(FileInformation fileInformation)
			throws IOException, GitAPIException, CustomException {
		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			GITResponseModel response = new GITResponseModel();
			try (Git git = new Git(repository)) {

				if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					File modelFile = new File(git.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName());
					if (modelFile.delete()) {
						git.rm().addFilepattern(
								"src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName())
								.call();
						git.commit().setMessage(fileInformation.getComment()).call();
						response.setMessage("Deleted file " + fileInformation.getFileName() + " ");
					}
				} else {
					String resourcePath;
					if (fileInformation.getFileType().equalsIgnoreCase("workflow")) {
						resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/workflow/"
								+ (fileInformation.getWorkspaceName() + "-" + fileInformation.getMiniApp())
										.toLowerCase()
								+ ".wid";
					} else {
						resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/"
								+ fileInformation.getFileType() + "/" + fileInformation.getFileName();
					}

					File resourceFile = new File(resourcePath);

					if (resourceFile.delete()) {
						System.out.println(resourcePath);

						if (fileInformation.getFileType().equalsIgnoreCase("workflow")) {
							git.rm().addFilepattern(
									"src/main/resources/workflow/"
											+ (fileInformation.getWorkspaceName() + "-" + fileInformation.getMiniApp())
													.toLowerCase()
											+ ".wid")
									.call();
						} else {
							git.rm().addFilepattern(
									"src/main/resources/" + fileInformation.getFileType() + "/"
											+ fileInformation.getFileName())
									.call();
						}

						git.commit().setMessage(fileInformation.getComment()).call();

						response.setMessage("Deleted file " + resourceFile.getName() + " ");
						response.setStatus("SUCCESS");
					}
					if (fileInformation.getFileType().equalsIgnoreCase("form") ||
							fileInformation.getFileType().equalsIgnoreCase("bpmn")) {
						RepositoryHelper.updateWidOnDeletion(fileInformation);
					}
				}

				ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
                AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename",fileInformation.getWorkspaceName(), "appname", fileInformation.getMiniApp()));
				if(appsByName != null) {
					Set<String> files = appsByName.getFiles();
					files.remove(fileInformation.getFileName());
					appsByName.setFiles(files);
					appsByNameParentRepository.updateModel(appsByName);
				}

				ParentRepository<DataByUser> dataByUserParentRepository = (ParentRepository<DataByUser>) parentRepositorySingleton.getRepository("dataByUser");
                DataByUser data = dataByUserParentRepository.modelWithFilters(Map.of("userid", fileInformation.getUserId()));
				if(data != null) {
					Set<String> filesDataByUser = data.getFiles();
					filesDataByUser.remove(fileInformation.getFileName());
					data.setFiles(filesDataByUser);
					dataByUserParentRepository.updateModel(data);
				}

				ParentRepository<FilesByApp> filesByAppParentRepository = (ParentRepository<FilesByApp>) parentRepositorySingleton.getRepository("filesByApp");
				FilesByApp filesByApp = filesByAppParentRepository.modelWithFilters(Map.of("workspacename",fileInformation.getWorkspaceName(), "appname" ,fileInformation.getMiniApp(), "filename", fileInformation.getFileName()));
				if(filesByApp != null) {
					filesByApp.setStatus("DELETED");
					filesByAppParentRepository.updateModel(filesByApp);
				}

				// get back the updated list - facing an issue
				GITResponseModel updatedRepo = findResourceFiles(fileInformation);
				response.setData(updatedRepo.getData());

			} catch (CustomException e) {
				throw new RuntimeException(e);
			}

			return response;
		}

	}

	public GITResponseModel findResourceFiles(FileInformation fileInformation) throws IOException, CustomException {

		String[] resourceTypes = { "bpmn", "dmn", "datamodel", "form", "page" };

		GITResponseModel response = new GITResponseModel();

		Map<String, List<ResourceModel>> resourceModelMap = new HashMap<String, List<ResourceModel>>();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {
			for (String resourceType : resourceTypes) {

				resourceModelMap.put(resourceType,
						this.listRepositoryContents(repository, resourceType, fileInformation));

			}

		}

		ResourceList resourceList = new ResourceList();
		resourceList.setResources(resourceModelMap);
		resourceModelMap.values().stream().forEach(e -> {
			if (!e.isEmpty())
				System.out.println("Model Value:::" + e.get(0).getResourceName());
		});

		System.out.println(resourceModelMap.toString());

		response.setStatus("SUCCESS");
		response.setData(resourceModelMap);

		return response;

	}

	private List<ResourceModel> listRepositoryContents(Repository repository, String resourceType,
			FileInformation fileInformation)
			throws IOException {

		Ref head = repository.getRefDatabase().findRef("HEAD");

		List<ResourceModel> resourceList = new ArrayList<ResourceModel>();

		System.out.println(head);

		// a RevWalk allows to walk over commits based on some filtering that is defined
		try (RevWalk walk = new RevWalk(repository)) {

			RevCommit commit = walk.parseCommit(head.getObjectId());
			RevTree tree = commit.getTree();

			System.out.println("Having tree: " + tree);

			// now use a TreeWalk to iterate over all files in the Tree recursively
			// you can set Filters to narrow down the results if needed
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				if (resourceType.equalsIgnoreCase("datamodel")) {
					treeWalk.setFilter(PathFilter.create("src/main/java/io/intelliflow/generated/models/"));
				} else {
					treeWalk.setFilter(PathFilter.create("src/main/resources/_rt/".replaceAll("_rt", resourceType)));
				}
				treeWalk.setRecursive(true);

				while (treeWalk.next()) {
					ResourceModel resourceModel = new ResourceModel();

					resourceModel.setResourceName(treeWalk.getNameString());
					resourceModel.setResourceType(resourceType);

					// Getting file lock status
					fileInformation.setFileName(treeWalk.getNameString());
					fileInformation.setFileType(resourceType);
					if (FileLocker.searchForLock(fileInformation) != null) {
						resourceModel.setLockStatus(true);
						resourceModel.setLockOwner(FileLocker.searchForLock(fileInformation));
					} else {
						resourceModel.setLockStatus(false);
					}

					resourceList.add(resourceModel);

					System.out.println("found: " + treeWalk.getNameString());

				}

				walk.dispose();

			}

		}

		return resourceList;
	}

	public GITResponseModel fetchFileContentFromDir(FileInformation fileInformation)
			throws IOException, CustomException {

		GITResponseModel response = new GITResponseModel();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {
			try (Git git = new Git(repository)) {
				String resourcePath = null;
				if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					resourcePath = git.getRepository().getDirectory().getParent() +
							"src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName();
				} else {
					resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/"
							+ fileInformation.getFileType() + "/" + fileInformation.getFileName();
				}

				response.setData(Files.readString(new File(resourcePath).toPath()));

			}
		}

		return response;
	}

	public GITResponseModel fetchFileContentFromRepo(FileInformation fileInformation)
			throws IOException, CustomException {
		Repository repository = RepositoryHelper.openRepository(fileInformation);

		Ref head = repository.getRefDatabase().findRef("HEAD");

		GITResponseModel response = new GITResponseModel();

		System.out.println(head);

		// a RevWalk allows to walk over commits based on some filtering that is defined
		try (RevWalk walk = new RevWalk(repository)) {

			RevCommit commit = walk.parseCommit(head.getObjectId());
			RevTree tree = commit.getTree();

			System.out.println("Having tree: " + tree);

			// creating paths for content or meta data
			String path;
			if (fileInformation.getOperation().equals("FETCH")) {
				if (fileInformation.getFileType().equalsIgnoreCase("workflow")) {
					path = "src/main/resources/workflow/"
							+ (fileInformation.getWorkspaceName() + "-" + fileInformation.getMiniApp()).toLowerCase()
							+ ".wid";
				} else if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					// This might cause it.
					path = "src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName();
				} else {
					path = "src/main/resources/" + fileInformation.getFileType() + "/" + fileInformation.getFileName();
				}
			} else {
				String[] name = fileInformation.getFileName().split(Pattern.quote("."));
				path = "src/main/resources/meta/" + fileInformation.getFileType() + "/" + name[0] + ".meta";
			}

			// now use a TreeWalk to iterate over all files in the Tree recursively
			// you can set Filters to narrow down the results if needed
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setFilter(PathFilter.create(path));
				treeWalk.setRecursive(true);

				if (!treeWalk.next()) {
					System.out.println("File Not Found");
					response.setMessage("File not found in repository");
					response.setStatus("FAILURE");
				} else {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					response.setMessage("File " + fileInformation.getFileName() + " found and data loaded");
					response.setData(new String(loader.getBytes(), StandardCharsets.UTF_8));
					response.setStatus("SUCCESS");
				}

				walk.dispose();

			}

		}
		return response;
	}

	public GITResponseModel saveFileAsDraft(FileInformation fileInformation)
			throws IOException, GitAPIException, CustomException {

		GITResponseModel response = new GITResponseModel();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			try (Git git = new Git(repository)) {
				String resourcePath;
				if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					resourcePath = git.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName();
				} else {
					resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/"
							+ fileInformation.getFileType() + "/" + fileInformation.getFileName();
				}

				File resourceFile = new File(resourcePath);

				try (FileWriter fileWriter = new FileWriter(resourceFile)) {

					fileWriter.write(fileInformation.getContent());

				}

				if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
					git.add()
							.addFilepattern(
									"/src/main/java/io/intelliflow/generated/models/" + fileInformation.getFileName())
							.call();
				} else {
					git.add()
							.addFilepattern(
									"src/main/resources/" + fileInformation.getFileType() + "/"
											+ fileInformation.getFileName())
							.call();
				}

				StashCreateCommand stashCreateCommand = Git.wrap(repository).stashCreate();

				stashCreateCommand.setWorkingDirectoryMessage(fileInformation.getComment());
				stashCreateCommand.setPerson(fileInformation.getPerson());

				RevCommit revCommit = stashCreateCommand.call();

				ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
				AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename", fileInformation.getWorkspaceName(),"appname", fileInformation.getMiniApp()));
				if(appsByName != null) {
					Set<String> filesAppByName = appsByName.getFiles();
					filesAppByName.remove(fileInformation.getFileName());
					appsByName.setFiles(filesAppByName);
					appsByNameParentRepository.updateModel(appsByName);
				}

				ParentRepository<DataByUser> dataByUserParentRepository = (ParentRepository<DataByUser>) parentRepositorySingleton.getRepository("dataByUser");
				DataByUser dataByUser = dataByUserParentRepository.modelWithFilters(Map.of("userid", fileInformation.getUserId()));
				if(dataByUser != null) {
					Set<String> filesDataUser = appsByName.getFiles();
					filesDataUser.remove(fileInformation.getFileName());
					dataByUser.setFiles(filesDataUser);
					dataByUserParentRepository.updateModel(dataByUser);
				}

				response.setMessage("draft index " + revCommit.getName() + " created successfully");
				response.setStatus("SUCCESS");

			}

		}

		return response;
	}

	public GITResponseModel getSavedDrafts(FileInformation fileInformation)
			throws IOException, GitAPIException {

		GITResponseModel response = new GITResponseModel();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {
			StashListCommand stashListCommand = Git.wrap(repository).stashList();

			Collection<RevCommit> stashList = stashListCommand.call();

			List<StashModel> drafts = new ArrayList<StashModel>();

			int refCount = 0;

			response.setMessage("Found " + stashList.size() + " drafts");
			for (RevCommit revCommit : stashList) {

				refCount++;

				StashModel stashModel = new StashModel();

				stashModel.setDraftName(revCommit.getName());
				stashModel.setCommiter(revCommit.getCommitterIdent().getName());
				stashModel.setComment(revCommit.getFullMessage());

				drafts.add(stashModel);

				Ref head = repository.exactRef("refs/heads/master");
				ObjectId headCommitId = head.getObjectId();
				ObjectId stashCommit = revCommit.getId();
				ObjectId stashHeadCommit = revCommit.getParent(0);

				ResolveMerger merger = (ResolveMerger) MergeStrategy.RESOLVE.newMerger(repository, true);
				merger.setWorkingTreeIterator(new FileTreeIterator(repository));
				merger.setBase(stashHeadCommit);

				// TODO: Need to use this to identify CONFLICTS and send appropriate result
				stashModel.setChanges(merger.getModifiedFiles());
				if (!merger.merge(headCommitId, stashCommit)) {
					// look into merger.getFailingPaths() and merger.getUnmergedPaths()

					System.out.println(merger.getModifiedFiles());

					System.out.println("====================================================");
				}

			}

			response.setData(drafts);

		} catch (CustomException e) {
			throw new RuntimeException(e);
		}

		return response;
	}

	public GITResponseModel loadDraft(FileInformation fileInformation) throws IOException,
			GitAPIException,
			CustomException {
		GITResponseModel response = new GITResponseModel();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			StashApplyCommand stashApplyCommand = Git.wrap(repository).stashApply();
			stashApplyCommand.setStashRef(fileInformation.getFileID());

			stashApplyCommand.call();

			response = this.fetchFileContentFromDir(fileInformation);

			StashDropCommand stashDropCommand = Git.wrap(repository).stashDrop();
			stashDropCommand.setStashRef(0); // TODO : need to be fetched from fileInformation

			stashDropCommand.call();
		}

		return response;

	}

	public void deleteDraft(FileInformation fileInformation)
			throws IOException, GitAPIException {

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			StashDropCommand stashDropCommand = Git.wrap(repository).stashDrop();
			stashDropCommand.setStashRef(0); // TODO : need to be fetched from fileInformation

			stashDropCommand.call();
		} catch (CustomException e) {
			throw new RuntimeException(e);
		}
	}

	public GITResponseModel createMetaFileInWorkspace(FileInformation fileInformation)
			throws IOException, GitAPIException, CustomException {

		GITResponseModel response = new GITResponseModel();
		// add file to the repository

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			try (Git git = new Git(repository)) {

				String resourcePath = git.getRepository().getDirectory().getParent() + "/src/main/resources/meta/"
						+ fileInformation.getFileType() + "/" + fileInformation.getFileName() + ".meta";

				File readmeFile = new File(resourcePath);
				readmeFile.createNewFile();

				if (fileInformation.getContent() != null) {
					FileWriter fileWriter = new FileWriter(readmeFile);
					fileWriter.write(fileInformation.getContent());
					fileWriter.close();
				}

				// run the add-call

				git.add().addFilepattern("src/main/resources/meta/" + fileInformation.getFileType() + "/"
						+ fileInformation.getFileName() + ".meta").call();

				git.commit().setMessage(fileInformation.getComment()).call();

				System.out.println(
						"Added Meta file " + readmeFile + " to repository at " + repository.getDirectory().getName());

				response.setMessage(
						"Added Meta file " + readmeFile + " to repository at " + repository.getDirectory().getName());
				response.setStatus("SUCCESS");

			}
		}

		return response;
	}

	public void mergeToMaster(FileInformation fileInformation)
			throws IOException, GitAPIException {
		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			try (Git git = new Git(repository)) {

				Ref checkout = git.checkout().setName("master").call();

				Log.info("Ref checkout : " + checkout);

				ObjectId mergeBase = repository.resolve("topic-master");

				MergeResult merge = git.merge().include(mergeBase).setCommit(true)
						.setFastForward(MergeCommand.FastForwardMode.NO_FF).setMessage("Auto commit").call();

				git.checkout().setName("topic-master").call();

			}
		} catch (CustomException e) {
			throw new RuntimeException(e);
		}
	}

	public GITResponseModel renameFile(FileInformation fileInformation)
			throws CustomException, GitAPIException, IOException {

		// TODO:As of now renaming datamodel as been disabled
		GITResponseModel response = new GITResponseModel();
		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {

			try (Git git = new Git(repository)) {

				File oldFile = new File(git.getRepository().getDirectory().getParent() + "/src/main/resources/"
						+ fileInformation.getFileType() + "/" + fileInformation.getFileName());

				int lastIndex = fileInformation.getFileName().lastIndexOf(".");
				String fileType = fileInformation.getFileName().substring(lastIndex);
				File updatedFile = new File(git.getRepository().getDirectory().getParent() + "/src/main/resources/"
						+ fileInformation.getFileType() + "/" + fileInformation.getUpdatedName() + fileType);

				if (oldFile.exists()) {

					if (!fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
						if (!updatedFile.exists()) {
							Path fileUpdate = Files.move(oldFile.toPath(), updatedFile.toPath());
							if (fileUpdate != null) {
								response.setMessage("File Renamed Successfully");
								if (fileInformation.getFileType().equalsIgnoreCase("datamodel")) {
									git.add()
											.addFilepattern(
													"src/main/java/io/intelliflow/generated/models/"
															+ StringUtils.capitalize(fileInformation.getUpdatedName()
																	.replaceAll("\\s", "")))
											.call();
									git.rm()
											.addFilepattern(
													"src/main/java/io/intelliflow/generated/models/"
															+ fileInformation.getFileName())
											.call();
								} else {
									git.add()
											.addFilepattern(
													"src/main/resources/" + fileInformation.getFileType() + "/"
															+ fileInformation.getUpdatedName() + fileType)
											.call();
									git.rm()
											.addFilepattern(
													"src/main/resources/" + fileInformation.getFileType() + "/"
															+ fileInformation.getFileName())
											.call();
								}
								git.commit().setMessage(
										fileInformation.getFileName() + " has been updated to "
												+ fileInformation.getUpdatedName() + fileType)
										.call();

								// Removing old file info from DB
								ParentRepository<FilesByApp> filesByAppParentRepository = (ParentRepository<FilesByApp>) parentRepositorySingleton.getRepository("filesByApp");
								filesByAppParentRepository.deleteModelWithParams(Map.of("workspacename", fileInformation.getWorkspaceName(),"appname", fileInformation.getMiniApp(),"filename", fileInformation.getFileName()));

								ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
								AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename",fileInformation.getWorkspaceName(),  "appname", fileInformation.getMiniApp()));
                                if(appsByName != null){
									Set<String> filesAppByName = appsByName.getFiles();
									filesAppByName.remove(fileInformation.getFileName());
									appsByName.setFiles(filesAppByName);
									appsByNameParentRepository.updateModel(appsByName);
								}

								ParentRepository<DataByUser> dataByUserParentRepository = (ParentRepository<DataByUser>) parentRepositorySingleton.getRepository("dataByUser");
								DataByUser dataByUser = dataByUserParentRepository.modelWithFilters(Map.of("userid",fileInformation.getUserId() ));
								if(dataByUser != null){
									Set<String> filesDataByUser = dataByUser.getFiles();
									filesDataByUser.remove(fileInformation.getFileName());
									dataByUser.setFiles(filesDataByUser);
									dataByUserParentRepository.updateModel(dataByUser);
								}

								FilesByApp filesByApp = new FilesByApp(fileInformation.getWorkspaceName(), fileInformation.getMiniApp(),
										fileInformation.getUpdatedName() + fileType, new Date(), new Date(), "Active", fileInformation.getUserId());
								filesByAppParentRepository.saveModel(filesByApp);


								AppsByName appsByNameFileUpdate = appsByNameParentRepository.modelWithFilters(Map.of("workspacename", fileInformation.getWorkspaceName(),"appname", fileInformation.getMiniApp()));
                                if(appsByNameFileUpdate !=  null){
									Set<String> filesAppByName = appsByNameFileUpdate.getFiles();
									filesAppByName.add(fileInformation.getUpdatedName() + fileType);
									appsByNameFileUpdate.setFiles(filesAppByName);
									appsByNameParentRepository.updateModel(appsByNameFileUpdate);
								}

								DataByUser dataByUserFileAdd = dataByUserParentRepository.modelWithFilters(Map.of("userid",fileInformation.getUserId()));
								if(dataByUserFileAdd != null){
									Set<String> filesDataByUser = dataByUserFileAdd.getFiles();
									filesDataByUser.add(fileInformation.getUpdatedName() + fileType);
									dataByUserFileAdd.setFiles(filesDataByUser);
									dataByUserParentRepository.updateModel(dataByUser);
								}

								updateWidOnRename(fileInformation);
							} else {
								response.setMessage("File renaming Failed!!!");
							}

							if (fileInformation.getFileType().equalsIgnoreCase("bpmn")) {
								String bpmnName = fileInformation.getUpdatedName() + fileType;
								int fileIndex = fileInformation.getFileName().indexOf(".");
								String updateoldFile = fileInformation.getFileName().substring(0, fileIndex);
								int updateFileIndex = bpmnName.indexOf(".");
								String updatenewFile = bpmnName.substring(0, updateFileIndex);
								File oldmetaFile = new File(git.getRepository().getDirectory().getParent()
										+ "/src/main/resources/meta/bpmn/" + updateoldFile + ".meta");
								File newmetaFile = new File(git.getRepository().getDirectory().getParent()
										+ "/src/main/resources/meta/bpmn/" + updatenewFile + ".meta");
								Path metaUpdate = Files.move(oldmetaFile.toPath(), newmetaFile.toPath());
								if (metaUpdate != null) {
									git.add()
											.addFilepattern(
													"/src/main/resources/meta/bpmn/" + updatenewFile + ".meta")
											.call();
									git.rm()
											.addFilepattern(
													"/src/main/resources/meta/bpmn" + updateoldFile + ".meta")
											.call();
								}
								git.commit().setMessage(
										fileInformation.getFileName() + " has been updated to "
												+ fileInformation.getUpdatedName())
										.call();
							}
						} else {
							throw new CustomException("File with same name already exists", Status.CONFLICT);
						}
					} else {
						response.setMessage(fileInformation.getFileType() + " cannot be renamed");
					}

				} else {
					throw new CustomException("File doest not exists", Status.CONFLICT);
				}
			}
		}

		return response;
	}

	public static EventResponseModel updateWidOnRename(FileInformation fileInformation)
			throws IOException, CustomException, GitAPIException {
		FileInformation widFileInfo = new FileInformation();
		widFileInfo.setWorkspaceName(fileInformation.getWorkspaceName());
		widFileInfo.setMiniApp(fileInformation.getMiniApp());
		widFileInfo.setFileType("workflow");
		EventResponseModel response = new EventResponseModel();

		try (Repository repository = RepositoryHelper.openRepository(fileInformation)) {
			try (Git git = new Git(repository)) {
				int lastIndex = fileInformation.getFileName().lastIndexOf(".");
				String fileType = fileInformation.getFileName().substring(lastIndex);
				File workFlowFile = new File(git.getRepository().getDirectory().getParent()
						+ "/src/main/resources/workflow/"
						+ (fileInformation.getWorkspaceName() + "-" + fileInformation.getMiniApp()).toLowerCase()
						+ ".wid");
				String getMappingExpression = "$.configuration.mapping";

				try {
					net.minidev.json.JSONArray exisitingMapArray = JsonPath
							.parse(Files.readString(workFlowFile.toPath())).read(getMappingExpression);
					if (fileInformation.getFileType().equalsIgnoreCase("bpmn")) {
						for (int i = 0; i < exisitingMapArray.size(); i++) {
							if (exisitingMapArray.get(i).getClass().equals(LinkedHashMap.class)) {
								if (((LinkedHashMap) exisitingMapArray.get(i)).get("bpmnname")
										.equals(fileInformation.getFileName())) {
									try {
										org.json.JSONArray mappingArray = new org.json.JSONArray();
										LinkedHashMap widObject = (LinkedHashMap) exisitingMapArray.get(i);
										widObject.put("bpmnname", fileInformation.getUpdatedName() + fileType);
										mappingArray.put(widObject);
									} catch (JSONException e) {
										throw new RuntimeException(e);
									}
								}

							}
						}
					}
					if (fileInformation.getFileType().equalsIgnoreCase("form")) {
						for (int i = 0; i < exisitingMapArray.size(); i++) {
							if (exisitingMapArray.get(i).getClass().equals(LinkedHashMap.class)) {
								if (((LinkedHashMap) exisitingMapArray.get(i)).get("formname")
										.equals(fileInformation.getFileName())) {
									try {
										org.json.JSONArray mappingArray = new org.json.JSONArray();
										LinkedHashMap widObject = (LinkedHashMap) exisitingMapArray.get(i);
										widObject.put("formname", fileInformation.getUpdatedName() + fileType);
										mappingArray.put(widObject);
									} catch (JSONException e) {
										throw new RuntimeException(e);
									}
								}

							}
						}
					}
					JSONObject widObject = new JSONObject(Files.readString(workFlowFile.toPath()));
					JSONObject newConfig = new JSONObject();
					newConfig.put("mapping", exisitingMapArray);
					widObject.remove("configuration");
					widObject.put("configuration", newConfig);
					widFileInfo.setContent(widObject.toString());
					widFileInfo.setComment("File saved");
					GITFileHelper myobj = new GITFileHelper();
					myobj.updateFileInRepository(widFileInfo);

				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (RuntimeException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return response;
	}

	public static EventResponseModel cloneFile(ParentRepositorySingleton parentRepositorySingleton, TemplateInformation templateInformation,
											   FileInformation fileInformation) throws IOException, CustomException, GitAPIException {

		EventResponseModel response = new EventResponseModel();

		try (Git sourceGit = RepositoryHelper.sourceRepository(templateInformation)) {
			try (Git destinationGit = RepositoryHelper.destinationRepository(parentRepositorySingleton, templateInformation)) {

				for (int i = 0; i < templateInformation.getFiledesc().getBpmn().size(); i++) {
					String bpmnlist = templateInformation.getFiledesc().getBpmn().get(i);
					File sourcebpmn = new File(sourceGit.getRepository().getDirectory().getParent() +
							"/src/main/resources/bpmn/" + bpmnlist);
					File destinationbpmn = new File(destinationGit.getRepository().getDirectory().getParent() +
							"/src/main/resources/bpmn/");
					FileUtils.copyFileToDirectory(sourcebpmn, destinationbpmn);
					destinationGit.add()
							.addFilepattern("src/main/")
							.call();
					destinationGit.commit().setMessage("updated").call();
					String fileName = String.valueOf(templateInformation.getFiledesc().getBpmn());
					Dbupdate(parentRepositorySingleton, templateInformation, fileName, fileInformation);

				}

				for (int i = 0; i < templateInformation.getFiledesc().getForms().size(); i++) {
					String formlist = templateInformation.getFiledesc().getForms().get(i);
					File sourceform = new File(sourceGit.getRepository().getDirectory().getParent() +
							"/src/main/resources/form/" + formlist);
					File destinationform = new File(destinationGit.getRepository().getDirectory().getParent() +
							"/src/main/resources/form/");
					FileUtils.copyFileToDirectory(sourceform, destinationform);
					destinationGit.add()
							.addFilepattern("src/main/")
							.call();
					destinationGit.commit().setMessage("updated").call();
					String fileName = String.valueOf(templateInformation.getFiledesc().getForms());
					Dbupdate(parentRepositorySingleton, templateInformation, fileName, fileInformation);
				}

				for (int i = 0; i < templateInformation.getFiledesc().getDmn().size(); i++) {
					String dmnlist = templateInformation.getFiledesc().getDmn().get(i);
					File sourcedmn = new File(sourceGit.getRepository().getDirectory().getParent() +
							"/src/main/resources/dmn/" + dmnlist);
					File destinationdmn = new File(destinationGit.getRepository().getDirectory().getParent() +
							"/src/main/resources/dmn/");
					FileUtils.copyFileToDirectory(sourcedmn, destinationdmn);
					destinationGit.add()
							.addFilepattern("src/main/")
							.call();
					destinationGit.commit().setMessage("updated").call();
					String fileName = String.valueOf(templateInformation.getFiledesc().getDmn());
					Dbupdate(parentRepositorySingleton, templateInformation, fileName, fileInformation);
				}

				for (int i = 0; i < templateInformation.getFiledesc().getDatamodel().size(); i++) {
					String datamodellist = templateInformation.getFiledesc().getDatamodel().get(i);
					File sourcedatamodel = new File(sourceGit.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/" + datamodellist);
					File destinationdatamodel = new File(destinationGit.getRepository().getDirectory().getParent() +
							"/src/main/java/io/intelliflow/generated/models/");
					FileUtils.copyFileToDirectory(sourcedatamodel, destinationdatamodel);
					String fileName = String.valueOf(templateInformation.getFiledesc().getDatamodel());
					Dbupdate(parentRepositorySingleton, templateInformation, fileName, fileInformation);
				}
				return response;
			}
		}
	}

	public static void Dbupdate(ParentRepositorySingleton parentRepositorySingleton, TemplateInformation templateInformation, String fileName,
			FileInformation fileInformation) {

		FilesByApp filesByApp = new FilesByApp(fileInformation.getWorkspaceName(), fileInformation.getMiniApp(), fileInformation.getFileName(),
				new Date(), new Date(), "Active", fileInformation.getUserId());
		ParentRepository<FilesByApp> filesByAppParentRepository = (ParentRepository<FilesByApp>) parentRepositorySingleton.getRepository("filesByApp");
		filesByAppParentRepository.saveModel(filesByApp);

		ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
		AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename",templateInformation.getDestworkspaceName(),"appname", templateInformation.getDestminiApp()));
		if(appsByName != null) {
			Set<String> files = appsByName.getFiles();
			files.add(fileName);
			appsByName.setFiles(files);
			appsByNameParentRepository.updateModel(appsByName);
		}

		ParentRepository<DataByUser> dataByUserParentRepository = (ParentRepository<DataByUser>) parentRepositorySingleton.getRepository("dataByUser");
		DataByUser result =  dataByUserParentRepository.modelWithFilters(Map.of("userid", fileInformation.getUserId()));

		if (result != null) {
			Set<String> filesBataByUser = result.getFiles();
			filesBataByUser.add(fileName);
			result.setFiles(filesBataByUser);
			dataByUserParentRepository.updateModel(result);
		} else {
			DataByUser data = new DataByUser(fileInformation.getUserId(), null, Set.of(fileName), null);
		    dataByUserParentRepository.saveModel(data);
		}
	}

	public EventResponseModel updateMessageProperties(MessageEventModel model) {

		String miniAppPath = repositoryFolderPath +
				model.getWorkspacename() + "/" + model.getAppname();
		EventResponseModel response = new EventResponseModel();
		response.setMessage("File renamed successfully");
		if (model.getStartMessages().isEmpty() && model.getEndMessages().isEmpty()) {
			System.out.println("No Messages Found");
			response.setMessage("No Messages here");
			return response;
		}
		try {
			File properties = new File(miniAppPath + "/src/main/resources/application.properties");
			FileReader reader = new FileReader(properties);
			Properties p = new Properties();
			p.load(reader);
			Set<Object> keys = p.keySet();
			/*
			 * for(Object key : keys) {
			 * if(key.toString().contains("mp.messaging") &&
			 * !key.toString().contains("mp.messaging.outgoing.kogito-") ) {
			 * p.remove(key);
			 * }
			 * }
			 */
			Set<String> startMsg = new LinkedHashSet<>(model.getStartMessages());
			Set<String> endMsg = new LinkedHashSet<>(model.getEndMessages());
			for (String startEvent : startMsg) {
				int temp = 0;
				for (Object key : keys) {
					if (key.toString().contains("mp.messaging.incoming." + startEvent)) {
						temp++;
					}
				}
				if (temp == 0) {
					p.put("mp.messaging.incoming." + startEvent + ".connector", "smallrye-kafka");
					p.put("mp.messaging.incoming." + startEvent + ".value.deserializer",
							"org.apache.kafka.common.serialization.StringDeserializer");
					p.put("mp.messaging.incoming." + startEvent + ".auto.offset.reset", "earliest");
				}
			}

			for (String endEvent : endMsg) {
				int temp = 0;
				for (Object key : keys) {
					if (key.toString().contains("mp.messaging.incoming." + endEvent + "-out")) {
						temp++;
					}
				}
				if (temp == 0) {
					String channelName = endEvent + "-out";
					p.setProperty("mp.messaging.outgoing." + channelName + ".connector", "smallrye-kafka");
					p.setProperty("mp.messaging.outgoing." + channelName + ".topic", endEvent);
					p.setProperty("mp.messaging.outgoing." + channelName + ".value.serializer",
							"org.apache.kafka.common.serialization.StringSerializer");
					p.setProperty("kogito.addon.messaging.outgoing.trigger." + endEvent, channelName);
				}
			}
			FileWriter writer = new FileWriter(properties);
			p.store(writer, model.getAppname() + " Properties");
			reader.close();
			writer.close();
			response.setMessage("Properties added");
			return response;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
