/*******************************************************************************
 *    Copyright (c) 2010 Eteration A.S. and others.
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    which accompanies this distribution, and is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 *     Contributors:
 *        IBM Corporation - initial API and implementation
 *           - This code is based on WTP SDK frameworks and Tomcat Server Adapters
 *           org.eclipse.jst.server.core
 *           org.eclipse.jst.server.ui
 *           
 *       Naci Dai and Murat Yener, Eteration A.S. 
 *******************************************************************************/
package org.eclipse.libra.framework.core.publish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.wst.server.core.IModule;

@SuppressWarnings("restriction")
public abstract class PublishHelper {

	protected abstract IPath getPublishFolder();


	public String getServerModules(List<IModule[]> modules, String prefix, String spacer) {
		StringBuilder builder = new StringBuilder();
		for (IModule[] moduleArr: modules) {
			for (IModule module : moduleArr) {
				if (builder.length() > 0)
					builder.append(spacer); //$NON-NLS-1$
				IPluginModelBase[] models = PluginRegistry.getActiveModels();
				IPath publishFolder = getPublishFolder();
				for (IPluginModelBase iPluginModelBase : models) {
					if (iPluginModelBase.getBundleDescription().getName()
							.equalsIgnoreCase(module.getName())) {
						if (builder.indexOf(iPluginModelBase.getPluginBase()
								.getId()
								+ "_"
								+ iPluginModelBase.getPluginBase().getVersion()
								+ ".jar") < 0) {
							builder.append(prefix
									+ publishFolder.append(
											iPluginModelBase.getPluginBase()
													.getId()
													+ "_"
													+ iPluginModelBase
															.getPluginBase()
															.getVersion()
													+ ".jar").toOSString());
							break;
						}
					}
				}
			}
		}
		return builder.toString();
	}

	public String getTargetBundles(FrameworkInstanceConfiguration config,
			String prefix, String spacer) {
		StringBuffer buffer = new StringBuffer();
		String[] paths = getTargetBundlePaths(config);
		for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
			if (buffer.length() > 0)
				buffer.append(spacer); //$NON-NLS-1$
			buffer.append(prefix + path);
		}
		return buffer.toString();
	}



	public String[] getTargetBundlePaths(FrameworkInstanceConfiguration config) {
		List<String> all = new ArrayList<String>();
		ITargetDefinition targetDefinition = config.getTargetDefinition();
		targetDefinition.resolve(new NullProgressMonitor());
		TargetBundle[] targetBundles = targetDefinition.getBundles();
		for (TargetBundle b : targetBundles) {
			if (b.getStatus().getSeverity() == IStatus.OK) {
				all.add(b.getBundleInfo().getLocation().getRawPath());
			}

		}
		return all.toArray(new String[all.size()]);
	}
	
	public TargetBundle[] getTargetBundles(FrameworkInstanceConfiguration config) {
		ITargetDefinition targetDefinition = config.getTargetDefinition();
		targetDefinition.resolve(new NullProgressMonitor());
		TargetBundle[] targetBundles = targetDefinition.getBundles();
		return targetBundles;
	}


	public String[] getTargetBundleIds(FrameworkInstanceConfiguration config) {
		List<String> all = new ArrayList<String>();
		ITargetDefinition targetDefinition = config.getTargetDefinition();
		targetDefinition.resolve(new NullProgressMonitor());
		TargetBundle[] targetBundles = targetDefinition.getBundles();
		for (TargetBundle b : targetBundles) {
			if (b.getStatus().getSeverity() == IStatus.OK) {
				all.add(b.getBundleInfo().getSymbolicName());
			}

		}
		return all.toArray(new String[all.size()]);
	}


	public void exportBundles(List<IModule[]> modules,
			FrameworkInstanceConfiguration config, IPath location) {
		exportBundles(modules, config, location, null);
	}

	/**
	 * @param modules
	 * @param config not really used
	 * @param location
	 * @param tmpLocation
	 */
	public void exportBundles(List<IModule[]> modules,
			FrameworkInstanceConfiguration config, final IPath location,
			final IPath tmpLocation) {

		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = true;
		info.exportSourceBundle = false;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = true;

		info.destinationDirectory = tmpLocation == null ? location
				.makeAbsolute().toOSString() : tmpLocation.makeAbsolute()
				.toOSString();

		List<Object> allWsBundles = new ArrayList<Object>();
		List<IProject> allBinBundles = new ArrayList<IProject>();

		for (IModule[] moduleArr: modules) {
			try {
				for (IModule iModule : moduleArr) {
					IProject project = iModule.getProject();

					IPluginModelBase pmb = PluginRegistry.findModel(project);
					ModelEntry entry = null;
					if(pmb != null)
					 entry = PluginRegistry.findEntry(pmb
							.getBundleDescription().getSymbolicName());
					if (!WorkspaceModelManager.isBinaryProject(project)
							&& WorkspaceModelManager.isPluginProject(project)) {
						if (entry != null
								&& entry.getModel() != null
								&& isValidModel(entry.getModel())
								&& hasBuildProperties(entry.getModel())
						) {
							allWsBundles.add(entry.getModel());
						} else {
							allBinBundles.add(project);
						}
					} else if (WorkspaceModelManager.isBinaryProject(project)
							&& WorkspaceModelManager.isPluginProject(project)) {
						allBinBundles.add(project);
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		copyBinaryJars(allBinBundles, (tmpLocation == null ? location
				: tmpLocation));
		info.items = allWsBundles.toArray();
		info.signingInfo = null;
		info.qualifier = "qualifier";

		final PluginExportOperation job = new PluginExportOperation(info,
				PDEUIMessages.PluginExportJob_name);
		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setProperty(IProgressConstants.ICON_PROPERTY,
				PDEPluginImages.DESC_PLUGIN_OBJ);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					try {
						if (tmpLocation != null) {
							// MOVE ALL
							// TODO Move this to a separate Job with feedback if errors occur?
							File sourceFiles = tmpLocation.append("/plugins").toFile();
							File[] fileList = sourceFiles.listFiles();
							if (fileList != null) for (File jars : fileList) {
								File destFile = location.append(jars.getName()).toFile();
								
								if (!destFile.exists()) {
									if (! destFile.createNewFile()){
										throw new IOException("Could not create destination file: "+destFile);
									}
								}
								
								FileInputStream fIn = null;
								FileOutputStream fOut = null;
								FileChannel source = null;
								FileChannel destination = null;
								try {
									source = (fIn=new FileInputStream(jars)).getChannel();
									destination = (fOut=new FileOutputStream(destFile)).getChannel();
									destination.transferFrom(source, 0, source.size());
								} finally {
									if (fIn!=null){
										try{
											fIn.close();
										}catch(IOException x){
											x.printStackTrace();
										}
									}
									if (fOut!=null){
										try{
											fOut.close();
										}catch(IOException x){
											x.printStackTrace();
										}
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		job.schedule();

	}

	private static void copyBinaryJars(List<IProject> list, IPath location) {
		FileInputStream fIn = null;
		FileOutputStream fOut = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			for (IProject project : list) {
				for (IResource resource : project.members(true)) {
					if (("jar".equalsIgnoreCase(resource.getFileExtension()))) {
						String jarId = "";
						IPluginModelBase[] models = PluginRegistry
								.getWorkspaceModels();
						for (IPluginModelBase iPluginModelBase : models) {
							if (iPluginModelBase
									.getPluginBase()
									.getId()
									.equalsIgnoreCase(
											resource.getProject().getName())) {
								iPluginModelBase.getPluginBase().getId();
								jarId = iPluginModelBase.getPluginBase()
										.getId()
										+ "_"
										+ iPluginModelBase.getPluginBase()
												.getVersion() + ".jar";
							}
						}

						fIn = new FileInputStream(new File(resource.getLocationURI()));
						fOut = new FileOutputStream(new File(location
								.append(jarId).makeAbsolute()
								.toPortableString())
						);
						inChannel = fIn.getChannel();
						outChannel = fOut.getChannel();
						inChannel.transferTo(0, inChannel.size(), outChannel);
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			if (fIn != null)
				try {
					fIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (fOut != null)
				try {
					fOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private static boolean hasBuildProperties(IPluginModelBase model) {
		File file = new File(model.getInstallLocation(), "build.properties"); //$NON-NLS-1$
		return file.exists();
	}

	protected boolean isValidModel(IModel model) {
		return model != null && model instanceof IPluginModelBase;
	}

}
