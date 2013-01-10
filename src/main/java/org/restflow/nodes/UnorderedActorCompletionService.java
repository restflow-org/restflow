package org.restflow.nodes;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.restflow.actors.AbstractActorRunner;
import org.restflow.actors.Actor;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.SynchronousActorRunner;
import org.restflow.actors.UnorderedActorRunner;

import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class UnorderedActorCompletionService extends AsynchronousActorCompletionService {

	protected final BlockingQueue<ActorRunner> _steppedActorQueue;
	private UnorderedCompletionManager _completionManager;
	
	public UnorderedActorCompletionService(int maxConcurrency) {
		
		super(maxConcurrency);
		
		_steppedActorQueue = new LinkedBlockingQueue<ActorRunner>(maxConcurrency);
//		_completionManager = new UnorderedCompletionManager();
		
		synchronized(this) {
//			_completionManagementThread = new Thread(_completionManager);
		}
	}
	
	
	public synchronized void start() {
		
		super.start();
		_completionManager = new UnorderedCompletionManager();
		_completionManagementThread = new Thread(_completionManager);		
		_completionManagementThread.start();		
	}
	
	public synchronized Actor submit(Actor actorMaster, Map<String,Object> variables) {

		Actor actor = super.submit(actorMaster, variables);
		
		_completionManager.incrementSubmittedRunnerCount();
		
		UnorderedActorRunner actorRunner =  new UnorderedActorRunner(actor, variables, _steppedActorQueue);

		try {
			_runningActorQueue.put(actorRunner);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		actorRunner.start();
		
		return actor;
	}
	
	public void signalEndOfRunners() {		
		_completionManager.endOfRunners();
	}
	
	private class UnorderedCompletionManager implements Runnable {
		
		private int _submittedRunnerCount;
		private int _completedRunnerCount;
		private boolean _endOfRunnersSeen;

		public UnorderedCompletionManager() {
			
			synchronized(this) {
				_submittedRunnerCount = 0;
				_completedRunnerCount = 0;
				_endOfRunnersSeen = false;
			}
		}
		
		public synchronized void incrementSubmittedRunnerCount() {
			_submittedRunnerCount++;
		}
		
		public synchronized void endOfRunners() {
			try {
				 _endOfRunnersSeen = true;
				_steppedActorQueue.put(AbstractActorRunner.EndOfActorRunners);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			
			try {
				while (true) {
					
					synchronized(this) {
						if (_endOfRunnersSeen && _completedRunnerCount == _submittedRunnerCount) {
							break;
						}
					}

					ActorRunner steppedRunner = _steppedActorQueue.take();
					
					if (steppedRunner != AbstractActorRunner.EndOfActorRunners) {
						_runningActorQueue.take();
						_completedActorQueue.put(steppedRunner);
						synchronized(this) { _completedRunnerCount++; }
					}
				}

				_completedActorQueue.put(SynchronousActorRunner.EndOfActorRunners);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
