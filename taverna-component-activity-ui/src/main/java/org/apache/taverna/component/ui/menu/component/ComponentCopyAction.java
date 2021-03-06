/**
 * 
 */
package org.apache.taverna.component.ui.menu.component;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;
import org.apache.taverna.component.api.Component;
import org.apache.taverna.component.api.ComponentException;
import org.apache.taverna.component.api.Family;
import org.apache.taverna.component.api.Version;
import org.apache.taverna.component.api.profile.Profile;
import org.apache.taverna.component.ui.panel.ComponentChoiceMessage;
import org.apache.taverna.component.ui.panel.ComponentChooserPanel;
import org.apache.taverna.component.ui.panel.ProfileChoiceMessage;
import org.apache.taverna.component.ui.panel.RegistryAndFamilyChooserPanel;
import org.apache.taverna.component.ui.preference.ComponentPreference;
import org.apache.taverna.component.ui.serviceprovider.ComponentServiceIcon;
import org.apache.taverna.component.ui.serviceprovider.ComponentServiceProviderConfig;
import org.apache.taverna.component.ui.util.Utils;

/**
 * @author alanrw
 */
public class ComponentCopyAction extends AbstractAction {
	private static final long serialVersionUID = -4440978712410081685L;
	private static final Logger logger = getLogger(ComponentCopyAction.class);
	private static final String COPY_COMPONENT = "Copy component...";

	private final ComponentPreference prefs;
	private final Utils utils;

	public ComponentCopyAction(ComponentPreference pref, ComponentServiceIcon icon, Utils utils) {
		super(COPY_COMPONENT, icon.getIcon());
		this.prefs = pref;
		this.utils = utils;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		ComponentChooserPanel source = new ComponentChooserPanel(prefs);
		source.setBorder(new TitledBorder("Source component"));

		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(source, gbc);

		final RegistryAndFamilyChooserPanel target = new RegistryAndFamilyChooserPanel(prefs);
		target.setBorder(new TitledBorder("Target family"));
		gbc.gridy++;
		overallPanel.add(target, gbc);

		source.addObserver(new Observer<ComponentChoiceMessage>() {
			@Override
			public void notify(Observable<ComponentChoiceMessage> sender,
					ComponentChoiceMessage message) throws Exception {
				Profile componentProfile = null;
				Family componentFamily = message.getComponentFamily();
				if (componentFamily != null)
					componentProfile = componentFamily.getComponentProfile();
				ProfileChoiceMessage profileMessage = new ProfileChoiceMessage(
						componentProfile);
				target.notify(null, profileMessage);
			}
		});

		int answer = showConfirmDialog(null, overallPanel, "Copy Component",
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doCopy(source.getChosenComponent(), target.getChosenFamily());
	}

	private void doCopy(Component sourceComponent, Family targetFamily) {
		if (sourceComponent == null) {
			showMessageDialog(null, "Unable to determine source component",
					"Component Copy Problem", ERROR_MESSAGE);
			return;
		} else if (targetFamily == null) {
			showMessageDialog(null, "Unable to determine target family",
					"Component Copy Problem", ERROR_MESSAGE);
			return;
		}

		try {
			String componentName = sourceComponent.getName();
			boolean alreadyUsed = targetFamily.getComponent(componentName) != null;
			if (alreadyUsed)
				showMessageDialog(null, componentName + " is already used",
						"Duplicate component name", ERROR_MESSAGE);
			else {
				Version targetVersion = doCopy(sourceComponent, targetFamily,
						componentName);
				try {
					utils.refreshComponentServiceProvider(new ComponentServiceProviderConfig(
							targetVersion.getID()).getConfiguration());
				} catch (Exception e) {
					logger.error(e);
				}
			}
		} catch (ComponentException e) {
			logger.error("failed to copy component", e);
			showMessageDialog(null,
					"Unable to create component: " + e.getMessage(),
					"Component Copy Problem", ERROR_MESSAGE);
		}
	}

	private Version doCopy(Component sourceComponent, Family targetFamily,
			String componentName) throws ComponentException {
		return targetFamily
				.createComponentBasedOn(
						componentName,
						sourceComponent.getDescription(),
						sourceComponent
								.getComponentVersionMap()
								.get(sourceComponent.getComponentVersionMap()
										.lastKey()).getImplementation());
	}
}
