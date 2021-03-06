/**
 *
 */
package org.apache.taverna.component.ui.panel;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.event.ItemEvent.SELECTED;
import static org.apache.log4j.Logger.getLogger;
import static org.apache.taverna.component.ui.util.Utils.LONG_STRING;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;
import org.apache.taverna.component.api.Component;
import org.apache.taverna.component.api.Family;
import org.apache.taverna.component.api.Registry;
import org.apache.taverna.component.ui.preference.ComponentPreference;

/**
 * @author alanrw
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComponentChooserPanel extends JPanel implements
		Observable<ComponentChoiceMessage>, Observer {
	private static final String NAME_LABEL = "Component name:";
	private static final long serialVersionUID = -4459660016225074302L;
	private static Logger logger = getLogger(ComponentChooserPanel.class);

	private final List<Observer<ComponentChoiceMessage>> observers = new ArrayList<>();
	private final JComboBox<String> componentChoice = new JComboBox<>();
	private final SortedMap<String, Component> componentMap = new TreeMap<>();
	private final RegistryAndFamilyChooserPanel registryAndFamilyChooserPanel;

	public ComponentChooserPanel(ComponentPreference prefs, FamilyChooserPanel familyPanel) {
		this(prefs);
		familyPanel.addObserver(new Observer<FamilyChoiceMessage>() {
			@Override
			public void notify(Observable<FamilyChoiceMessage> sender,
					FamilyChoiceMessage message) throws Exception {
				try {
					updateComponentModel();
				} catch (Exception e) {
					logger.error("problem when component/family was selected",
							e);
				}
			}
		});
	}

	public ComponentChooserPanel(ComponentPreference prefs) {
		super(new GridBagLayout());
		registryAndFamilyChooserPanel = new RegistryAndFamilyChooserPanel(prefs);

		componentChoice.setPrototypeDisplayValue(LONG_STRING);

		updateComponentModel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = WEST;
		gbc.fill = HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		add(registryAndFamilyChooserPanel, gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		add(new JLabel(NAME_LABEL), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		add(componentChoice, gbc);
		registryAndFamilyChooserPanel.addObserver(this);

		componentChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED) {
					updateToolTipText();
					notifyObservers();
				}
			}
		});
	}

	protected void updateToolTipText() {
		Component chosenComponent = componentMap.get(componentChoice
				.getSelectedItem());
		if (chosenComponent == null)
			componentChoice.setToolTipText(null);
		else
			componentChoice.setToolTipText(chosenComponent.getDescription());
	}

	private void notifyObservers() {
		ComponentChoiceMessage message = new ComponentChoiceMessage(
				registryAndFamilyChooserPanel.getChosenFamily(),
				getChosenComponent());
		for (Observer<ComponentChoiceMessage> o : getObservers())
			try {
				o.notify(ComponentChooserPanel.this, message);
			} catch (Exception e) {
				logger.error(
						"observer had problem with component selection message",
						e);
			}
	}

	private void updateComponentModel() {
		componentMap.clear();
		componentChoice.removeAllItems();
		componentChoice.setToolTipText(null);
		notifyObservers();
		componentChoice.addItem("Reading components");
		componentChoice.setEnabled(false);
		new ComponentUpdater().execute();
	}

	public Component getChosenComponent() {
		if (componentMap.isEmpty())
			return null;
		return componentMap.get(componentChoice.getSelectedItem());
	}

	@Override
	public void notify(Observable sender, Object message) {
		try {
			if (message instanceof FamilyChoiceMessage)
				updateComponentModel();
			else if (message instanceof ProfileChoiceMessage)
				registryAndFamilyChooserPanel.notify(null,
						(ProfileChoiceMessage) message);
		} catch (Exception e) {
			logger.error("problem when component/family was selected", e);
		}
	}

	@Override
	public void addObserver(Observer<ComponentChoiceMessage> observer) {
		observers.add(observer);
		Component chosenComponent = getChosenComponent();
		ComponentChoiceMessage message = new ComponentChoiceMessage(
				registryAndFamilyChooserPanel.getChosenFamily(),
				chosenComponent);
		try {
			observer.notify(this, message);
		} catch (Exception e) {
			logger.error("failed to notify about addition of observer", e);
		}
	}

	@Override
	public List<Observer<ComponentChoiceMessage>> getObservers() {
		return observers;
	}

	@Override
	public void removeObserver(Observer<ComponentChoiceMessage> observer) {
		observers.remove(observer);
	}

	public Registry getChosenRegistry() {
		return registryAndFamilyChooserPanel.getChosenRegistry();
	}

	public Family getChosenFamily() {
		return registryAndFamilyChooserPanel.getChosenFamily();
	}

	private class ComponentUpdater extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			Family chosenFamily = registryAndFamilyChooserPanel
					.getChosenFamily();
			if (chosenFamily != null)
				for (Component component : chosenFamily.getComponents())
					componentMap.put(component.getName(), component);

			return null;
		}

		@Override
		protected void done() {
			componentChoice.removeAllItems();
			try {
				get();
				for (String componentName : componentMap.keySet())
					componentChoice.addItem(componentName);
				if (!componentMap.isEmpty()) {
					componentChoice.setSelectedItem(componentMap.firstKey());
					updateToolTipText();
				} else
					componentChoice.addItem("No components available");
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e);
				componentChoice.addItem("Unable to read components");
			}
			notifyObservers();
			componentChoice.setEnabled(!componentMap.isEmpty());
		}
	}
}
