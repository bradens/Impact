package git;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Owner;

import process.Spawner;

import impact.Resources;

public class GitController {
	
	private Spawner spawner;

	public GitController() {
		spawner = new Spawner(Resources.repository);
	}
	
	public void reset(String commitID) {
		spawner.spawnProcess(new String[] {"git", "reset", "--hard", commitID});
	}

	public List<String> getAllCommits() {
		List<String> commits = new ArrayList<String>();
		parseLogForCommits(commits);
		return commits;
	}

	private void parseLogForCommits(List<String> commits) {
		String output = spawner.spawnProcess(new String[] {"git", "log", "--reverse", "--no-merges"});

		String[] lines = output.split(System.getProperty("line.separator"));
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].matches(Resources.gitLogCommit)) {
				String[] split = lines[i].split(" ");
				commits.add(split[1]);
			}
		}
	}

	public List<String> getAllFiles() {
		List<String> files = new ArrayList<String>();
		findAllJavaFiles(files);
		return files;
	}

	private void findAllJavaFiles(List<String> files) {
		String output = spawner.spawnProcess(new String[] {"find", "."});

		String[] lines = output.split(System.getProperty("line.separator"));
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].endsWith(".java")) {
				if(lines[i].startsWith("./"))
					lines[i] = lines[i].replaceFirst("./", "");
				files.add(lines[i]);
			}
		}
	}

	public String getCommitDiff(String commit) {
		return spawner.spawnProcess(new String[] {"git", "diff-tree", "--unified=0", commit});
	}

	public String getCommitDiffJavaOnly(String commit, List<String> javaFiles) {
		if(javaFiles.isEmpty())
			return getCommitDiff(commit);
		else {
			javaFiles.add(0, "--");
			javaFiles.add(0, commit);
			javaFiles.add(0, "--unified=0");
			javaFiles.add(0, "diff-tree");
			javaFiles.add(0, "git");
		}
		return spawner.spawnProcess(javaFiles);
	}
	
	public String getDiffJavaOnly(List<String> javaFiles) {
		if(javaFiles.isEmpty())
			return "";
		else {
			javaFiles.add(0, "--");
			javaFiles.add(0, "diff");
			javaFiles.add(0, "git");
		}
		return spawner.spawnProcess(javaFiles);
	}

	public String getAuthorOfCommit(String commit) {
		return spawner.spawnProcess(new String[] {"git", "show", "-s", "--format=%ce", commit})
				.replace("\n", "");
	}

	public List<Owner> getOwnersOfFileRange(String file, int start, int end) {
		List<Owner> owners = new ArrayList<Owner>();

		String output = spawner.spawnProcess(new String[] {"git", "blame", "-e", "-L"+start+","+end, file});
		String[] lines = output.split(System.getProperty("line.separator"));

		for(int i = 0; i < lines.length; i++) {
			Pattern pattern = Pattern.compile(Resources.gitBlame);
			Matcher matcher = pattern.matcher(lines[i]);

			if(matcher.find()) {
				Owner owner = new Owner();
				owner.setEmail(matcher.group(1));
				owner.setOwnership((float)1/(end-start+1));
				updateOwnersList(owners, owner);
			}
		}

		return owners;
	}

	private void updateOwnersList(List<Owner> owners, Owner owner) {
		for(Owner own: owners) {
			if(own.getEmail().equals(owner.getEmail())) {
				own.setOwnership(own.getOwnership()+owner.getOwnership());
				return;
			}
		}
		owners.add(owner);
	}

	/**
	 * Returns the HEAD commit ID of the repository.
	 * @return
	 */
	public String getHead() {
		String output = spawner.spawnProcess(new String[] {"git", "rev-parse", "HEAD"});
		String[] lines = output.split(System.getProperty("line.separator"));

		Pattern pattern = Pattern.compile(Resources.gitHead);
		Matcher matcher = pattern.matcher(lines[0]);

		if(matcher.find()) {
			return lines[0];
		}

		return null;
	}
}
