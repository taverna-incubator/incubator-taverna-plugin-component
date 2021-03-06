/**
 * 
 */
package org.apache.taverna.component.ui.preference;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.ui.DeselectingButton;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class LocalRegistryPanel extends JPanel {
	private static final String BROWSE_LABEL = "Browse";
	private static final String LOCATION_LABEL = "Location:";
	private static final String NAME_LABEL = "Name:";
	private static final long serialVersionUID = 732945735813617327L;

	private final Logger logger = getLogger(LocalRegistryPanel.class);

	private JTextField registryNameField = new JTextField(20);
	private JTextField locationField = new JTextField(20);

	public LocalRegistryPanel() {
		super(new GridBagLayout());

		setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 20;
		add(new JLabel(NAME_LABEL), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = HORIZONTAL;
		add(registryNameField, constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		constraints.ipadx = 20;
		constraints.fill = NONE;
		add(new JLabel(LOCATION_LABEL), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = HORIZONTAL;
		add(locationField, constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		constraints.ipadx = 20;
		constraints.fill = NONE;
		add(new DeselectingButton(new AbstractAction(BROWSE_LABEL) {
			private static final long serialVersionUID = -8676803966947261009L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				pickDirectory();
			}
		}), constraints);
	}

	private void pickDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(LocalRegistryPanel.this);
		try {
			if (returnVal == APPROVE_OPTION)
				locationField.setText(chooser.getSelectedFile()
						.getCanonicalPath());
		} catch (IOException e) {
			logger.error("unexpected filesystem problem", e);
		}
	}

	/**
	 * @return the registryNameField
	 */
	public JTextField getRegistryNameField() {
		return registryNameField;
	}

	/**
	 * @return the locationField
	 */
	public JTextField getLocationField() {
		return locationField;
	}
}
