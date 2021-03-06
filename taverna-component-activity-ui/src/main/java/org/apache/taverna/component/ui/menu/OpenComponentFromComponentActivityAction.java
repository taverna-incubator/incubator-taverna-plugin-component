/**
 * 
 */
package org.apache.taverna.component.ui.menu;

import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;

import org.apache.log4j.Logger;
import org.apache.taverna.component.api.ComponentFactory;
import org.apache.taverna.component.api.Version;
import org.apache.taverna.component.ui.ComponentAction;
import org.apache.taverna.component.ui.ComponentActivityConfigurationBean;
import org.apache.taverna.component.ui.serviceprovider.ComponentServiceIcon;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
public class OpenComponentFromComponentActivityAction extends ComponentAction {
	private static Logger logger = getLogger(OpenComponentFromComponentActivityAction.class);

	private final FileManager fileManager;
	private final ComponentFactory factory;
	private final FileType fileType;

	public OpenComponentFromComponentActivityAction(FileManager fileManager,
			ComponentFactory factory, FileType ft,
			GraphViewComponent graphView, ComponentServiceIcon icon) {
		super("Open component...", graphView);
		this.fileManager = fileManager;
		this.factory = factory;
		this.fileType = ft;
		setIcon(icon);
	}

	private Activity selection;

	@Override
	public void actionPerformed(ActionEvent ev) {
		try {
			Version.ID ident = new ComponentActivityConfigurationBean(
					selection.getConfiguration(), factory);
			WorkflowBundle d = fileManager.openDataflow(fileType, ident);
			markGraphAsBelongingToComponent(d);
		} catch (OpenException e) {
			logger.error("failed to open component", e);
		} catch (MalformedURLException e) {
			logger.error("bad URL in component description", e);
		}
	}

	public void setSelection(Activity selection) {
		this.selection = selection;
	}
}
