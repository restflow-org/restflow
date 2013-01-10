package org.restflow.nodes;

import java.util.Map;

import org.restflow.actors.AbstractActorRunner;
import org.restflow.actors.Actor;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.OrderedActorRunner;

import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class OrderedActorCompletionService extends AsynchronousActorCompletionService {

	public OrderedActorCompletionService(int maxConcurrency) {
		super(maxConcurrency);

//		Runnable completionManager = new OrderedCompletionManager();

		synchronized(this) {
//			_completionManagementThread = new Thread(completionManager);
		}
	}
	
	public synchronized void start() {
		
		super.start();
		
		Runnable completionManager = new OrderedCompletionManager();
		_completionManagementThread = new Thread(completionManager);
		_completionManagementThread.start();		
	}

	
	public synchronized Actor submit(Actor actorMaster, Map<String,Object> variables) {

		Actor actor = super.submit(actorMaster, variables);

		OrderedActorRunner actorRunner =  new OrderedActorRunner(actor, variables);
		
		actorRunner.start();

		try {
			_runningActorQueue.put(actorRunner);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return actor;
	}

	@Override
	public void signalEndOfRunners() {
		try {
			_runningActorQueue.put(AbstractActorRunner.EndOfActorRunners);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class OrderedCompletionManager implements Runnable {
		
		public void run() {
			
			try {
				while (true) {
					
					ActorRunner runner = _runningActorQueue.take();
				
					if (runner == AbstractActorRunner.EndOfActorRunners) {
						_completedActorQueue.put(AbstractActorRunner.EndOfActorRunners);
						break;
					} else {
						((OrderedActorRunner)runner).waitForCompletion();
						_completedActorQueue.put(runner);
					}
				}
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
				Thread.currentThread().interrupt();
			}
		}
	}
}
