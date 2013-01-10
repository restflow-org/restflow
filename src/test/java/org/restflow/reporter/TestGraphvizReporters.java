package org.restflow.reporter;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.InflowProperty;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.Director;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.reporter.GraphvizReporter;
import org.restflow.test.RestFlowTestCase;


public class TestGraphvizReporters extends RestFlowTestCase {

	public void test_GraphvizReporters_SimpleWorkflow() throws Exception {
		
		for (Director director : new Director[] {new PublishSubscribeDirector(), new DataDrivenDirector()} ) {

			WorkflowContext context = new WorkflowContextBuilder().build();
			
			final Workflow workflow = new WorkflowBuilder()
			
			.name("TopWF")
			.context(context)
			.prefix("/RUN")
			.director(director)
			
			.inflow("v", "/multiplier")
	
			.node(new GroovyNodeBuilder()
				.name("GenerateScaleFactors")
				.sequence("constant", new Object [] {2, 5, 30})
				.step("value=constant")
				.outflow("value", "/scaleFactor"))					
					
			.node(new JavaNodeBuilder()
				.name("ScaleMultipliers")
				.inflow("/scaleFactor", "s")
				.inflow("/multiplier", "c", InflowProperty.ReceiveOnce)
				.bean(new Object() {
						public int c, s, v;
						public void step() { v = s * c; }
					})
				.outflow("v", "/scaledMultiplier"))
	
			.node(new JavaNodeBuilder()
				.name("MultiplySequenceByEight")
				.inflow("/scaledMultiplier", "b")
				.bean(new Object() {
					public int a, b, c;
					public void step() { c = 8 * b; }
				})
				.outflow("c", "/product"))
	
			.node(new JavaNodeBuilder()
				.name("RenderProducts")
				.inflow("/product", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
	
			.outflow("/product", "e")
			
			.reporter("DotReport", new GraphvizReporter.WorkflowGraphReporter())
			
			.build();
	
			workflow.configure();
			
			assertEquals(
					"digraph Workflow {"														+ EOL +
					"node1 [label=\"inportal\",shape=ellipse,peripheries=1];"					+ EOL +
					"node2 [label=\"outportal\",shape=ellipse,peripheries=1];"					+ EOL +
					"node3 [label=\"GenerateScaleFactors\",shape=ellipse,peripheries=1];"		+ EOL +
					"node4 [label=\"ScaleMultipliers\",shape=ellipse,peripheries=1];"			+ EOL +
					"node5 [label=\"MultiplySequenceByEight\",shape=ellipse,peripheries=1];"	+ EOL +
					"node6 [label=\"RenderProducts\",shape=ellipse,peripheries=1];"				+ EOL +
					"node7 [label=\"/multiplier\",shape=box,peripheries=1];"					+ EOL +
					"node1 -> node7 [label=\"v\"];"												+ EOL +
					"node8 [label=\"/scaleFactor\",shape=box,peripheries=1];"					+ EOL +
					"node3 -> node8 [label=\"value\"];"											+ EOL +
					"node9 [label=\"/scaledMultiplier\",shape=box,peripheries=1];"				+ EOL +
					"node4 -> node9 [label=\"v\"];"												+ EOL +
					"node10 [label=\"/product\",shape=box,peripheries=1];"						+ EOL +
					"node5 -> node10 [label=\"c\"];"											+ EOL +
					"node10 -> node2 [label=\"e\"];"											+ EOL +
					"node8 -> node4 [label=\"s\"];"												+ EOL +
					"node7 -> node4 [label=\"c\"];"												+ EOL +
					"node9 -> node5 [label=\"b\"];"												+ EOL +
					"node10 -> node6 [label=\"v\"];"											+ EOL +
					"}"																			+ EOL,
				workflow.getReport("DotReport"));
		}
	}
	
	public void test_GraphvizReporters_NestedWorkflow() throws Exception {
		
		for (Director director : new Director[] {new PublishSubscribeDirector(), new DataDrivenDirector()} ) {

			WorkflowContext context = new WorkflowContextBuilder().build();
			
			final 	Workflow workflow = new WorkflowBuilder()

			.name("top")
			.context(context)
			.prefix("/run{RUN}")
			
			.inflow("u", "/inputNumber")
		
			.node(new GroovyNodeBuilder() 
				.name("incrementer")
				.inflow("/inputNumber", "m")
				.stepsOnce()
				.step("n = m + 1")
				.outflow("n", "/incrementedInputNumber"))
			
			.node(new WorkflowNodeBuilder()
			
				.stepsOnce()
			
				.name("multiplier")
				.inflow("/inputNumber", "multiplier", "/multiplier")
				.inflow("/incrementedInputNumber", "multiplicand", "/multiplicand")
				
				.node(new JavaNodeBuilder()
					.inflow("/multiplier", "x")
					.inflow("/multiplicand", "y")
					.stepsOnce()
					.bean(new CloneableBean() {
						public int x, y, z;
						public void step() {z = x * y; System.out.println(x + "*" + y);}
						})
					.outflow("z", "/product"))
				
				.node(new GroovyNodeBuilder()
					.inflow("/product", "value")
					.step("println value"))
					
				.outflow("/product", "product", "/outputNumber")
				)
				
			.outflow("/outputNumber", "v")

			.reporter("DotReport", new GraphvizReporter.WorkflowGraphReporter())
			
			.build();
			
			
			workflow.configure();
			
			assertEquals(
					"digraph Workflow {"													+ EOL +
					"node1 [label=\"inportal\",shape=ellipse,peripheries=1];"				+ EOL +
					"node2 [label=\"outportal\",shape=ellipse,peripheries=1];"				+ EOL +
					"node3 [label=\"incrementer\",shape=ellipse,peripheries=1];"			+ EOL +
					"node4 [label=\"multiplier\",shape=ellipse,peripheries=2];"				+ EOL +
					"node5 [label=\"/inputNumber\",shape=box,peripheries=1];"				+ EOL +
					"node1 -> node5 [label=\"u\"];"											+ EOL +
					"node6 [label=\"/incrementedInputNumber\",shape=box,peripheries=1];"	+ EOL +
					"node3 -> node6 [label=\"n\"];"											+ EOL +
					"node7 [label=\"/outputNumber\",shape=box,peripheries=1];"				+ EOL +
					"node4 -> node7 [label=\"product\"];"									+ EOL +
					"node7 -> node2 [label=\"v\"];"											+ EOL +
					"node5 -> node3 [label=\"m\"];"											+ EOL +
					"node6 -> node4 [label=\"multiplicand\"];"								+ EOL +
					"node5 -> node4 [label=\"multiplier\"];"								+ EOL +
					"}"																		+ EOL,
				workflow.getReport("DotReport"));
		}
	}
}
