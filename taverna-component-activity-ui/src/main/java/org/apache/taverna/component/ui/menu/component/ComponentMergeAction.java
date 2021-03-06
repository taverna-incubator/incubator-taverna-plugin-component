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
import org.apache.taverna.component.api.Version;
import org.apache.taverna.component.ui.panel.ComponentChoiceMessage;
import org.apache.taverna.component.ui.panel.ComponentChooserPanel;
import org.apache.taverna.component.ui.panel.ProfileChoiceMessage;
import org.apache.taverna.component.ui.preference.ComponentPreference;
import org.apache.taverna.component.ui.serviceprovider.ComponentServiceIcon;

/**
 * @author alanrw
 */
public class ComponentMergeAction extends AbstractAction {
	private static final long serialVersionUID = 6791184757725253807L;
	private static final Logger logger = getLogger(ComponentMergeAction.class);
	private static final String MERGE_COMPONENT = "Merge component...";

	private final ComponentPreference prefs;

	public ComponentMergeAction(ComponentPreference prefs,
			ComponentServiceIcon icon) {
		super(MERGE_COMPONENT, icon.getIcon());
		this.prefs = prefs;
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

		final ComponentChooserPanel target = new ComponentChooserPanel(prefs);
		target.setBorder(new TitledBorder("Target component"));
		gbc.gridy++;
		overallPanel.add(target, gbc);

		source.addObserver(new Observer<ComponentChoiceMessage>() {
			@Override
			public void notify(Observable<ComponentChoiceMessage> sender,
					ComponentChoiceMessage message) throws Exception {
				target.notify(null, new ProfileChoiceMessage(message
						.getComponentFamily().getComponentProfile()));
			}
		});

		int answer = showConfirmDialog(null, overallPanel, "Merge Component",
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doMerge(source.getChosenComponent(), target.getChosenComponent());
	}

	private void doMerge(Component sourceComponent, Component targetComponent) {
		if (sourceComponent == null) {
			showMessageDialog(null, "Unable to determine source component",
					"Component Merge Problem", ERROR_MESSAGE);
			return;
		} else if (targetComponent == null) {
			showMessageDialog(null, "Unable to determine target component",
					"Component Merge Problem", ERROR_MESSAGE);
			return;
		} else if (sourceComponent.equals(targetComponent)) {
			showMessageDialog(null, "Cannot merge a component with itself",
					"Component Merge Problem", ERROR_MESSAGE);
			return;
		}

		try {
			Version sourceVersion = sourceComponent.getComponentVersionMap()
					.get(sourceComponent.getComponentVersionMap().lastKey());
			targetComponent.addVersionBasedOn(
					sourceVersion.getImplementation(), "Merge from "
							+ sourceComponent.getFamily().getName() + ":"
							+ sourceComponent.getName());
		} catch (ComponentException e) {
			logger.error("failed to merge component", e);
			showMessageDialog(null, "Failed to merge component: " + e,
					"Component Merge Problem", ERROR_MESSAGE);
		}
	}
}
