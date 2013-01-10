import ssrl.workflow.actors.WorkflowBuilder as WorkflowBuilder
import ssrl.workflow.WorkflowContextBuilder as WorkflowContextBuilder
import ssrl.workflow.nodes.PythonNodeBuilder as PythonNodeBuilder
import jarray
import java

# create a context for the workflow run
context = WorkflowContextBuilder().build()

# define the worlfow
workflow =  (WorkflowBuilder()

    .context(context)

    .name("Hello")

    .node(PythonNodeBuilder()
        .name("CreateGreeting")
        .sequence('greeting', jarray.array([	
            'Hello',
            'Goodbye', 
            'Have a good night'
            ], java.lang.String))
        .step('g = greeting')
        .outflow('g', '/greeting'))

    .node(PythonNodeBuilder()
        .name("PrintGreeting")
        .inflow("/greeting", "text")
        .step("print text"))

    .build()
)

# configure and initialize the workflow
workflow.configure()		
workflow.initialize()

# run the workflow once
workflow.run()

