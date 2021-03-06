/**
 * 
 */
package org.apache.taverna.component.ui.menu.component;

import static java.lang.String.format;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.workbench.file.FileManager;

import org.apache.log4j.Logger;
import org.apache.taverna.component.api.Component;
import org.apache.taverna.component.api.ComponentException;
import org.apache.taverna.component.api.Version;
import org.apache.taverna.component.ui.panel.ComponentChooserPanel;
import org.apache.taverna.component.ui.preference.ComponentPreference;
import org.apache.taverna.component.ui.serviceprovider.ComponentServiceIcon;
import org.apache.taverna.component.ui.serviceprovider.ComponentServiceProviderConfig;
import org.apache.taverna.component.ui.util.Utils;

import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * @author alanrw
 */
public class ComponentDeleteAction extends AbstractAction {
	private static final String COMPONENT_PROBLEM_TITLE = "Component Problem";
	private static final String CONFIRM_MSG = "Are you sure you want to delete %s?";
	private static final String CONFIRM_TITLE = "Delete Component Confirmation";
	private static final String DELETE_COMPONENT_LABEL = "Delete component...";
	private static final String DELETE_FAILED_TITLE = "Component Deletion Error";
	private static final String FAILED_MSG = "Unable to delete %s: %s";
	private static final String OPEN_COMPONENT_MSG = "The component is open";
	private static final String TITLE = "Component choice";
	private static final String WHAT_COMPONENT_MSG = "Unable to determine component";
	private static final long serialVersionUID = -2992743162132614936L;
	private static final Logger logger = getLogger(ComponentDeleteAction.class);

	private final FileManager fm;
	private final ComponentPreference prefs;
	private final Utils utils;

	public ComponentDeleteAction(FileManager fm, ComponentPreference prefs,
			ComponentServiceIcon icon, Utils utils) {
		super(DELETE_COMPONENT_LABEL, icon.getIcon());
		this.fm = fm;
		this.prefs = prefs;
		this.utils = utils;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		ComponentChooserPanel panel = new ComponentChooserPanel(prefs);
		int answer = showConfirmDialog(null, panel, TITLE, OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doDelete(panel.getChosenComponent());
	}

	private void doDelete(final Component chosenComponent) {
		if (chosenComponent == null) {
			showMessageDialog(null, WHAT_COMPONENT_MSG,
					COMPONENT_PROBLEM_TITLE, ERROR_MESSAGE);
		} else if (componentIsInUse(chosenComponent)) {
			showMessageDialog(null, OPEN_COMPONENT_MSG,
					COMPONENT_PROBLEM_TITLE, ERROR_MESSAGE);
		} else if (showConfirmDialog(null,
				format(CONFIRM_MSG, chosenComponent.getName()), CONFIRM_TITLE,
				YES_NO_OPTION) == YES_OPTION)
			new SwingWorker<Configuration, Object>() {
				@Override
				protected Configuration doInBackground() throws Exception {
					return deleteComponent(chosenComponent);
				}

				@Override
				protected void done() {
					refresh(chosenComponent, this);
				}
			}.execute();
	}

	private Configuration deleteComponent(Component component)
			throws ComponentException {
		ComponentServiceProviderConfig config = new ComponentServiceProviderConfig(
				component.getFamily());
		component.delete();
		return config.getConfiguration();
	}

	protected void refresh(Component component,
			SwingWorker<Configuration, Object> worker) {
		try {
			utils.refreshComponentServiceProvider(worker.get());
		} catch (ExecutionException e) {
			logger.error("failed to delete component", e.getCause());
			showMessageDialog(
					null,
					format(FAILED_MSG, component.getName(), e.getCause()
							.getMessage()), DELETE_FAILED_TITLE, ERROR_MESSAGE);
		} catch (InterruptedException e) {
			logger.warn("interrupted during component deletion", e);
		}
	}

	private boolean componentIsInUse(Component component) {
		for (WorkflowBundle d : fm.getOpenDataflows()) {
			Object dataflowSource = fm.getDataflowSource(d);
			if (dataflowSource instanceof Version.ID
					&& ((Version.ID) dataflowSource).mostlyEqualTo(component))
				return true;
		}
		return false;
	}
}
