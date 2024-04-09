package io.intelliflow.tester;

import java.io.IOException;

import io.intelliflow.helper.GITFileHelper;
import io.quarkus.runtime.QuarkusApplication;

public class RunTest  implements QuarkusApplication{

//	public static void main(String[] args) throws GitAPIException {
//		try {
////			Repository repo = RepositoryHelper.createRepositorØ
//			GITFileHelper fileOperations = new GITFileHelper();
//			//fileOperations.createFileInRepository(null, null, null, null, null);
//			///fileOperations.findResourceFiles();
//			
//
//			
//			
//			fileOperations.updateFileInReposiotry(null);
//			
//			//fileOperations.deleteFileInRepository();
//		
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	

	@Override
	public int run(String... args) throws Exception {
		
		try {
//			Repository repo = RepositoryHelper.createRepositorØ
			GITFileHelper fileOperations = new GITFileHelper();
			//fileOperations.createFileInRepository(null, null, null, null, null);
			///fileOperations.findResourceFiles();
			

			
			//directoryManager.mirrorRepository("fc1");
			
			fileOperations.updateFileInRepository(null);
			
			//fileOperations.deleteFileInRepository();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

}
