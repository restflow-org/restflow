package org.restflow.beans;

import org.springframework.beans.factory.InitializingBean;

import org.restflow.modelgrep.ModelGrep;

//proxy until all of the workflows can be changed
public class TextScanner extends ModelGrep implements InitializingBean {
	
	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
