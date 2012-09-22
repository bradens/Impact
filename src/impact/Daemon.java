package impact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import messaging.MessageCenter;
import models.Changeset;
import models.Message;
import models.Method;
import models.Owner;
import models.Pair;
import models.Range;
import ast.CallGraphGenerator;
import git.GitController;
import db.DatabaseConnector;
import diff.UnifiedDiffParser;

import impact.Resources;

public class Daemon {
	
	private DatabaseConnector db;
	private GitController gc;
	private UnifiedDiffParser udp;
	private CallGraphGenerator cgg;
	
	public Daemon(DatabaseConnector db) {
		this.db = db;
		gc = new GitController();
		udp = new UnifiedDiffParser();
		cgg = new CallGraphGenerator(db);
	}
	
	public void run() {
		long startTime = 0;
		
		// Build the initial call graph on daemon start
		buildCallGraph();
		String currentCommit = db.getCurrentCommit();
		
		for(;;) {
			if(startTime ==0)
				startTime = System.currentTimeMillis();
			startTime += (1000.0 / Resources.fps);
			
			if(currentCommit == null || !currentCommit.equals(gc.getHead())) {
				buildCallGraph();
				currentCommit = db.getCurrentCommit();
			}
			
			//Diff
			System.out.println("Diffing for current changes");
			List<Changeset> changeset = udp.parse(gc.getDiffJavaOnly(gc.getAllFiles()));
			
			if(!changeset.isEmpty()) {
				List<Pair<Method, Float>> changedMethods = getMethodsOfChangeset(changeset);
				for(Pair<Method, Float> changedMethod: changedMethods) {
					// Do something with the changed methods here
					List<Method> callers = getCallers(changedMethod);
					for(Method caller: callers) {
						List<Pair<Method, Owner>> owners = blameCaller(caller);
						for(Pair<Method, Owner> owner: owners) {
							// Do something here with actual messaging
							Message message = new Message(owner.getSecond().getEmail(), "Me", 
									"Changed a function that impacted yours.", 
									changedMethod.getFirst().toString(), 
									owner.getFirst().toString());
							MessageCenter mc = new MessageCenter();
							if(Resources.email)
								mc.sendAsEmail(message);
							if(Resources.tweet)
								mc.sendAsTweet(message);
						}
					}
				}
			}
			
			LockSupport.parkNanos((long)(Math.max(0, 
				    startTime - System.currentTimeMillis()) * 1000000));
		}
		
	}
	
	public void buildCallGraph() {
		System.out.println("Generating call graph for current HEAD of repository");
		db.deleteCallGraph();
		cgg.createCallGraphAtCommit(gc.getHead());
	}
	
	private List<Pair<Method, Float>> getMethodsOfChangeset(List<Changeset> changesets) {
		List<Pair<Method, Float>> changedMethods = new ArrayList<Pair<Method, Float>>();
		for(Changeset changeset: changesets) {
			for(Range range: changeset.getRanges()) {
				updateChangedMethods(changedMethods, db.getChangedMethods(changeset.getNewFile(), 
						range.getStart(), range.getEnd()));
			}
		}

		return changedMethods;
	}
	
	/**
	 * This method updates the list of changed methods so that
	 * you have no duplicate listings of methods (update their
	 * weight instead).
	 * @param methods
	 * @param method
	 */
	private void updateChangedMethods(List<Pair<Method, Float>> changedMethods, 
			List<Pair<Method, Float>> newChanges) {
		for(Pair<Method, Float> change: newChanges) {
			boolean inserted = false;
			for(Pair<Method, Float> method: changedMethods) {
				if(method.getFirst().getStart() == change.getFirst().getStart() && 
						method.getFirst().getEnd() == change.getFirst().getEnd()) {
					method.setSecond(Math.min(method.getSecond() + change.getSecond(), 1.0f));
					inserted = true;
					break;
				}
			}
			if(!inserted)
				changedMethods.add(change);
		}
	}
	
	private List<Method> getCallers(Pair<Method, Float> method) {
		return db.getCallersOfMethod((Method)method.getFirst());
	}
	
	private List<Pair<Method, Owner>> blameCaller(Method method) {
		List<Pair<Method, Owner>> owners = new ArrayList<Pair<Method, Owner>>();
		List<Owner> ownersOfMethod = gc.getOwnersOfFileRange(method.getFile(), 
				method.getStart(), method.getEnd());
		for(Owner owner: ownersOfMethod) {
			owners.add(new Pair<Method, Owner>(method, owner));
		}
		return owners;
	}

}
