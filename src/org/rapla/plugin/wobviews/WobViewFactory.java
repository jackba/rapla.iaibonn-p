package org.rapla.plugin.wobviews;

import javax.swing.Icon;

import org.rapla.facade.CalendarModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.SwingCalendarView;
import org.rapla.gui.SwingViewFactory;
import org.rapla.gui.images.Images;
import org.rapla.servletpages.RaplaPageGenerator;

public class WobViewFactory implements SwingViewFactory {

	public final static String WOB_VIEW = "wobview";

	public SwingCalendarView createSwingView(RaplaContext context, CalendarModel model, boolean editable) throws RaplaException {
		return new WobView( context, model, editable);
	}

	public RaplaPageGenerator createHTMLView(RaplaContext context, CalendarModel model) throws RaplaException {
		return null;
	}

	public String getViewId() {
		return WOB_VIEW;
	}

	public String getMenuSortKey() {
		return "F";
	}

	public String getName() {
		return "Textansicht";
	}

    Icon icon;
    public Icon getIcon()
    {
        if ( icon == null) {
            icon = Images.getIcon("/org/rapla/plugin/wobviews/images/wob.png");
        }
        return icon;
    }
}
