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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rapla.components.calendar.RaplaCalendar;
import org.rapla.components.layout.TableLayout;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.RaplaWidget;

/** sample UseCase that only displays the text of the configuration and
 all reservations of the user.*/
class ExportDialog extends RaplaGUIComponent implements RaplaWidget
{

    RaplaCalendar sourcePeriodChooser;
    RaplaCalendar destPeriodChooser;
    JPanel panel = new JPanel();
    JLabel label = new JLabel();
    public final static String ROLE = ExportDialog.class.getName();

    public ExportDialog(RaplaContext sm) throws RaplaException {
        super(sm);
        sourcePeriodChooser = createRaplaCalendar();
        destPeriodChooser = createRaplaCalendar();
        label.setText("Ausgewaehlte Veranstaltungen exportiern:");
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.setLayout(new TableLayout(new double[][]{
                 {TableLayout.PREFERRED ,5 , TableLayout.FILL }
                 ,{20, 5, TableLayout.PREFERRED ,5 ,TableLayout.PREFERRED }
        }
        ));
        panel.add(label, "0,0,2,1");
        panel.add( new JLabel(getString("start_date")),"0,2" );
        panel.add( sourcePeriodChooser,"2,2" );
        panel.add( new JLabel(getString("end_date")),"0,4" );
        panel.add( destPeriodChooser,"2,4" );
    }

    public RaplaCalendar getDestChooser() {
        return destPeriodChooser;
    }

    public RaplaCalendar getSourceChooser() {
        return sourcePeriodChooser;
    }

    public JComponent getComponent() {
        return panel;
    }
}

