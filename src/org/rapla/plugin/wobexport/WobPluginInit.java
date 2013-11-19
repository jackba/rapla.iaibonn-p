/*--------------------------------------------------------------------------*
 | Copyright (C) 2006 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.plugin.wobexport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.rapla.entities.Category;
import org.rapla.entities.domain.Permission;
import org.rapla.facade.CalendarModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.IdentifiableMenuEntry;
import org.rapla.plugin.mail.MailToUserInterface;

public class WobPluginInit extends RaplaGUIComponent  implements IdentifiableMenuEntry,ActionListener  {

	String id = "Wob Export";
	private JMenuItem item;

	CalendarModel model;

	public WobPluginInit(RaplaContext sm) throws RaplaException {
		super(sm);
		try {
			Category exportGroups = getQuery().getSuperCategory().getCategory(
					Permission.GROUP_CATEGORY_KEY).getCategory("informatik")
					.getCategory("export");
			if (!getUser().isAdmin() && !getUser().belongsTo(exportGroups)) {
				return;
			}
		} catch (Exception ex) {
			if (!getUser().isAdmin())
				return;
		}
		item = new JMenuItem(id);
		item.setIcon(getIcon("icon.export"));
		item.setIcon( getIcon("icon.export") );
        item.addActionListener(this);
        model = (CalendarModel) getService(CalendarModel.class);
	}
	
    
    public String getId() {
		return id;
	}

	public JMenuItem getMenuElement() {
		return item;
	}
		
	public void actionPerformed(ActionEvent evt) {
		try {
			/*
			final ExportDialog useCase = new ExportDialog(getContext());
			
			String[] buttons = new String[] { getString("abort"),
					getString("export") };
			final DialogUI dialog = DialogUI.create(getContext(),
					getMainComponent(), true, useCase.getComponent(),
					buttons);
			dialog.setTitle("Veranstaltung exportieren");
			dialog.setSize(400, 200);
			dialog.getButton(0).setIcon(getIcon("icon.abort"));
			dialog.getButton(1).setIcon(getIcon("icon.copy"));

			// dialog.getButton( 1).setEnabled(false);
			final RaplaCalendar sourceBox = useCase.getSourceChooser();
			final RaplaCalendar destBox = useCase.getDestChooser();
			sourceBox.setDate(model.getSelectedDate());
			Calendar cal = getRaplaLocale().createCalendar();
			cal.setTime(model.getSelectedDate());
			cal.add(Calendar.MONTH, 1);
			destBox.setDate(cal.getTime());
			DateChangeListener listener = new DateChangeListener() {
				public void dateChanged(DateChangeEvent evt) {
					Date startDate = sourceBox.getDate();
					Date endDate = destBox.getDate();
					dialog.getButton(1).setEnabled(
							startDate.before(endDate));
				}
			};
			sourceBox.addDateChangeListener(listener);
			destBox.addDateChangeListener(listener);
			dialog.startNoPack();
			if (dialog.getSelectedIndex() == 1) {
				Date startDate = sourceBox.getDate();
				Date endDate = destBox.getDate();
				export(startDate, endDate);
			}
			*/
			CalendarModel model = getService(CalendarModel.class);
             
			Date startDate = model.getStartDate();
			Date endDate = model.getEndDate();
			export(startDate, endDate);
			
		} catch (Exception ex) {
			showException(ex, getMainComponent());
		}
	
	}
	
	private void export(Date start, Date end) throws RaplaException {
		final WobExport export = new WobExport(getContext());
		export.setHighlightProblems(true);

		final String[] buttons = new String[] { getString("abort"),
				getString("export") };

		// versuch mit dem formatiertem document :::
		final JTextPane editorPane = new JTextPane();
		editorPane.setEditable(true);

		final StyledDocument doc = editorPane.getStyledDocument();
		final Style defaultStyle = StyleContext.getDefaultStyleContext()
				.getStyle(StyleContext.DEFAULT_STYLE);

		final Style regular = doc.addStyle("regular", defaultStyle);
		final Style error = doc.addStyle("error", regular);
		StyleConstants.setForeground(error, Color.red);

		final List lines = export.exportAsStringArray(model.getReservations(
				start, end));

		// insert entry into the document ...
		for (int i = 0, count = lines.size(); i < count; i++) {
			final Object value = lines.get(i);
			final Style selectedStyle;
			final String content;

			if (value instanceof StyledWobEntry) {
				final StyledWobEntry entry = (StyledWobEntry) value;
				if (entry.isError()) {
					selectedStyle = error;
				} else {
					selectedStyle = regular;
				}

				content = entry.getLine();
			} else {
				content = String.valueOf(value);
				selectedStyle = regular;
			}

			try {
				doc.insertString(doc.getLength(), content, selectedStyle);
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}

		// setze content-pane
		final JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(600, 500));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		editorScrollPane.setAlignmentX(0);
		editorScrollPane.setAlignmentY(0);

		final DialogUI dialog = DialogUI.create(getContext(),
				getMainComponent(), true, editorScrollPane, buttons);
		dialog.setTitle("Exportierte Liste per Mail verschicken");
		dialog.setSize(600, 500);
		dialog.getButton(0).setIcon(getIcon("icon.abort"));
		dialog.getButton(1).setIcon(getIcon("icon.mail"));
		dialog.startNoPack();

		if (dialog.getSelectedIndex() == 1) {
			MailToUserInterface mail = (MailToUserInterface) getContext()
					.lookup(MailToUserInterface.class);
			String subject = "WOB Export ";

			String body;

			try {
				body = doc.getText(0, doc.getLength());
			} catch (BadLocationException e) {
				body = "Problem occured. Please contact the Rapla-Developer or/and the Administrator.\n\n "
						+ e.getLocalizedMessage();
			}

			mail.sendMail(getUser().getUsername(), subject, body);
            System.out.println(body);
		}

	}

}
