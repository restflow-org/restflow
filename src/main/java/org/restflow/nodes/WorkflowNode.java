package org.restflow.nodes;

import java.util.Collection;
import java.util.Map;

import org.restflow.actors.Workflow;
import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.enums.ChangedState;
import org.restflow.exceptions.RestFlowException;
import org.restflow.metadata.WrapupResult;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;


public interface WorkflowNode extends Comparable<WorkflowNode> {
	
	/**************************
	 *  Configuration setters *
	 **************************/
	void setApplicationContext(ApplicationContext context) throws BeansException;
	void setName(String name);
	void registerInflow(String label, String outflowPath, boolean b) throws Exception;
	void registerInflow(String label, String newNodeOutflowBinding,
			String inflowPacketBinding, boolean receiveOnce) throws Exception;
	void replaceInflow(String label, String outflowPath) throws Exception;
	Outflow registerOutflow(String label, String binding, boolean isDefaultUri) throws Exception;
	void setInflows(Map<String, Object> inflows) throws Exception;
	void setOutflows(Map<String, String> outflows) throws Exception;
	void setStepsOnce(boolean b);
	void setUriPrefix(String prefix);
	void setWorkflow(Workflow workflow);
	
	/**************************
	 *  Configuration getters *
	 **************************/
	Collection<String> getInflowLabels();
	Map<String,Inflow> getLabelInflowMap();
	int getMaxConcurrentSteps();
	Collection<Inflow> getNodeInflows();
	String getBeanName();
	String getName();
	String getUriPrefix();
	Map<String, Outflow> getOutflows();
	boolean isHidden();
	boolean stepsOnce();
	String getQualifiedWorkflowNodeName();
	String getWorkflowNodeNameAsUri();
	
	/**************************
	 *     Node lifecycle     *
	 **************************/
	void elaborate() throws Exception;
	void configure() throws Exception;
	void initialize() throws Exception;
	void reset() throws Exception;
	boolean readyForInputPacket(String label) throws Exception;
	void setInputPacket(String label, Packet packet) throws Exception;
	ChangedState trigger() throws Exception;
	public ChangedState manualStart() throws Exception;	
	public void manualFinish() throws Exception;		
	ChangedState startTrigger() throws Exception;
	void finishTrigger() throws Exception;
	boolean outputsReady() throws Exception;
	Packet getOutputPacket(String label) throws Exception;
	boolean allEosReceived();
	boolean allEosSent();
	boolean isNodeFinished() throws RestFlowException;
	WrapupResult wrapup() throws Exception;
	void dispose() throws Exception;

	
	/**************************
	 * Comparator for sorting *
	 **************************/
	int compareTo(WorkflowNode node);
	
	
	/**************************
	 *       Enumerations     *
	 **************************/
	enum DoneStepping {TRUE, FALSE, UNINITIALIZED};
	enum NodeFinished {TRUE, FALSE, UNINITIALIZED}
}