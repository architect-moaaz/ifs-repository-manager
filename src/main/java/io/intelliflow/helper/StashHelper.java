package io.intelliflow.helper;

import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.StashCreateCommand;
import org.eclipse.jgit.api.StashListCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.StashApplyFailureException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class StashHelper {

	// create stash will work only after adding the resources using git add
	public static void createStash(Repository repository) {

		StashCreateCommand stashCreateCommand = Git.wrap(repository).stashCreate();

		try {

			PersonIdent person = new PersonIdent("Moaaz", "moaaz.shareef@intelliflow.io");

			stashCreateCommand.setIndexMessage("Index Message 1");
			stashCreateCommand.setWorkingDirectoryMessage("Working Directory Message 1");
			stashCreateCommand.setPerson(person);

			RevCommit revCommit = stashCreateCommand.call();

		} catch (GitAPIException e) {
			e.printStackTrace();
		}

	}

	public static void fetchStashList(Repository repository) {

		StashListCommand stashListCommand = Git.wrap(repository).stashList();

		try {

			Collection<RevCommit> stashList = stashListCommand.call();

			int refCount = 0;

			for (RevCommit revCommit : stashList) {
				System.out.println(revCommit.getName());
				System.out.println(revCommit.getFullMessage());
				System.out.println(revCommit.getCommitterIdent().getName());
				System.out.println("ref : " + refCount);
				System.out.println("=====================================");

				refCount++;
			}

		} catch (InvalidRefNameException e) {

			e.printStackTrace();

		} catch (GitAPIException e) {

			e.printStackTrace();
		}
	}

	public static void dropStash(Repository repository) throws GitAPIException {

		ObjectId objectId = Git.wrap(repository).stashDrop().setStashRef(0).call();
		System.out.println("Drop ObjectId : " + objectId);

	}

	public static void applyStash(Repository repository)
			throws WrongRepositoryStateException, NoHeadException, StashApplyFailureException, GitAPIException {

		ObjectId objectId = Git.wrap(repository).stashApply().setStashRef("d241a9a0dafc53dbc2333032437c0501d1bb781b")
				.call();
		System.out.println("Apply ObjectId : " + objectId);

	}

}
