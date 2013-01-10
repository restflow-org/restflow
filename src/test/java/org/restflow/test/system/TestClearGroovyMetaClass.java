package org.restflow.test.system;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import junit.framework.TestCase;

public class TestClearGroovyMetaClass extends TestCase {
	

	//Add methods to this class within the groovy script.
	public class X {}

	
	//This test demonstrates how to clear the metaclass in a groovy script.  Without the removeMetaClass line
	//in the first test, the second test will fail when run back-to-back.
	
	public void test_PolluteAndCleanMetaClass() throws Exception {

		
		Binding binding = new Binding();
		binding.setVariable("x", new X() );
		
		String script = "org.restflow.test.system.TestClearGroovyMetaClass.X.metaClass.test << { println x }; x.test()";
		
		final GroovyShell sh = new GroovyShell( binding );
		Script compiledScript = sh.parse( script );
		compiledScript.run();
	
		//remove our 'permanent' (ThreadLocal?) mark on the metaClassRegistry...
		GroovySystem.getMetaClassRegistry().removeMetaClass( X.class );
		
	}
	
	public void test_UseCleanMetaClass() throws Exception {
		Binding binding = new Binding();
		binding.setVariable("x", new X() );
		
		String script = "println x;  x.test()";
		
		final GroovyShell sh = new GroovyShell( binding );
		Script compiledScript = sh.parse( script );

		try {
			compiledScript.run();
			fail( "x.test should not be defined.  The metaclassRegistry is not empty.");
		} catch (MissingMethodException e) {
			e.printStackTrace();
		}
	}


}
