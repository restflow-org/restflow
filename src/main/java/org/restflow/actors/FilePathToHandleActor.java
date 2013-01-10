package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;
import java.io.File;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restflow.actors.Actor.ActorFSM;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;


/**
 * This class is thread safe.  Its superclass is thread safe, and all the mutable fields 
 * it adds are synchronized on the instance.  
 * 
 * The template is a special input needed by the actor; the key to this input is defined
 * in the static definition below  All other inputs defined by the workflow enter the
 * model and are available to the template engine.
 */

@ThreadSafe()
public class FilePathToHandleActor extends AbstractActor {
	@GuardedBy("this") protected Log logger;

    
	public FilePathToHandleActor() {
		super();
		
		synchronized(this) {
			logger = LogFactory.getLog(getClass());
		}
	}
	
	@Override
	public synchronized Object clone() throws CloneNotSupportedException {
		FilePathToHandleActor theClone = (FilePathToHandleActor) super.clone();
		theClone.logger = LogFactory.getLog(getClass());
		return theClone;
	}
	
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}
	
	public void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}
	
	public synchronized void configure() throws Exception {
		super.configure();		
		_state = ActorFSM.CONFIGURED;
	}
	
	public synchronized void initialize() throws Exception {
		super.initialize();
		_state = ActorFSM.INITIALIZED;		
	}
	
	@Override
	public synchronized void step() throws Exception {
		
		super.step();

		for ( String key : _inputSignature.keySet() ) {
            String fileHandleKey = key+"File";
            String path =  (String)_inputValues.get(key);
            if ( path == null ) {
                File f = new File("" );
		        _outputValues.put( fileHandleKey, null);
            } else {
                File f = new File( path );
                if ( !f.exists()) throw new Exception ("file '"+path+"' does not exist");
		        _outputValues.put( fileHandleKey, f);
            }
		}
		
		
		_state = ActorFSM.STEPPED;
	}

	public synchronized void wrapup() throws Exception {	
		super.wrapup();
		_state = ActorFSM.WRAPPED_UP;
	}
	
	public synchronized void dispose() throws Exception {
		super.dispose();
		_state = ActorFSM.DISPOSED;
	}
	
}
