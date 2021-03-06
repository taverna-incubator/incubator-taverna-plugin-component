/**
 * 
 */
package org.apache.taverna.component.ui.panel;

import org.apache.taverna.component.api.Registry;

/**
 * @author alanrw
 */
public class RegistryChoiceMessage {
	private final Registry chosenRegistry;

	public RegistryChoiceMessage(Registry chosenRegistry) {
		this.chosenRegistry = chosenRegistry;
	}

	/**
	 * @return the chosenRegistry
	 */
	public Registry getChosenRegistry() {
		return chosenRegistry;
	}
}
