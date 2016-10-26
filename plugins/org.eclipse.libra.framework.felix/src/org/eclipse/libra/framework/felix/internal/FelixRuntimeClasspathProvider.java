package org.eclipse.libra.framework.felix.internal;

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;

public class FelixRuntimeClasspathProvider extends
		RuntimeClasspathProviderDelegate {
	/**
	 * @see RuntimeClasspathProviderDelegate#resolveClasspathContainer(IProject,
	 *      IRuntime)
	 */
	@Override
	public IClasspathEntry[] resolveClasspathContainer(IProject project,
			IRuntime runtime) {
		IPath installPath = runtime.getLocation();

		if (installPath == null)
			return new IClasspathEntry[0];

		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		// String runtimeId = runtime.getRuntimeType().getId();
		IPath path = installPath.append("osgi");
		addLibraryEntries(list, path.toFile(), true);

		return list
				.toArray(new IClasspathEntry[list.size()]);
	}
}
