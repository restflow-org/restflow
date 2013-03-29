package org.restflow.actors;

import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.ActorStatus;
import org.restflow.actors.JavaActor;
import org.restflow.actors.Workflow;
import org.restflow.exceptions.ActorDeclarationException;
import org.restflow.exceptions.InputTypeException;
import org.restflow.exceptions.NullInputException;
import org.restflow.exceptions.RestFlowException;
import org.restflow.exceptions.WorkflowRuntimeException;
import org.restflow.test.RestFlowTestCase;
import org.yaml.snakeyaml.Yaml;


@SuppressWarnings("unchecked")
public class TestJavaActor extends RestFlowTestCase {

	private WorkflowContext _context;
	private static String EOL = System.getProperty("line.separator");
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_context = new WorkflowContextBuilder()
			.build();
		
		MultiplyBean.throwExceptionInConstructor = false;
		MultiplyBean.throwExceptionInSetter = false;
		MultiplyBean.throwExceptionInStep = false;
		MultiplyBean.throwExceptionInGetter = false;
		MultiplyBean.throwExceptionInInitialize = false;
		MultiplyBean.throwExceptionInStep = false;
		MultiplyBean.throwExceptionInWrapup = false;
		MultiplyBean.throwExceptionInClone = false;
		MultiplyBean.throwExceptionInStatusSetter = false;
		MultiplyBean.clearAccumulatorInInitialize = false;
		MultiplyBean.setCountToNullInStep = false;
	}
	
	public void testClone() throws Exception {		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 4" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		JavaActor clone = (JavaActor)actor.clone();
		assertNotSame(bean, clone.getWrappedBean());
		
		clone.setInputValue("originalValue", 5);
		clone.step();
		assertEquals(20, clone.getOutputValue("multipliedValue"));
	}

	public void testClone_ExceptionInBeanClone() throws Exception {		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 4" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		MultiplyBean.throwExceptionInClone = true;
	
		Exception exception = null;
		try {
			actor.clone();
		} catch(CloneNotSupportedException e) {
			exception = e;
		}
		assertEquals("java.lang.CloneNotSupportedException: " +
					 "Exception cloning class org.restflow.actors.TestJavaActor$MultiplyBean " +
					 "wrapped by actor MultiplyActor", 
					 exception.toString());
	}

	public void testClone_UncloneableBean() throws Exception {		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		UncloneableMultiplyBean bean = new UncloneableMultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 4" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
	
		JavaActor clone = (JavaActor)actor.clone();
		assertNotSame(bean, clone.getWrappedBean());
		
		clone.setInputValue("originalValue", 5);
		clone.step();
		assertEquals(20, clone.getOutputValue("multipliedValue"));
	}

	public void testConfigure_BeanSet() throws Exception {

		JavaActor actor = new JavaActor();
		MultiplyBean bean = new MultiplyBean();
		
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
		
		actor.setWrappedBean(bean);
		assertNull(actor.getWrappedBeanClass());
		assertSame(bean, actor.getWrappedBean());
		
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		assertEquals(MultiplyBean.class, actor.getWrappedBeanClass());
		assertSame(bean, actor.getWrappedBean());
	}

	public void testConfigure_BeanClassNameSet() throws Exception {

		JavaActor actor = new JavaActor();
		
		actor.setBeanClassName("org.restflow.actors.TestJavaActor$MultiplyBean");
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
		
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		assertEquals(MultiplyBean.class, actor.getWrappedBeanClass());

		Object bean = actor.getWrappedBean();
		assertSame(MultiplyBean.class, bean.getClass());
	}

	
	public void testConfigure_BothBeanAndClassNameSet() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		actor.setWrappedBean(new MultiplyBean());
		actor.setBeanClassName("org.restflow.actors.TestJavaActor$MultiplyBean");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		Exception exception = null;
		try {
			actor.configure();
		} catch (ActorDeclarationException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "Actor MultiplyActor declared both a bean instance and a bean class.", 
					 exception.toString());
	}

	public void testConfigure_BeanClassNameSet_NotFound() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		
		actor.setBeanClassName("org.restflow.actors.TestJavaActor$MissingBean");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
		
		Exception exception = null;
		try {
			actor.configure();
		} catch(ActorDeclarationException e) {
			exception = e;
		}
		
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "Bean class org.restflow.actors.TestJavaActor$MissingBean " +
					 "not found for actor MultiplyActor", 
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.ClassNotFoundException: " +
				     "org.restflow.actors.TestJavaActor$MissingBean",
				     exception.getCause().toString());
		
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
	}

	public void testConfigure_NoBeanSet_NoClassNameSet() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
		
		Exception exception = null;
		try {
			actor.configure();
		} catch(ActorDeclarationException e) {
			exception = e;
		}
		
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "No bean class provided for actor MultiplyActor",
					 exception.toString());
		assertNull(exception.getCause());
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
	}

	public void testConfigure_ExceptionPrivateBeanConstructor() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
		
		actor.setBeanClassName("org.restflow.actors.TestJavaActor$BeanWithPrivateConstructor");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		Exception exception = null;
		try {
			actor.configure();
		} catch(WorkflowRuntimeException e) {
			exception = e;
		}
		
		assertEquals("org.restflow.exceptions.WorkflowRuntimeException: " +
					 "Error instantiating instance of bean class " +
					 "class org.restflow.actors.TestJavaActor$BeanWithPrivateConstructor " +
					 "for actor MultiplyActor",
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.IllegalAccessException: " +
					 "Class org.restflow.actors.JavaActor can not access a member of class " +
					 "org.restflow.actors.TestJavaActor$BeanWithPrivateConstructor " + 
					 "with modifiers \"private\"",
			     exception.getCause().toString());
		
		assertNull(actor.getWrappedBean());
	}
	
	public void testConfigure_ExceptionConstructingBean() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		
		assertNull(actor.getWrappedBean());
		assertNull(actor.getWrappedBeanClass());
		
		actor.setBeanClassName("org.restflow.actors.TestJavaActor$MultiplyBean");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		MultiplyBean.throwExceptionInConstructor = true;
		
		Exception exception = null;
		try {
			actor.configure();
		} catch(WorkflowRuntimeException e) {
			exception = e;
		}
		
		assertEquals("org.restflow.exceptions.WorkflowRuntimeException: " +
					 "Error instantiating instance of bean class " +
					 "class org.restflow.actors.TestJavaActor$MultiplyBean " +
					 "for actor MultiplyActor",
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.Exception: Exception in constructor",
			     exception.getCause().toString());
		
		assertNull(actor.getWrappedBean());
	}
	
	public void testConfigure_NoDefaultMethodsOnBean() throws Exception {

		JavaActor actor = new JavaActor();
		EmptyBean bean = new EmptyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		actor.step();
		actor.wrapup();
	}
	
	public void testConfigure_DefaultInitialize() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		assertFalse(bean.initialized);
		
		actor.initialize();
		assertTrue(bean.initialized);
		assertFalse(bean.customInitialization);
	}

	public void testConfigure_CustomInitialize() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setInitializeMethod("customInitialize");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		assertFalse(bean.initialized);
		
		actor.initialize();
		assertTrue(bean.initialized);
		assertTrue(bean.customInitialization);
	}

	public void testConfigure_CustomInitializeMissing() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setInitializeMethod("customInitializeMissing");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		
		Exception exception = null;
		try {
			actor.configure();
		} catch (ActorDeclarationException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "Error finding declared method customInitializeMissing " +
					 "on bean class org.restflow.actors.TestJavaActor$MultiplyBean " +
					 "for actor MultiplyActor",
					 exception.toString());
		Throwable cause = exception.getCause();
		assertNotNull(cause);
		assertEquals("java.lang.NoSuchMethodException: " +
					 "org.restflow.actors.TestJavaActor$MultiplyBean.customInitializeMissing()",
					 cause.toString());
	}
	
	public void testConfigure_CustomSetStatusMissing() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setStatusSetter("customSetStatusMissing");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		
		Exception exception = null;
		try {
			actor.configure();
		} catch (ActorDeclarationException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "Error finding declared method customSetStatusMissing " +
					 "on bean class org.restflow.actors.TestJavaActor$MultiplyBean " +
					 "for actor MultiplyActor",
					 exception.toString());
		Throwable cause = exception.getCause();
		assertNotNull(cause);
		assertEquals("java.lang.NoSuchMethodException: " +
					 "org.restflow.actors.TestJavaActor$MultiplyBean.customSetStatusMissing" +
					 "(org.restflow.actors.ActorStatus)",
					 cause.toString());
	}
	
	public void testConfigure_DefaultStep() throws Exception {

		JavaActor actor = new JavaActor();
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 2" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		actor.initialize();
		actor.setInputValue("originalValue", 1);

		assertFalse(bean.stepped);
		actor.step();
		assertTrue(bean.stepped);
		assertFalse(bean.customStep);
	}
	
	public void testConfigure_CustomStep() throws Exception {

		JavaActor actor = new JavaActor();
		MultiplyBean bean = new MultiplyBean();
		actor.setStepMethod("customStep");
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		actor.initialize();
		actor.setInputValue("originalValue", 1);

		assertFalse(bean.stepped);
		actor.step();
		assertTrue(bean.stepped);
		assertTrue(bean.customStep);
	}

	public void testConfigure_CustomStepMissing() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setStepMethod("customStepMissing");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		
		Exception exception = null;
		try {
			actor.configure();
		} catch (ActorDeclarationException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "Error finding declared method customStepMissing " +
					 "on bean class org.restflow.actors.TestJavaActor$MultiplyBean " +
					 "for actor MultiplyActor",
					 exception.toString());
		Throwable cause = exception.getCause();
		assertNotNull(cause);
		assertEquals("java.lang.NoSuchMethodException: " +
					 "org.restflow.actors.TestJavaActor$MultiplyBean.customStepMissing()",
					 cause.toString());
	}

	public void testConfigure_DefaultWrapup() throws Exception {

		JavaActor actor = new JavaActor();
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		actor.initialize();
		assertFalse(bean.wrappedUp);
		actor.wrapup();
		assertTrue(bean.wrappedUp);
		assertFalse(bean.customWrapup);
	}

	
	public void testConfigure_CustomWrapup() throws Exception {

		JavaActor actor = new JavaActor();
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setWrapupMethod("customWrapup");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		actor.initialize();
		assertFalse(bean.wrappedUp);
		actor.wrapup();
		assertTrue(bean.wrappedUp);
		assertTrue(bean.customWrapup);
	}

	public void testConfigure_CustomWrapupMissing() throws Exception {

		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setWrapupMethod("customWrapupMissing");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		
		Exception exception = null;
		try {
			actor.configure();
		} catch (ActorDeclarationException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.ActorDeclarationException: " +
					 "Error finding declared method customWrapupMissing " +
					 "on bean class org.restflow.actors.TestJavaActor$MultiplyBean " +
					 "for actor MultiplyActor",
					 exception.toString());
		Throwable cause = exception.getCause();
		assertNotNull(cause);
		assertEquals("java.lang.NoSuchMethodException: " +
					 "org.restflow.actors.TestJavaActor$MultiplyBean.customWrapupMissing()",
					 cause.toString());
	}
	
	public void testInitialize_DefaultSetStatus() throws Exception {
	
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		
		actor.configure();
		assertNull(bean.actorStatus);
		assertFalse(bean.statusSet);
		
		actor.initialize();
		assertNotNull(bean.actorStatus);
		assertTrue(bean.statusSet);
		assertFalse(bean.customSetStatus);
	}

	public void testInitialize_CustomSetStatus() throws Exception {
	
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);	
		actor.setStatusSetter("customSetStatus");
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		assertNull(bean.actorStatus);
		assertFalse(bean.statusSet);
		
		actor.initialize();
		assertNotNull(bean.actorStatus);
		assertTrue(bean.statusSet);
		assertTrue(bean.customSetStatus);
	}

	public void testInitialize_ExceptionInSetStatus() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();

		actor.configure();
		assertNull(bean.actorStatus);
		assertFalse(bean.statusSet);
		MultiplyBean.throwExceptionInStatusSetter = true;

		Exception exception = null;
		try {
			actor.initialize();
		} catch(RestFlowException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.WorkflowRuntimeException: " +
					 "Exception in setStatus() method of actor MultiplyActor", 
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.Exception: Exception in status setter", 
				     exception.getCause().toString());
	}
	
	public void testInitialize_setSettings() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		actor.setWrappedBean(new MultiplyBean());

		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));

		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		actor.step();
		assertEquals(12, actor.getOutputValue("multipliedValue"));
	}
	
	public void testInitialize_setStateVariablesInitialValues_NoNulls() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);

		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator: -1" + EOL +
				"count: 3" + EOL
		));		

		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		
		actor.initialize();
		assertEquals(-1, bean.getAccumulator());
		assertEquals(new Integer(3), bean.getCount());
	}
	
	public void testInitialize_setStateVariablesInitialValues_Nulls() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);

		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator:" + EOL +
				"count:" + EOL
		));		
		
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		
		actor.initialize();
		assertEquals(0, bean.getAccumulator());
		assertEquals(new Integer(0), bean.getCount());
	}
	
	public void testInitialize_getUpdatedStateVariables() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);

		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator: -1" + EOL +
				"count: 5" + EOL
		));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		
		MultiplyBean.clearAccumulatorInInitialize = true;
		
		actor.initialize();
		assertEquals(0, bean.getAccumulator());
		assertEquals(new Integer(5), bean.getCount());
	}
	
	public void testSetInputs() throws Exception {
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		actor.setWrappedBean(new MultiplyBean());
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
	}
	
	public void testSetInputValue_CorrectType() throws Exception {
		JavaActor actor = MultiplyBean.getConfiguredActor();
		actor.initialize();
		actor.setInputValue("originalValue", 1);
		actor.step();
		assertEquals(2, actor.getOutputValue("multipliedValue"));
	}

	public void testSetInputValue_IncorrectType() throws Exception {
		JavaActor actor = MultiplyBean.getConfiguredActor();
		actor.initialize();
		actor.setInputValue("originalValue", "hello");
		
		Exception exception = null;
		try {
			actor.step();
		} catch (InputTypeException ex) {
			exception = ex;
		}
		assertEquals("org.restflow.exceptions.InputTypeException: " +
				 	 "Data of incorrect type received on input 'originalValue' of actor MultiplyActor", 
				 	 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.IllegalArgumentException: " + 
					 "Cannot invoke org.restflow.actors.TestJavaActor$UncloneableMultiplyBean.setOriginalValue on " +
				     "bean class 'class org.restflow.actors.TestJavaActor$MultiplyBean' " +
				     "- argument type mismatch - had objects of type \"java.lang.String\" " +
				     "but expected signature \"java.lang.Integer\"", 
					 exception.getCause().toString());
	}	

	public void testSetInputValue_SetterException() throws Exception {
		JavaActor actor = MultiplyBean.getConfiguredActor();
		MultiplyBean.throwExceptionInSetter = true;
		actor.initialize();
		
		actor.setInputValue("originalValue", 1);
		Exception exception = null;
		try {
			actor.step();
		} catch (WorkflowRuntimeException ex) {
			exception = ex;
		}
		assertEquals("org.restflow.exceptions.WorkflowRuntimeException: " +
					 "Exception calling setter for input 'originalValue' on actor MultiplyActor", 
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.Exception: Exception in setter", exception.getCause().toString());
	}

	
	public void testSetInputValue_IncorrectType_Workflow() throws Exception {
		JavaActor actor = BadWorkflowProcessingBean.getConfiguredActor();
		actor.initialize();
		actor.setInputValue("workflow", "hello");
		
		Exception exception = null;
		try {
			actor.step();
		} catch (InputTypeException inputTypeException) {
			exception = inputTypeException;
		}
		assertEquals("org.restflow.exceptions.InputTypeException: " +
					 "Data of incorrect type received on input 'workflow' " +
					 "of actor BadWorkflowProcessingActor", 
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.IllegalArgumentException: " +
					 "Cannot invoke org.restflow.actors.TestJavaActor$BadWorkflowProcessingBean.setWorkflow on " +
					 "bean class 'class org.restflow.actors.TestJavaActor$BadWorkflowProcessingBean' " +
					 "- argument type mismatch - had objects of type \"java.lang.String\" " +
					 "but expected signature \"org.restflow.actors.Workflow\"", 
				     exception.getCause().toString());
	}	
	
	
	public void testSetInputValue_NewValueForSetting() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
	
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
	
		Exception exception = null;
		try {
			actor.setInputValue("factor", 4);
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.RestFlowException: " +
					 "Attempt to reassign value of setting 'factor' on actor MultiplyActor",
					 exception.toString());
	}

	public void testStep_Success() throws Exception {
		JavaActor actor = MultiplyBean.getConfiguredActor();
		actor.initialize();
		actor.setInputValue("originalValue", 1);
		actor.step();
		assertEquals(2, actor.getOutputValue("multipliedValue"));
	}	

	public void testStep_SetStatus() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		assertEquals(0, bean.getAccumulator());
		assertEquals((Integer)0, bean.getCount());
		bean.statusSet = false;
		bean.actorStatus = null;
		actor.step();
		assertNotNull(bean.actorStatus);
		assertTrue(bean.statusSet);
	}

	
	public void testStep_SetStatusException() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		assertEquals(0, bean.getAccumulator());
		assertEquals((Integer)0, bean.getCount());
		bean.statusSet = false;
		bean.actorStatus = null;
		MultiplyBean.throwExceptionInStatusSetter = true;
		
		Exception exception = null;
		try {
			actor.step();
		} catch(RestFlowException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.WorkflowRuntimeException: " +
					 "Exception in setStatus() method of actor MultiplyActor", 
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.Exception: Exception in status setter", 
				     exception.getCause().toString());
	}
	
	public void testStep_SetAndUpdateStateVariables() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
	
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator: 0" + EOL +
				"count: 0" + EOL
		));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		assertEquals(0, bean.getAccumulator());
		assertEquals((Integer)0, bean.getCount());
		actor.step();
		assertEquals(12, actor.getOutputValue("multipliedValue"));
		assertEquals((Integer)1, bean.getCount());
		assertEquals(12, bean.getAccumulator());
	
		actor.setInputValue("originalValue", 6);
		bean.setCount(17);
		actor.step();
		assertEquals(18, actor.getOutputValue("multipliedValue"));
		assertEquals((Integer)2, bean.getCount());
		assertEquals(30, bean.getAccumulator());
		
		bean.setAccumulator(0);
		actor.setInputValue("originalValue", 8);
		actor.step();
		assertEquals(24, actor.getOutputValue("multipliedValue"));
		assertEquals((Integer)3, bean.getCount());
		assertEquals(54, bean.getAccumulator());
	}

	public void testStep_SetAndUpdateStateVariablesWithNulls() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
	
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator: 0" + EOL +
				"count: 0" + EOL
		));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		assertEquals(0, bean.getAccumulator());
		assertEquals((Integer)0, bean.getCount());
		MultiplyBean.setCountToNullInStep = true;
		actor.step();
		assertEquals(12, actor.getOutputValue("multipliedValue"));
		assertNull(bean.getCount());
		assertEquals(12, bean.getAccumulator());
	
		actor.setInputValue("originalValue", 6);
		bean.setCount(17);
		actor.step();
		assertEquals(18, actor.getOutputValue("multipliedValue"));
		assertNull(bean.getCount());
		assertEquals(30, bean.getAccumulator());
	}

	public void testStep_SetInputValue_NonNullValue() throws Exception {
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		actor.setInputValue("originalValue", 72);
		actor.step();
		assertEquals(new Integer(72), bean.getOriginalValue());
	}	

	public void testStep_SetInputValue_NullValue_NotNullable() throws Exception {
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		Exception exception = null;
		try {
			actor.setInputValue("originalValue", null);
		} catch (NullInputException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.NullInputException: " +
					 "Null data received on non-nullable input 'originalValue' " +
					 "of actor <MultiplyActor>", 
					 exception.toString());
	}	

	public void testStep_SetInputValue_NullValue_Nullable() throws Exception {
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load(
				"originalValue:" + EOL +
				"  nullable: true" + EOL
		));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", null);
		try {actor.step();} catch(Exception e) {}
		assertNull(bean.getOriginalValue());
	}	

	
	public void testStep_StepException() throws Exception {
		JavaActor actor = MultiplyBean.getConfiguredActor();
		MultiplyBean.throwExceptionInStep = true;
		actor.initialize();
		actor.setInputValue("originalValue", 1);
		Exception exception = null;
		try {
			actor.step();
		} catch (RestFlowException ex) {
			exception = ex;
		}
		assertEquals("Exception in step() method of actor 'MultiplyActor'", exception.getMessage());
		assertNotNull(exception.getCause());
		assertEquals("Exception in step", exception.getCause().getMessage());
	}	
	
	public void testWrapup() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		assertFalse(bean.wrappedUp);
		actor.wrapup();
		assertTrue(bean.wrappedUp);
	}
	
	public void testWrapup_SetStatus() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		bean.statusSet = false;
		bean.actorStatus = null;
		assertFalse(bean.wrappedUp);
		actor.wrapup();
		assertTrue(bean.wrappedUp);
		assertNotNull(bean.actorStatus);
		assertTrue(bean.statusSet);
	}

	public void testWrapup_SetStatusException() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		bean.statusSet = false;
		bean.actorStatus = null;
		assertFalse(bean.wrappedUp);

		MultiplyBean.throwExceptionInStatusSetter = true;
		
		Exception exception = null;
		try {
			actor.wrapup();
		} catch(RestFlowException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("org.restflow.exceptions.WorkflowRuntimeException: " +
					 "Exception in setStatus() method of actor MultiplyActor", 
					 exception.toString());
		assertNotNull(exception.getCause());
		assertEquals("java.lang.Exception: Exception in status setter", 
				     exception.getCause().toString());
	}
	
	public void testWrapup_SetStateVariables() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
	
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator: 0" + EOL +
				"count: 0" + EOL
		));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		assertEquals(0, bean.getAccumulator());
		assertEquals((Integer)0, bean.getCount());
		actor.step();
		assertEquals(12, actor.getOutputValue("multipliedValue"));
		assertEquals((Integer)1, bean.getCount());
		assertEquals(12, bean.getAccumulator());
	
		bean.setCount(17);
		bean.setAccumulator(0);
		actor.wrapup();
		assertEquals((Integer)1, bean.getCount());
		assertEquals(12, bean.getAccumulator());
	}
	
	public void testWrapup_SetStateVariablesWithNulls() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName("MultiplyActor");
		MultiplyBean bean = new MultiplyBean();
		actor.setWrappedBean(bean);
	
		Yaml yaml = new Yaml();
		actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
		actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
		actor.setSettings((Map<String,Object>)yaml.load("factor: 3" + EOL));
		actor.setState((Map<String,Object>)yaml.load(
				"accumulator: 0" + EOL +
				"count: 0" + EOL
		));
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		actor.setInputValue("originalValue", 4);
		assertEquals(0, bean.getAccumulator());
		assertEquals((Integer)0, bean.getCount());
		MultiplyBean.setCountToNullInStep = true;
		actor.step();
		assertEquals(12, actor.getOutputValue("multipliedValue"));
		assertNull(bean.getCount());
		assertEquals(12, bean.getAccumulator());
	
		bean.setCount(17);
		bean.setAccumulator(0);
		actor.wrapup();
		assertNull(bean.getCount());
		assertEquals(12, bean.getAccumulator());
	}
	
	public static class UncloneableMultiplyBean {

		public static boolean throwExceptionInConstructor = false;
		public static boolean throwExceptionInInitialize = false;
		public static boolean throwExceptionInStep = false;
		public static boolean throwExceptionInWrapup = false;
		public static boolean throwExceptionInSetter = false;
		public static boolean throwExceptionInGetter = false;
		public static boolean throwExceptionInClone = false;
		public static boolean throwExceptionInStatusSetter = false;
		
		public static boolean clearAccumulatorInInitialize = false;
		public static boolean setCountToNullInStep = false;
		
		public boolean initialized = false;
		public boolean statusSet = false;
		public boolean stepped = false;
		public boolean wrappedUp = false;
		public boolean customInitialization = false;
		public boolean customSetStatus = false;
		public boolean customStep = false;
		public boolean customWrapup = false;
		
		public ActorStatus actorStatus = null;
		
		private int _factor;
		private Integer _originalValue;
		private int _multipliedValue;
		private int _accumulator;
		private Integer _count;
		
		public UncloneableMultiplyBean() throws Exception {
			if (throwExceptionInConstructor) {
				throw new Exception("Exception in constructor");
			}
			_count = 0;
		}
		
		public void initialize() throws Exception {
			if (throwExceptionInInitialize) {
				throw new Exception("Exception in setter");
			}
			if (clearAccumulatorInInitialize) {
				_accumulator = 0;
			}
			initialized = true;
		}
		
		public void customInitialize() {
			initialized = true;
			customInitialization = true;
		}
		
		public void setStatus(ActorStatus status) throws Exception {
			if (throwExceptionInStatusSetter) {
				throw new Exception("Exception in status setter");
			}			
			actorStatus = status;
			statusSet = true;
		}

		public void customSetStatus(ActorStatus status) {
			actorStatus = status;
			statusSet = true;
			customSetStatus = true;
		}
		
		public void setFactor(Integer value) throws Exception {
			_factor = value;
		}
		
		public void setOriginalValue(Integer value) throws Exception {
			if (throwExceptionInSetter) {
				throw new Exception("Exception in setter");
			}
			_originalValue = value;
		}
		
		public Integer getOriginalValue() {
			return _originalValue;
		}
		
		public void step() throws Exception {
			if (throwExceptionInStep) {
				throw new Exception("Exception in step");
			}
			_multipliedValue = _factor * _originalValue; 
			_accumulator += _multipliedValue;
			if (setCountToNullInStep) {
				_count = null;
			} else {
				_count++;
			}
			stepped = true;
		}
		
		public void customStep() throws Exception {
			stepped = true;
			customStep = true;
		}

		public void wrapup() throws Exception {
			if (throwExceptionInWrapup) {
				throw new Exception("Exception in wrapup");
			}
			wrappedUp = true;
		}

		public void customWrapup() throws Exception {
			wrappedUp = true;
			customWrapup = true;
		}
		
		public int getMultipliedValue() throws Exception {
			if (throwExceptionInGetter) {
				throw new Exception("Exception in getter");
			}
			return _multipliedValue;
		}
		
		public void setAccumulator(int value) {
			_accumulator = value;
		}
		
		public int getAccumulator() {
			return _accumulator;
		}
		
		public void setCount(Integer value) {
			_count = value;
		}
		
		public Integer getCount() {
			return _count;
		}
	}

	public static class MultiplyBean extends UncloneableMultiplyBean implements Cloneable {
		
		public MultiplyBean() throws Exception {
			super();
		}

		public Object clone()  throws CloneNotSupportedException {
			if (throwExceptionInClone) {
				throw new CloneNotSupportedException("Exception in constructor");
			}
			return super.clone();
		}
		
		public static JavaActor getConfiguredActor() throws Exception {
			JavaActor actor = new JavaActor();
			actor.setName("MultiplyActor");
			actor.setWrappedBean(new MultiplyBean());
			Yaml yaml = new Yaml();
			actor.setInputs((Map<String,Object>)yaml.load("originalValue:" + EOL));
			actor.setOutputs((Map<String,Object>)yaml.load("multipliedValue:" + EOL));
			actor.setSettings((Map<String,Object>)yaml.load("factor: 2" + EOL));
			actor.setApplicationContext(new WorkflowContextBuilder().build());
			actor.afterPropertiesSet();
			actor.elaborate();
			actor.configure();
			return actor;
		}
	}

	public static class EmptyBean {}
	
	
	
	public static class BeanWithPrivateConstructor {
		private BeanWithPrivateConstructor() {}
	}
	
	public static class BadWorkflowProcessingBean {
		
		@SuppressWarnings("unused")
		private Object _workflow;
		
		public void setWorkflow(Workflow workflow) {
			_workflow = (Workflow)workflow;        
	    }
	
		public void step() {}
		
		public static JavaActor getConfiguredActor() throws Exception {
			JavaActor actor = new JavaActor();
			actor.setName("BadWorkflowProcessingActor");
			actor.setWrappedBean(new BadWorkflowProcessingBean());
			Yaml yaml = new Yaml();
			actor.setInputs((Map<String,Object>)yaml.load("workflow:" + EOL));
			actor.setApplicationContext(new WorkflowContextBuilder().build());
			actor.afterPropertiesSet();
			actor.elaborate();
			actor.configure();
			return actor;
		}
	}
}
