/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import static net.sf.taverna.t2.component.ui.menu.profile.ComponentProfileMenuSection.COMPONENT_PROFILE_SECTION;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.component.api.ComponentFactory;
import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 */
public class ComponentProfileImportMenuAction extends AbstractMenuAction {
	private static final URI COMPONENT_PROFILE_IMPORT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentProfileImport");

	private ComponentFactory factory;
	private ComponentPreference prefs;
	private ComponentServiceIcon icon;

	public ComponentProfileImportMenuAction() {
		super(COMPONENT_PROFILE_SECTION, 200, COMPONENT_PROFILE_IMPORT_URI);
	}

	public void setComponentFactory(ComponentFactory factory) {
		this.factory = factory;
	}
	
	public void setIcon(ComponentServiceIcon icon) {
		this.icon = icon;
	}

	public void setPreferences(ComponentPreference prefs) {
		this.prefs = prefs;
	}

	@Override
	protected Action createAction() {
		return new ComponentProfileImportAction(factory, prefs, icon);
	}
}
