package org.restflow.directors;

import java.util.List;

import net.jcip.annotations.GuardedBy;

import org.restflow.WorkflowContext;
import org.restflow.actors.Workflow;
import org.restflow.data.InflowToOutflowsMap;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.TraceRecorder;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.Contract;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;


public abstract class AbstractDirector implements Director  {

	///////////////////////////////////////////////////////////////////////////
	////                    private instance fields                        ////

	protected DirectorFSM _state;

	///////////////////////////////////////////////////////////////////////////
	////                   protected instance fields                       ////

	protected Workflow _workflow;
	protected List<WorkflowNode> _nodes;
	protected InflowToOutflowsMap _inflowToOutflowsMap;
	protected boolean _nodesStepOnce = false;
	@GuardedBy("this") protected WorkflowContext _workflowContext;
	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public AbstractDirector() {
		_state = DirectorFSM.CONSTRUCTED;
	}
	
	///////////////////////////////////////////////////////////////////////////
	///   workflow configuration setters -- PROPERTIES_UNSET state only    ////
		
	public void setNodesStepOnce(boolean nodesStepOnce) {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED);
		_nodesStepOnce = nodesStepOnce;
	}

	///////////////////////////////////////////////////////////////////////////
	///   actor configuration setters -- UNCONFIGURED   ////
	
	public synchronized void setApplicationContext(ApplicationContext context) throws BeansException {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED || _state == DirectorFSM.PROPERTIES_SET);
		_workflowContext = (WorkflowContext)context;
	}

	public void setWorkflow(Workflow workflow) {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED || _state == DirectorFSM.PROPERTIES_SET);
		_workflow = workflow;
	}
	
	///////////////////////////////////////////////////////////////////////////
	////              public director lifecycle methods                    ////
	
	public void afterPropertiesSet() {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED);
	}
	
	public boolean nodesStepOnce() {
		return _nodesStepOnce;
	}
	
	
	public WorkflowModified elaborate() throws Exception {
		
		Contract.requires(_state == DirectorFSM.PROPERTIES_SET || _state == DirectorFSM.MODIFIED);

		_nodes = _workflow.getNodes();
		_inflowToOutflowsMap = _workflow.getInflowToOutflowsMap();

//		if (_nodesStepOnce) {
//			for (WorkflowNode node: _nodes) {
//				node.setStepsOnce(true);
//			}
//		}

		return WorkflowModified.FALSE;
	}
	
	public void configure() throws Exception {	
		Contract.requires(_state == DirectorFSM.ELABORATED);
	}
	
	public void initialize() throws Exception {
		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		Contract.requires(_state == DirectorFSM.CONFIGURED || _state == DirectorFSM.WRAPPED_UP);
	}
	
	public void run() throws Exception {
 		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.WRAPPED_UP);
	}
	
	public WrapupResult wrapup() throws Exception {
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);
		return null;
	}

	public void dispose() throws Exception {
		Contract.requires(_state == DirectorFSM.WRAPPED_UP);
	}
	
	
	public DirectorFSM state() {
		return _state;
	}
}