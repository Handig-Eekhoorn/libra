/*******************************************************************************
 *   Copyright (c) 2010 Eteration A.S. and others.
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *      Naci Dai and Murat Yener, Eteration A.S. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.framework.equinox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;



public interface IEquinoxFrameworkInstance  extends IOSGIFrameworkInstance {
	public static final String PROPERTY_JAVA_PROFILE = "JAVA_PROFILE";

	
	public FrameworkInstanceConfiguration getEquinoxConfiguration() throws CoreException;
	
	public String getJavaPofile();
	public void setJavaProfile(String id);

}
