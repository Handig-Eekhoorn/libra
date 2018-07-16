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

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.libra.framework.core.FrameworkCorePlugin;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.libra.framework.core.OSGIFrameworkInstanceBehaviorDelegate;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.jonas.internal.JonasFrameworkInstanceBehavior;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;

public class JonasFrameworkInstance extends FrameworkInstanceDelegate implements
		IJonasFrameworkInstance {

	protected transient IJonasVersionHandler versionHandler;

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		IStatus status = super.canModifyModules(add, remove);
		if (!status.isOK())
			return status;

		if (getJonasVersionHandler() == null)
			return new Status(IStatus.ERROR, JonasPlugin.PLUGIN_ID, 0,
					Messages.errorNoRuntime, null);

		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];
				IStatus status2 = getJonasVersionHandler().canAddModule(module);
				if (status2 != null && !status2.isOK())
					return status2;
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		try {
			getJonasConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Can't setup for Felix configuration.", e);
		}
	}

	@Override
	public void importRuntimeConfiguration(IRuntime runtime,
			IProgressMonitor monitor) throws CoreException {

		super.importRuntimeConfiguration(runtime, monitor);
		OSGIFrameworkInstanceBehaviorDelegate fsb = (OSGIFrameworkInstanceBehaviorDelegate) getServer()
				.loadAdapter(JonasFrameworkInstanceBehavior.class, null);
		if (fsb != null) {
			IPath tempDir = fsb.getTempDirectory();
			if (!tempDir.isAbsolute()) {
				IPath rootPath = ResourcesPlugin.getWorkspace().getRoot()
						.getLocation();
				tempDir = rootPath.append(tempDir);
			}
			setInstanceDirectory(tempDir.toPortableString());
		}

		try {
			getJonasConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Can't setup for Felix configuration.", e);
		}
	}

	public JonasFramework getJonasRuntime() {
		if (getServer().getRuntime() == null)
			return null;
		return (JonasFramework) getServer().getRuntime().loadAdapter(
				JonasFramework.class, null);
	}

	public IJonasVersionHandler getJonasVersionHandler() {
		if (versionHandler == null) {
			if (getServer().getRuntime() == null || getJonasRuntime() == null)
				return null;

			versionHandler = getJonasRuntime().getVersionHandler();
		}
		return versionHandler;
	}

	public FrameworkInstanceConfiguration getJonasConfiguration()
			throws CoreException {

		return getFrameworkInstanceConfiguration();

	}

	private static ITargetLocation[] getDefaultBundleContainers(IPath installPath) {
		try {
			DirectoryScanner scanner = new DirectoryScanner();
			String baseDir = installPath.append("repositories/maven2-internal").toOSString();
			scanner.setBasedir(baseDir);
			scanner.setIncludes(new String[]{"**/*.jar"});
			scanner.scan();
			String[] bundles = scanner.getIncludedFiles();
			ITargetPlatformService service = FrameworkCorePlugin.getTargetPlatformService();

			if(bundles != null && bundles.length>0){
				
				ITargetLocation[] containers = new ITargetLocation[bundles.length];
				int i=0;	
				for(String b: bundles){
					File bundle = new File(b);
					IPath baseDirFile = new Path(baseDir);
					
					containers[i] = service.newDirectoryLocation(baseDirFile.append(bundle.getParent()).toOSString());
					i++;
				}
				return containers;
			}
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}
		return new ITargetLocation[0];
	}

	@Override
	public ITargetDefinition createDefaultTarget() throws CoreException {
		IPath installPath = getServer().getRuntime().getLocation();
		ITargetPlatformService service = FrameworkCorePlugin.getTargetPlatformService();

		ITargetDefinition targetDefinition = service.newTarget();
		targetDefinition.setName(getServer().getName());
		ITargetLocation[] containers = getDefaultBundleContainers(installPath);

		targetDefinition.setTargetLocations(containers);
		service.saveTargetDefinition(targetDefinition);

		return targetDefinition;
	}

	

}
