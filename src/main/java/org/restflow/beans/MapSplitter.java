package org.restflow.beans;

import java.util.Map;

import org.restflow.actors.AbstractActor;
import org.restflow.util.Contract;


public class MapSplitter extends AbstractActor {

	@Override
	public void step() throws Exception {
		super.step();

		_outputValues = (Map<String,Object>)_inputValues.get("map");
		
		_state = ActorFSM.STEPPED;
	}
	
	public void afterPropertiesSet() throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}

	public void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}

	@Override
	public void configure() throws Exception {
		super.configure();
		_state = ActorFSM.CONFIGURED;
	}	
	
	@Override
	public void initialize() throws Exception {
		super.initialize();
		
		_state = ActorFSM.INITIALIZED;
	}	
	
	@Override
	public void wrapup() throws Exception {
		super.wrapup();
		_state = ActorFSM.WRAPPED_UP;	
	}

	@Override
	public void dispose() throws Exception {
		super.dispose();
		_state = ActorFSM.DISPOSED;	
	}
	
	
}
