package org.rapla.plugin.wobviews;

import java.awt.Frame;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.SwingUtilities;

import org.rapla.components.iolayer.IOInterface;
import org.rapla.facade.CalendarModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;

public class WobHtmlExporter extends RaplaGUIComponent {

	public final static String ROLE = WobHtmlExporter.class.getName();

	
	private String output;

	public WobHtmlExporter(RaplaContext context) throws RaplaException {
		super(context);
	}


	public void calc(final Date start, final Date end, final CalendarModel model, final PreviewCreator previewCreator) {
		try {
			output = previewCreator.export(start, end, model.getReservations(start, end));
		} catch (Exception x) {
			showException(x, getMainComponent());
		}
	}

	public void saveFile() throws RaplaException {
		if (this.output == null)
			throw new NullPointerException();

		final Frame frame = (Frame) SwingUtilities.getRoot(getMainComponent());
		IOInterface io = getService( IOInterface.class);
        byte[] content = output.getBytes();
        try {
            io.saveFile( frame, null, new String[] {"html"}, createFileName(), content);
        } catch (IOException e) {
            throw new RaplaException("Cant export file!", e);
        }
    }


	private String createFileName() {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int month = calendar.get(Calendar.MONTH);
		final int year = calendar.get(Calendar.YEAR);

		return "Textansicht_" + day + "." + month + "." + year + ".html";
	}


}
