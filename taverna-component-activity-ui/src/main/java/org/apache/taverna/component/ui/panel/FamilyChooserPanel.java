/**
 *
 */
package org.apache.taverna.component.ui.panel;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NONE;
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
import org.apache.taverna.component.api.ComponentException;
import org.apache.taverna.component.api.Family;
import org.apache.taverna.component.api.Registry;
import org.apache.taverna.component.api.profile.Profile;

/**
 * @author alanrw
 */
public class FamilyChooserPanel extends JPanel implements
		Observer<ProfileChoiceMessage>, Observable<FamilyChoiceMessage> {
	private static final String FAMILY_LABEL = "Component family:";
	private static final String READING_MSG = "Reading families";
	private static final long serialVersionUID = -2608831126562927778L;
	private static Logger logger = getLogger(FamilyChooserPanel.class);

	private final List<Observer<FamilyChoiceMessage>> observers = new ArrayList<>();
	private final JComboBox<String> familyBox = new JComboBox<>();
	// private JTextArea familyDescription = new JTextArea(10,60);
	private final SortedMap<String, Family> familyMap = new TreeMap<>();
	private Registry chosenRegistry = null;
	private Profile profileFilter = null;

	public FamilyChooserPanel(RegistryChooserPanel registryPanel) {
		this();
		registryPanel.addObserver(new Observer<RegistryChoiceMessage>() {
			@Override
			public void notify(Observable<RegistryChoiceMessage> sender,
					RegistryChoiceMessage message) throws Exception {
				try {
					chosenRegistry = message.getChosenRegistry();
					updateList();
				} catch (RuntimeException e) {
					logger.error("failed to update list after registry choice",
							e);
				}
			}
		});
	}

	public FamilyChooserPanel() {
		super(new GridBagLayout());
		familyBox.setPrototypeDisplayValue(LONG_STRING);

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = NONE;
		this.add(new JLabel(FAMILY_LABEL), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = BOTH;
		this.add(familyBox, gbc);
		familyBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED) {
					updateDescription();
					notifyObservers();
				}
			}
		});

		familyBox.setEditable(false);
	}

	protected void updateDescription() {
		Family chosenFamily = getChosenFamily();
		if (chosenFamily != null)
			familyBox.setToolTipText(chosenFamily.getDescription());
		else
			familyBox.setToolTipText(null);
	}

	private void updateList() {
		familyMap.clear();
		familyBox.removeAllItems();
		familyBox.setToolTipText(null);
		notifyObservers();
		familyBox.addItem(READING_MSG);
		familyBox.setEnabled(false);
		new FamilyUpdater().execute();
	}

	private void notifyObservers() {
		Family chosenFamily = getChosenFamily();
		FamilyChoiceMessage message = new FamilyChoiceMessage(chosenFamily);
		for (Observer<FamilyChoiceMessage> o : getObservers())
			try {
				o.notify(this, message);
			} catch (Exception e) {
				logger.error("failed to notify about change of state of panel",
						e);
			}
	}

	public Family getChosenFamily() {
		if (familyBox.getSelectedIndex() < 0)
			return null;
		return familyMap.get(familyBox.getSelectedItem());
	}

	@Override
	public void addObserver(Observer<FamilyChoiceMessage> observer) {
		observers.add(observer);
		Family chosenFamily = getChosenFamily();
		FamilyChoiceMessage message = new FamilyChoiceMessage(chosenFamily);
		try {
			observer.notify(this, message);
		} catch (Exception e) {
			logger.error("failed to notify about family choice", e);
		}
	}

	@Override
	public List<Observer<FamilyChoiceMessage>> getObservers() {
		return observers;
	}

	@Override
	public void removeObserver(Observer<FamilyChoiceMessage> observer) {
		observers.remove(observer);
	}

	@Override
	public void notify(Observable<ProfileChoiceMessage> sender,
			ProfileChoiceMessage message) throws Exception {
		try {
			profileFilter = message.getChosenProfile();
			updateList();
		} catch (RuntimeException e) {
			logger.error("failed to update list after profile choice", e);
		}
	}

	private void updateFamiliesFromRegistry() throws ComponentException {
		for (Family f : chosenRegistry.getComponentFamilies()) {
			if (profileFilter == null) {
				familyMap.put(f.getName(), f);
				continue;
			}
			Profile componentProfile;
			try {
				componentProfile = f.getComponentProfile();
			} catch (ComponentException | RuntimeException e) {
				logger.error("failed to get profile of component", e);
				componentProfile = null;
			}
			if (componentProfile != null) {
				String id = componentProfile.getId();
				if ((profileFilter == null) || id.equals(profileFilter.getId()))
					familyMap.put(f.getName(), f);
			} else
				logger.info("Ignoring " + f.getName());
		}
	}

	private class FamilyUpdater extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			if (chosenRegistry != null)
				updateFamiliesFromRegistry();
			return null;
		}

		@Override
		protected void done() {
			familyBox.removeAllItems();
			try {
				get();
				for (String name : familyMap.keySet())
					familyBox.addItem(name);
				if (!familyMap.isEmpty()) {
					String firstKey = familyMap.firstKey();
					familyBox.setSelectedItem(firstKey);
					updateDescription();
				} else
					familyBox.addItem("No families available");
			} catch (InterruptedException | ExecutionException e) {
				familyBox.addItem("Unable to read families");
				logger.error(e);
			}

			notifyObservers();
			familyBox.setEnabled(!familyMap.isEmpty());
		}
	}
}
