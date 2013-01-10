package org.restflow.directors;

import org.restflow.actors.Workflow;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.WrapupResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;


public interface Director extends ApplicationContextAware, InitializingBean, Cloneable {
	
	/*************************
	 * Configuration setters *
	 *************************/
	void setWorkflow(Workflow workflow);
	void setNodesStepOnce(boolean stepOnce);
	
	/**************************
	 *   Workflow lifecycle   *
	 *************************/
	WorkflowModified elaborate() throws Exception;
	void configure() throws Exception;
	void initialize() throws Exception;
	void run() throws Exception;
	WrapupResult wrapup() throws Exception;
	void dispose() throws Exception;
	boolean nodesStepOnce();
	
	DirectorFSM state();

	///////////////////////////////////////////////////////////////////////////
	////                   finite state enumeration                        ////
	
	enum DirectorFSM {
		CONSTRUCTED,
		PROPERTIES_SET,
		MODIFIED,
		ELABORATED,
		CONFIGURED,
		INITIALIZED,
		RUNNING,
		RAN,
		WRAPPED_UP,
		DISPOSED
	}


}