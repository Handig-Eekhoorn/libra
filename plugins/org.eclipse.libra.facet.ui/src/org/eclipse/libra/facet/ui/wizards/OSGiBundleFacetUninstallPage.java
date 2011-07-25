/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kaloyan Raev (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.facet.ui.wizards;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.libra.facet.OSGiBundleFacetUninstallConfig;
import org.eclipse.libra.facet.OSGiBundleFacetUninstallStrategy;
import org.eclipse.libra.facet.internal.ui.LibraFacetUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.project.facet.ui.AbstractFacetWizardPage;


public class OSGiBundleFacetUninstallPage extends AbstractFacetWizardPage {

	private static final String WIZARD_PAGE_NAME = "osgi.bundle.facet.uninstall.page"; //$NON-NLS-1$
	private static final String IMG_PATH_BUNDLE_WIZBAN = "icons/wizban/bundle_wizban.png"; //$NON-NLS-1$
	
	private OSGiBundleFacetUninstallConfig config;

	public OSGiBundleFacetUninstallPage() {
		super(WIZARD_PAGE_NAME);

		setTitle(Messages.OSGiBundleFacetUninstallPage_Title);
        setDescription(Messages.OSGiBundleFacetUninstallPage_Description);
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(LibraFacetUIPlugin.PLUGIN_ID, IMG_PATH_BUNDLE_WIZBAN));
	}

	public void setConfig(Object config) {
		this.config = (OSGiBundleFacetUninstallConfig) config;
	}

	public void createControl(Composite parent) {
		DataBindingContext dbc = new DataBindingContext();
		WizardPageSupport.create(this, dbc);
		
		Composite container = new Composite(parent, SWT.NONE);
		
		Button[] buttons = new Button[OSGiBundleFacetUninstallStrategy.values().length];
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new Button(container, SWT.RADIO);
			buttons[i].setText(OSGiBundleFacetUninstallStrategy.values()[i].description());
			dbc.bindValue(
					SWTObservables.observeSelection(buttons[i]), 
					config.getOptionValues()[i], 
					null, 
					null);
		}
		
		GridLayoutFactory.swtDefaults().generateLayout(container);
		setControl(container);
		Dialog.applyDialogFont(container);
	}

}
