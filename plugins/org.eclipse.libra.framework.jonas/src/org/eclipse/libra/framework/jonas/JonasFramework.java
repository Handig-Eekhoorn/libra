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
package org.eclipse.libra.framework.jonas;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.libra.framework.core.FrameworkDelegate;
import org.eclipse.libra.framework.core.IOSGIFrameworkWorkingCopy;
import org.eclipse.wst.server.core.IRuntimeType;


public class JonasFramework extends FrameworkDelegate implements
		IOSGIFrameworkWorkingCopy, IJonasFramework {
	public JonasFramework() {
	}
	

	public IJonasVersionHandler getVersionHandler() {
		IRuntimeType type = getRuntime().getRuntimeType();
		return JonasPlugin.getJonasVersionHandler(type.getId());
	}

	@SuppressWarnings("rawtypes")
	public List getFrameworkClasspath(IPath configPath) {
		IPath installPath = getRuntime().getLocation();
		// If installPath is relative, convert to canonical path and hope for
		// the best
		if (!installPath.isAbsolute()) {
			try {
				String installLoc = (new File(installPath.toOSString()))
						.getCanonicalPath();
				installPath = new Path(installLoc);
			} catch (IOException e) {
				// Ignore if there is a problem
			}
		}
		return getVersionHandler().getFrameworkClasspath(installPath, configPath);
	}

	/**
	 * Verifies the Felix installation directory. If it is correct, true is
	 * returned. Otherwise, the user is notified and false is returned.
	 * 
	 * @return boolean
	 */
	@Override
	public IStatus verifyLocation() {
		return getVersionHandler()
				.verifyInstallPath(getRuntime().getLocation());
	}
	

	@Override
	public IStatus validate() {
		IStatus status = super.validate();
		if (!status.isOK())
			return status;
	
		status = verifyLocation();
		if (!status.isOK())
			return status;
		// return new Status(IStatus.ERROR, FelixPlugin.PLUGIN_ID, 0,
		// Messages.errorInstallDir, null);
		// don't accept trailing space since that can cause startup problems
		if (getRuntime().getLocation().hasTrailingSeparator())
			return new Status(IStatus.ERROR, JonasPlugin.PLUGIN_ID, 0,
					Messages.errorInstallDirTrailingSlash, null);
		if (getVMInstall() == null)
			return new Status(IStatus.ERROR, JonasPlugin.PLUGIN_ID, 0,
					Messages.errorJRE, null);
	
	
		File f = getRuntime().getLocation().append("conf").toFile();
		File[] conf = f.listFiles();
		if (conf != null) {
			int size = conf.length;
			for (int i = 0; i < size; i++) {
				if (!f.canRead())
					return new Status(IStatus.WARNING, JonasPlugin.PLUGIN_ID,
							0, Messages.warningCantReadConfig, null);
			}
		}
		
		return Status.OK_STATUS;
	}

}
