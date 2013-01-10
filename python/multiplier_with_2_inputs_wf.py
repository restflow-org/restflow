import ssrl.workflow.actors.WorkflowBuilder as WorkflowBuilder
import ssrl.workflow.WorkflowContextBuilder as WorkflowContextBuilder
import ssrl.workflow.nodes.GroovyNodeBuilder as GroovyNodeBuilder

# create a context for the workflow run
context = WorkflowContextBuilder().build()

# define the worlfow
workflow =  (WorkflowBuilder()

    .context(context)

    .name("Multiplier")

    .inflow("a", "/multiplier")
    .inflow("b", "/multiplicand")

    .node(GroovyNodeBuilder()
        .type("x", "Integer")
        .type("y", "Integer")
        .type("z", "Integer")
        .inflow("/multiplier", "x")
        .inflow("/multiplicand", "y")
        .step("z = x * y")
        .outflow("z", "/product"))

    .node(GroovyNodeBuilder()
        .inflow("/product", "value")
        .step("println value"))
    
    .outflow("/product", "c")

    .build()
)

# configure and initialize the workflow
workflow.configure()		

for i in range(0,10):
    
    workflow.initialize()
    workflow.set("a", i)
    workflow.set("b", i * 3)
    
    print "Running workflow with a = %d and b = %d" % (i, i *3)
    workflow.run()
    print "Workflow output result c = %d" % workflow.get("c")
    
    workflow.wrapup()


