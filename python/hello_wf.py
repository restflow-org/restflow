import ssrl.workflow.actors.WorkflowBuilder as WorkflowBuilder
import ssrl.workflow.WorkflowContextBuilder as WorkflowContextBuilder
import ssrl.workflow.nodes.PythonNodeBuilder as PythonNodeBuilder

# create a context for the workflow run
context = WorkflowContextBuilder().build()

# define the worklfow
workflow = (WorkflowBuilder()

    # use the context created above
    .context(context)

    # name the workflow
    .name("Hello")

    # declare a single node along with a Groovy actor implementation
    .node(PythonNodeBuilder()
        .name("Greeter")
        .step("print 'Hello World!'"))

    # build the workflow
    .build()
)

# configure and initialize the workflow
workflow.configure()		
workflow.initialize()

# run the workflow once
workflow.run()
