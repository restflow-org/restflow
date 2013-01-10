package org.restflow.actors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.beanutils.PropertyUtils;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.data.InputSignatureElement;
import org.restflow.exceptions.ActorDeclarationException;
import org.restflow.exceptions.ActorDefinitionException;
import org.restflow.exceptions.InputTypeException;
import org.restflow.exceptions.RestFlowException;
import org.restflow.exceptions.WorkflowRuntimeException;
import org.restflow.util.Contract;


/**
 * This class enables java beans to be used as actor implementations. This actor calls
 * setters and getters on the bean to provide input values and retrieve output
 * values before and after each step. It optionally calls methods for initializing 
 * and stepping the actor, and for wrapping up at the end of a run. The bean 
 * communicates changes in input and output readiness via an actor status object 
 * shared with the bean via an optional setter method.  
 * <br/><br/>

 * The names of the methods on the bean
 * implementing initialize, step, and wrapup are configurable.  Bean state is 
 * retrieved by this class following each step and restored prior to the next 
 * step so that new instances of the bean can continue computations started in
 * other instances.
 * <br/><br/> 
 * 
 * This class is thread safe.  Its superclass is thread safe, and all the mutable 
 * fields it adds are synchronized on the instance.  The clone method instantiates 
 * a new instance of the wrapped bean so that the bean does not have to be thread 
 * safe. The bean's clone method is used for this purpose if implemented; 
 * otherwise reflection is used to create a new instance.  Calls to setters for
 * the actor inputs and state, the call to the step method, and the calls to the
 * getters on the actor outputs and state variables all occur with single call 
 * to step on JavaActor, again so that the bean itself need not be thread safe.
 */
@ThreadSafe()
@SuppressWarnings("rawtypes")
public class JavaActor extends AbstractActor {

	///////////////////////////////////////////////////////////////////////////
	////                    private instance fields                        ////
	
	@GuardedBy("this") private Object _wrappedBean;
	@GuardedBy("this") private String _beanClassName;
	@GuardedBy("this") private Class<? extends Object> _wrappedBeanClass;
	@GuardedBy("this") private String _initializeMethodName;
	@GuardedBy("this") private String _statusSetterName;
	@GuardedBy("this") private String _stepMethodName;
	@GuardedBy("this") private String _wrapupMethodName;
	@GuardedBy("this") private String _disposeMethodName;
	@GuardedBy("this") private Method _cloneMethod;
	@GuardedBy("this") private Method _initializeMethod;
	@GuardedBy("this") private Method _statusSetterMethod;
	@GuardedBy("this") private Method _stepMethod;
	@GuardedBy("this") private Method _wrapupMethod;
	@GuardedBy("this") private Method _disposeMethod;
	
	private Map<String,Field> _fields;
	private boolean _fieldsAccessible;

	///////////////////////////////////////////////////////////////////////////
	////                      private enumerations                         ////

	private enum StateValueAssignmentMode {
		IncludeNullValues,
		SkipNullValues
	}
	
	///////////////////////////////////////////////////////////////////////////
	////                  private final class fields                       ////

	private static final String  DEFAULT_INITIALIZE_METHOD_NAME = "initialize";
	private static final String  DEFAULT_STATUS_SETTER_NAME 	= "setStatus";
	private static final String  DEFAULT_STEP_METHOD_NAME 		= "step";
	private static final String  DEFAULT_WRAPUP_METHOD_NAME 	= "wrapup";
	private static final String  DEFAULT_DISPOSE_METHOD_NAME 	= "dispose";
	
	private static final Class[] EMPTY_SIGNATURE 		 = new Class[]{};
	private static final Class[] STATUS_SETTER_SIGNATURE = new Class[]{ActorStatus.class};
	
	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public JavaActor() {
		super();
		
		synchronized(this) {
			_initializeMethodName = null;
			_statusSetterName = null;
			_stepMethodName = null;
			_wrapupMethodName = null;
			_disposeMethodName = null;
			_initializeMethod = null;
			_statusSetterMethod = null;
			_stepMethod = null;
			_wrapupMethod = null;
			_disposeMethod = null;
			_cloneMethod = null;
			_fieldsAccessible = false;
			
			// initialize contract state machine
			_state = ActorFSM.CONSTRUCTED;
		}
	}
	
	/**
	 * Clones the instance of JavaActor, and creates a new instance of the
	 * bean wrapped by the actor.  The bean's clone() method is used to create the new
	 * bean instance if the bean class is Cloneable; otherwise reflection is used to 
	 * create a new instance of the bean.  In the latter case, the initialization
	 * method on the bean is called so that bean is ready to step immediately.
	 * The shallow copy of JavaActor fields performed by the Object.clone() 
	 * suffices for all fields other than the reference to the wrapped bean.
	 * 
	 * @throws CloneNotSupportedException if there is an exception invoking the
	 *         clone method on the bean; or creating a new instance of the bean via 
	 *         reflection when there is not clone() method; or initializing a new 
	 *         (uncloned) instance of the bean.  The same type of exception must 
	 *         be thrown in all of these cases due to the declaration of Object.clone().
	 */
	public synchronized Object clone() throws CloneNotSupportedException {
		
		Contract.requires(_state == ActorFSM.INITIALIZED);
		
		// clone the instance of JavaActor, depending on the superclass
		// to perform a shallow copy of all instance fields other than _wrappedBean
		JavaActor theClone = (JavaActor) super.clone();
		
		// invoke the wrapped bean's clone method if it was detected in configure()
		if (_cloneMethod != null) {
			try {
				// reflection is used to invoke the clone method on the bean because the 
				// _wrappedBean field is of type Object, and Object.clone() is protected				
				theClone._wrappedBean = _cloneMethod.invoke(_wrappedBean);
			} catch (Exception cause) {
				throw new CloneNotSupportedException("Exception cloning " +
						_wrappedBean.getClass() + " wrapped by actor " + this);
			}
		
		// create and initialize a completely new instance of the bean if no clone method
		} else {
			
			try {
				// create a new instance of the wrapped bean via reflection
				theClone._wrappedBean = _wrappedBeanClass.newInstance();
			} catch (Exception e) {
				throw new CloneNotSupportedException("Exception creating a new instance of " +
						_wrappedBean.getClass() + " wrapped by ator " + this);
			}
			
			try {
				// initialize cloned bean
				theClone._initializedBean();
			} catch (Exception e) {
				throw new CloneNotSupportedException(
						"Exception initializing new instance of " + 
						_wrappedBean.getClass() + " wrapped by actor " + this + ". Initialize() threw: " +
						e.toString());
			}
		}
		
		// return the new instance
		return theClone;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	////                     actor configuration setters                   ////
	
	/**
	 * Provides the actor with a fully constructed instance of the wrapped java bean.
	 * 
	 * @param bean		The instance of the bean to be wrapped by this actor.
	 */
	public synchronized void setWrappedBean(Object bean) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_wrappedBean = bean;
	}
	
	/**
	 * Sets the class name of the bean to be wrapped by this actor. The bean
	 * will be instantiated by configure() method.
	 * 
	 * @param className	The fully qualified class name for the wrapped bean.
	 */
	public synchronized void setBeanClassName(String className) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_beanClassName = className;
	}

	/**
	 * Sets the name of the method called on the bean when this actor is initialized,
	 * overriding the default method name of 'initialize'.
	 * 
	 * @param name		The name of the initialize method.
	 */
	public synchronized void setInitializeMethod(String name) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_initializeMethodName = name;
	}

	/**
	 * Sets the name of the method called on the bean when this actor is stepped,
	 * overriding the default method name of 'step'.
	 * 
	 * @param name		The name of the step method.
	 */
	public synchronized void setStepMethod(String name) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_stepMethodName = name;
	}

	/**
	 * Sets the name of the setter on the bean for providing access
	 * to the actor status, overriding the default method name of 'setStatus'.
	 * 
	 * @param name		The name of the initialize method.
	 */
	public synchronized void setStatusSetter(String name) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_statusSetterName = name;
	}
	
	/**
	 * Sets the name of the method called on the bean when this actor wraps up,
	 * overriding the default method name of 'wrapup'.
	 * 
	 * @param name		The name of the wrapup method.
	 */
	public synchronized void setWrapupMethod(String name) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_wrapupMethodName = name;
	}

	/**
	 * Sets the name of the method called on the bean when this actor is diposed,
	 * overriding the default method name of 'dispose'.
	 * 
	 * @param name		The name of the wrapup method.
	 */
	public synchronized void setDisposeMethod(String name) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_disposeMethodName = name;
	}

	
	///////////////////////////////////////////////////////////////////////////
	////                    actor lifecycle methods                        ////
	
	/**
	 * Indicates to the class that all configuration setters have been called
	 * and that configure() can now be invoked legally.
	 */
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_state = ActorFSM.PROPERTIES_SET;
	}
	
	public void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}
	
	/**
	 * Configures this actor using the actor configuration settings.
	 * Instantiates a wrapped bean if an instance was not provided, 
	 * and uses reflection to detect the initialize, statusSet, step,
	 * and wrapup methods on the bean for use when the workflow is run.
	 * 
	 * @throws ActorDeclarationException if the bean or the bean class
	 *         is declared incorrectly, or if a <b>declared</b> initialize,
	 *         setStatus, step, or wrapup method is missing.
	 * @throws WorkflowRuntimeException if an exception occurs while 
	 *         creating an instance of the bean.
	 * @throws Exception if super.configure() throws it.
	 */
	public synchronized void configure() throws Exception {
		
		Contract.requires(_state == ActorFSM.ELABORATED);

		// configure the superclass
		super.configure();
		
		// create an instance of the wrapped bean if not yet assigned
		if (_wrappedBean == null) {
			_wrappedBean = _instantiateBean();
		
		// otherwise save the class of the bean for creating new instances later
		} else {
			
			if (_beanClassName != null) {
				throw new ActorDeclarationException(
						"Actor " + this + " declared both a bean instance " +
						"and a bean class.");
			}
			
			_wrappedBeanClass = _wrappedBean.getClass();
		}
			
		// get each of the actor methods on the bean if they exist
		
		_initializeMethod = _getActorMethodOnBean(
				_initializeMethodName, 
				DEFAULT_INITIALIZE_METHOD_NAME, 
				EMPTY_SIGNATURE);
		
		_statusSetterMethod = _getActorMethodOnBean(
				_statusSetterName, 
				DEFAULT_STATUS_SETTER_NAME, 
				STATUS_SETTER_SIGNATURE);
		
		_stepMethod = _getActorMethodOnBean(
				_stepMethodName,
				DEFAULT_STEP_METHOD_NAME, 
				EMPTY_SIGNATURE);
		
		_wrapupMethod = _getActorMethodOnBean(
				_wrapupMethodName, 
				DEFAULT_WRAPUP_METHOD_NAME, 
				EMPTY_SIGNATURE);

		_disposeMethod = _getActorMethodOnBean(
				_disposeMethodName, 
				DEFAULT_DISPOSE_METHOD_NAME, 
				EMPTY_SIGNATURE);

		
		_fields = new Hashtable<String,Field>();
		
		for (String inputName : _inputSignature.keySet()) {
			_cachePublicField(inputName);
		}
		
		for (String outputName : _outputSignature.keySet()) {
			_cachePublicField(outputName);
		}
		
		for (String stateVariableName : _stateVariables.keySet()) {
			_cachePublicField(stateVariableName);
		}

		_fieldsAccessible = _fields.size() > 0;

		
		// detect the clone method on the bean if it is cloneable
		if (_wrappedBean instanceof Cloneable) {
			try {
				_cloneMethod = _wrappedBeanClass.getMethod("clone", EMPTY_SIGNATURE);
			} catch(NoSuchMethodException cause) {
				throw new ActorDefinitionException(
						"Error finding clone() method" +
						" on bean " + _wrappedBean.getClass() + " for actor " + this, 
						cause);
			}
		}
		
		_state = ActorFSM.CONFIGURED;
	}

	private void _cachePublicField(String name) {
		Field field = null;
		try { 
			field = _wrappedBean.getClass().getField(name);
			field.setAccessible(true);
			_fields.put(name, field);
		} catch (Exception e) {
		}		
	}
	
	/**
	 * Initializes this actor, making it ready to step for the first time.
	 * This method assigns values to properties on the bean declared as settings 
	 * (constants) and initializes state variables for which an initial value was provided
	 * in the actor declaration. It then calls the initialization method on the bean
	 * and retrieves any updated values of state variables.  The actor status is 
	 * provided to actor prior to the call to initialize so that the bean can
	 * initialize actor input enable flags.
	 * 
	 * @throws InputTypeException if the the type of a configured value for
	 *         a setting or a state variable is incompatible with the type
	 *         of argument on the corresponding setter on the bean.
	 * @throws WorkflowRuntimeException if an exception occurs within a setter
	 *         for an actor setting or initial state value, or if an exception
	 *         is thrown with the bean's initialization method.
	 * @throws Exception if super.initialize() throws it.
	 */
	public synchronized void initialize() throws Exception {
		
		Contract.requires(_state == ActorFSM.CONFIGURED || _state == ActorFSM.WRAPPED_UP);
		
		// initialize the superclass
		super.initialize();

		// initialize the wrapped bean
		_initializedBean();
		
		_state = ActorFSM.INITIALIZED;
	}
		
	
	/**
	 * Steps the actor by calling the step method on the bean.  Prior to calling
	 * step on the bean, this method copies the latest values of all state variables
	 * and actor input values to corresponding bean properties.
	 * It also provides to the bean the actor status object if a setter exists. 
	 * After calling step() on the bean, this method retrieves and stores
	 * the values of actor outputs and updated state variables.
	 * 
	 * @throws InputTypeException if the type of data provided as actor input
	 * 		   is incompatible with the argument on the corresponding setter on the bean.
	 * @throws WorkflowRuntimeException if an exception occurs within a setter
	 *         for an actor state or input value, within a getter for an actor state or output
	 *         value, or within the step method on the bean.
	 * @throws Exception if super.step() throws it.
	 */
	public synchronized void step() throws Exception {
		
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);

		super.step();
		
		// copy latest state variable values to bean properties
		_setStateValuesOnBean(StateValueAssignmentMode.IncludeNullValues);
		
		// copy staged input values to bean properties
		_setInputValuesOnBean();

		// set the status if setter is defined
		if (_statusSetterMethod != null) {
			_setActorStatusOnBean();
		}
		
		// invoke the bean step method if defined
		// TODO write test showing actor goes to STEPPED state even if exception is thrown.
		try {			
			if (_stepMethod != null) {
				_callMethodOnBean(_stepMethod);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			_state = ActorFSM.STEPPED;
		}
		
		// retrieve and store actor outputs
		_getOutputValuesFromBean();
		
		// update stored values for state variables
		_getStateValuesFromBean();
	}

	/**
	 * Calls the wrapup method on the bean following the last call to step(). 
	 * Prior to calling wrapup on the bean, this method copies the latest values of 
	 * all state variables to corresponding bean properties, and provides to the bean 
	 * the actor status object if a setter exists.
	 * @throws Exception 
	 */
	public synchronized void wrapup() throws Exception {

		super.wrapup();
		
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		
		// set the status if setter is defined
		if (_statusSetterMethod != null) {
			_setActorStatusOnBean();
		}
		
		// copy final state variable values to bean properties
		_setStateValuesOnBean(StateValueAssignmentMode.IncludeNullValues);

		// call bean wrapup method if defined
		if (_wrapupMethod != null) {
			_callMethodOnBean(_wrapupMethod);
		}
		
		_state = ActorFSM.WRAPPED_UP;
	}

	public synchronized void dispose() throws Exception {

		Contract.requires(_state == ActorFSM.WRAPPED_UP);
		
		super.dispose();
		
		// copy final state variable values to bean properties
		_setStateValuesOnBean(StateValueAssignmentMode.IncludeNullValues);

		// call bean wrapup method if defined
		if (_disposeMethod != null) {
			_callMethodOnBean(_disposeMethod);
		}
		
		_state = ActorFSM.DISPOSED;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	////      package-protected field accessors for facilitating tests     ////
	
	Object getWrappedBean() {
		return _wrappedBean;
	}

	Class getWrappedBeanClass() {
		return _wrappedBeanClass;
	}

	
	///////////////////////////////////////////////////////////////////////////
	////                   private helper methods                          ////
	
	/** Uses reflection to call the passed method on the bean */
	private void _callMethodOnBean(Method method) throws WorkflowRuntimeException {
		
		Contract.disallows(_state == ActorFSM.PROPERTIES_SET);
		Contract.disallows(_state == ActorFSM.DISPOSED);
		
		try {
			method.invoke(_wrappedBean);
			
		} catch (Exception reflectionException) {
			throw new WorkflowRuntimeException(
					"Exception in " + method.getName() + "() method of actor '" + this + "'",
					 reflectionException.getCause()
			);
		}
	}
	
	/** Uses reflection to look up the method named by first parameter (the custom method name), 
	 * or by the second parameter (the default method name) if the first is null */
	private Method _getActorMethodOnBean(String customMethodName, 
			String defaultMethodName, Class[] signature) throws ActorDeclarationException {

		Contract.requires(_state == ActorFSM.ELABORATED);
		
		Method method;

		if (customMethodName != null) {
			
			// look for the method named by the first parameter and throw an exception if
			// the custom method is not defined on the bean
			try {
				method = _wrappedBeanClass.getMethod(customMethodName, signature);
			} catch(NoSuchMethodException cause) {
				throw new ActorDeclarationException(
					"Error finding declared method " + customMethodName +
					" on bean " + _wrappedBean.getClass() + " for actor " + this, 
					cause);
			}
			
		} else {
			
			// look for the method named by the second parameter, but do not throw an exception
			// if the default method is not defined on the bean
			try {
				method = _wrappedBeanClass.getMethod(defaultMethodName, signature);
			} catch(NoSuchMethodException e) {
				method = null;
			}
		}
		
		if (method != null) {
			method.setAccessible(true);
		}
		
		return method;
	}
	
	public Object getFieldValue(String name) throws Exception {
		Field field = _wrappedBean.getClass().getField(name);
		field.setAccessible(true);
		return field.get(_wrappedBean);
	}
	
	public void setFieldValue(String name, Object value) throws Exception {
		Field field = _wrappedBean.getClass().getField(name);
		field.setAccessible(true);
		field.set(_wrappedBean,value);
	}

	
	/** Uses reflection to get the value of the named property on the bean */
	private Object _getBeanProperty(String name) throws WorkflowRuntimeException {
	
		Contract.disallows(_state == ActorFSM.PROPERTIES_SET);
		Contract.disallows(_state == ActorFSM.DISPOSED);

		Object value = null;

		Field field = null;
		if (_fieldsAccessible && (field = _fields.get(name)) != null) {
			try {
				value = field.get(_wrappedBean);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			try {
				value = PropertyUtils.getNestedProperty(_wrappedBean, name);
				
			} catch (Exception reflectionException) {
				throw new WorkflowRuntimeException(
						"Exception calling getter for output '" + name + "' on actor " + this,
						reflectionException.getCause()
				);
			}
		}
		
		return value;
	}

	/** Uses reflection to retrieve from the bean the latest values of the 
	 * actor outputs and store them in the superclass */
	private void _getOutputValuesFromBean() throws Exception {
		
		Contract.requires(_state == ActorFSM.STEPPED);
		
		for (String name: _outputSignature.keySet()) {
			Object value = _getBeanProperty(name);
			_storeOutputValue(name, value);
		}
	}

	/** Uses reflection to retrieve from the bean the latest values of the 
	 * actor state variables and store them in the superclass */
	private void _getStateValuesFromBean() throws RestFlowException {
		
		Contract.disallows(_state == ActorFSM.PROPERTIES_SET);
		Contract.disallows(_state == ActorFSM.DISPOSED);

		for (Map.Entry<String,Object> entry : _stateVariables.entrySet()) {
			String name = entry.getKey();
			Object value = _getBeanProperty(name);
			_stateVariables.put(name, value);
		}
	}

	/** Initializes the wrapped bean */
	private void _initializedBean() throws RestFlowException {
		
		// copy any constants to bean properties
		_setConstantValuesOnBean();

		// call bean initialize method if defined
		if (_initializeMethod != null) {

			// copy non-null state variable initialization values to bean properties
			_setStateValuesOnBean(StateValueAssignmentMode.SkipNullValues);

			// set the status if a setter for it is defined
			if (_statusSetterMethod != null) {
				_setActorStatusOnBean();
			}
			
			// invoke the initialize method
			_callMethodOnBean(_initializeMethod);
			
			// update stored values for state variables
			_getStateValuesFromBean();
		}
	}

	
	/** Instantiates a wrapped bean using its class name */
	private Object _instantiateBean() throws ActorDeclarationException, WorkflowRuntimeException {
		
		Contract.requires(_state == ActorFSM.ELABORATED);
		
		Object bean;
		
		if (_beanClassName == null) {
			throw new ActorDeclarationException("No bean class provided for actor " + this);
		}
	
		try {
			_wrappedBeanClass = Class.forName(_beanClassName);
		} catch (ClassNotFoundException cause) {
			throw new ActorDeclarationException("Bean class " + _beanClassName +
											    " not found for actor " + this, cause);
		}
	
		try {
			bean = _wrappedBeanClass.newInstance();
		} catch (Exception cause) {
			throw new WorkflowRuntimeException(
					"Error instantiating instance of bean class " + 
					_wrappedBeanClass + " for actor " + this, cause);
		}
		
		return bean;
	}

	/** Uses reflection to set the actor status property on the bean */
	private void _setActorStatusOnBean() throws WorkflowRuntimeException {

		Contract.disallows(_state == ActorFSM.PROPERTIES_SET);
		Contract.disallows(_state == ActorFSM.DISPOSED);

		try {
			
			// call the method on the bean via reflection
			_statusSetterMethod.invoke(_wrappedBean, _actorStatus);
			
		} catch (Exception reflectionException) {
			throw new WorkflowRuntimeException(
					"Exception in " + _statusSetterMethod.getName() + "() method of actor " + this,
					 reflectionException.getCause()
			);
		}
	}

	/** Uses reflection to set the named property on the bean to the provided value */
	private void _setBeanProperty(String name, Object value) throws WorkflowRuntimeException {
		
		Contract.disallows(_state == ActorFSM.PROPERTIES_SET);
		Contract.disallows(_state == ActorFSM.DISPOSED);

		Field field = null;
		if (_fieldsAccessible && (field = _fields.get(name)) != null) {
			try {
				field.set(_wrappedBean, value);
			} catch (Exception e) {
				throw new WorkflowRuntimeException("Exception setting property '" + name + "' on actor '" + this + "'", e);
			}
		} else {
			
			try {
				PropertyUtils.setNestedProperty(_wrappedBean, name, value);
				
			} catch (IllegalArgumentException cause) {
				throw new InputTypeException(
						"Data of incorrect type received on input '" + name + "' of actor " + this, 
						cause);
			} catch (Exception reflectionException) {
				throw new WorkflowRuntimeException(
						"Exception calling setter for input '" + name + "' on actor " + this,
						reflectionException.getCause()
				);
			}
		}
	}

	/** Assigns values to the actor settings on the bean */
	private void _setConstantValuesOnBean() throws WorkflowRuntimeException {

		Contract.requires(_state == ActorFSM.CONFIGURED || _state == ActorFSM.INITIALIZED || _state == ActorFSM.WRAPPED_UP);
		
		for (Map.Entry<String,Object> entry : _constants.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			_setBeanProperty(name, value);
		}
	}
	
	/** Assigns values to the actor inputs on the bean */
	private void _setInputValuesOnBean() throws WorkflowRuntimeException {
		
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);

		// copy staged input values for each input signature element to bean properties
		for (Map.Entry<String,InputSignatureElement> signatureEntry : _inputSignature.entrySet()) {
			
			// get the label and latest value for the next signature element
			InputSignatureElement element = signatureEntry.getValue();
			String name = (String) signatureEntry.getKey();
			Object value = _inputValues.get(name);
			
			// set the value on the bean if it is assigned or nullable
			if (value != null || element.isNullable()) {
				_setBeanProperty(name, value);
			}
		}
	}

	/** Assigns values to the actor state variables on the bean, optionally skipping null values. */
	private void _setStateValuesOnBean(StateValueAssignmentMode mode) throws WorkflowRuntimeException {
		
		Contract.disallows(_state == ActorFSM.PROPERTIES_SET);
		Contract.disallows(_state == ActorFSM.DISPOSED);
		
		for (Map.Entry<String,Object> entry : _stateVariables.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			if (value != null || mode != StateValueAssignmentMode.SkipNullValues) {
				_setBeanProperty(name, value);
			}
		}
	}

}