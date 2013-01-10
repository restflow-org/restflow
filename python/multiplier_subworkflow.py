import ssrl.workflow.actors.WorkflowBuilder as WorkflowBuilder
import ssrl.workflow.WorkflowContextBuilder as WorkflowContextBuilder
import ssrl.workflow.nodes.PythonNodeBuilder as PythonNodeBuilder
import ssrl.workflow.data.ConsumableObjectStore as ConsumableObjectStore
import ssrl.workflow.nodes.WorkflowNodeBuilder as WorkflowNodeBuilder

EOL = '\n'

store = ConsumableObjectStore()
    
# create a context for the workflow run
context = (WorkflowContextBuilder()
               .store(store)
               .build())

# define the worlfow
workflow = (WorkflowBuilder()

    .name("top")
    .context(context)
    
    .inflow("u", "/inputNumber")

    .node(PythonNodeBuilder() 
        .type("m", "Integer")
        .type("n", "Integer")
        .inflow("/inputNumber", "m")
        .stepsOnce()
        .step("n = m + 1")
        .outflow("n", "/incrementedInputNumber"))

    .node(WorkflowNodeBuilder()

        .name("multiplier")
        .stepsOnce()
        
        .inflow("/inputNumber", "/multiplier")
        .inflow("/incrementedInputNumber", "/multiplicand")
        
        .node(PythonNodeBuilder()
            .type("x", "Integer")
            .type("y", "Integer")
            .type("z", "Integer")
            .inflow("/multiplier", "x")
            .inflow("/multiplicand", "y")
            .stepsOnce()
            .step(  "z = x * y            "       + EOL +
                    "print x, '*', y      "       + EOL )
            .outflow("z", "/product"))
        
        .node(PythonNodeBuilder()
            .inflow("/product", "value")
            .stepsOnce()
            .step("print value"))
            
        .outflow("/product", "/outputNumber"))
        
    .outflow("/outputNumber", "v")

    .build()
)

# configure and initialize the workflow
workflow.configure()


for u in range(0,4):
    workflow.initialize()
    workflow.set("u", u)
    workflow.run()
    v = workflow.get("v")
    
    assert v == u*(u+1)
#    
#    assert u    ==  store.take("/inputNumber")
#    assert u+1  ==  store.take("/incrementedInputNumber")
#    assert u    ==  store.take("/top.multiplier/multiplier")
#    assert u+1  ==  store.take("/top.multiplier/multiplicand")
#    assert v    ==  store.take("/top.multiplier/product")
#    assert v    ==  store.take("/outputNumber")
#    assert 0    ==  store.size()


