package net.sf.taverna.t2.component.ui.view;

import java.awt.Frame;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import uk.org.taverna.commons.services.ServiceRegistry;
import uk.org.taverna.scufl2.api.activity.Activity;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.component.api.ComponentFactory;
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;

public class ComponentActivityContextViewFactory implements
		ContextualViewFactory<Activity> {
	private ColourManager colourManager;//FIXME beaninject
	private ViewUtil util;//FIXME beaninject
	public ComponentFactory factory;//FIXME beaninject
	public ActivityIconManager aim;//FIXME beaninject
	public ServiceDescriptionRegistry sdr;//FIXME beaninject
	public EditManager em;//FIXME beaninject
	public FileManager fm;//FIXME beaninject
	public ServiceRegistry sr;//FIXME beaninject

	@Override
	public boolean canHandle(Object selection) {
		//FIXME
		return false;
		//return selection instanceof ComponentActivity;
	}

	@Override
	public List<ContextualView> getViews(Activity selection) {
		return Arrays.<ContextualView>asList(new ComponentActivityContextualView(selection));
	}

	@SuppressWarnings("serial")
	private class ComponentActivityContextualView extends
			HTMLBasedActivityContextualView {
		public ComponentActivityContextualView(Activity activity) {
			super(activity, colourManager);
			init();
		}

		private void init() {
		}

		@Override
		public String getViewTitle() {
			return "Component service";
		}

		/**
		 * View position hint
		 */
		@Override
		public int getPreferredPosition() {
			// We want to be on top
			return 100;
		}

		@Override
		public Action getConfigureAction(Frame owner) {
			return new ComponentConfigureAction(getActivity(), owner, factory, aim, sdr, em, fm, sr);
		}

		@Override
		protected String getRawTableRowsHtml() {
			return util.getRawTablesHtml(getConfigBean());
		}
	}
}
