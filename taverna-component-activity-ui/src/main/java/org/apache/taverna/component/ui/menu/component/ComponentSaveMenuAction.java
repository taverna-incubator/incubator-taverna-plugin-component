/**
 * 
 */
package org.apache.taverna.component.ui.menu.component;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.component.ui.serviceprovider.ComponentServiceIcon;
import org.apache.taverna.component.ui.util.Utils;

import net.sf.taverna.t2.workbench.file.FileManager;

/**
 * @author alanrw
 */
public class ComponentSaveMenuAction extends AbstractComponentMenuAction {
	private static final URI SAVE_COMPONENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentSave");

	private Action action;
	private FileManager fm;
	private ComponentServiceIcon icon;
	private Utils utils;

	public ComponentSaveMenuAction() {
		super(1100, SAVE_COMPONENT_URI);
	}

	//FIXME beaninject net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction
	public void setSaveWorkflowAction(Action action) {
		this.action = action;
	}

	public void setFileManager(FileManager fm) {
		this.fm = fm;
	}

	public void setIcon(ComponentServiceIcon icon) {
		this.icon = icon;
	}

	public void setUtils(Utils utils) {
		this.utils = utils;
	}

	@Override
	protected Action createAction() {
		return new ComponentSaveAction(action, fm, icon, utils);
	}
}
