	package org.rapla.plugin.wobviews;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.rapla.components.calendar.DateChangeEvent;
import org.rapla.components.calendar.DateChangeListener;
import org.rapla.facade.CalendarModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.SwingCalendarView;
import org.rapla.gui.toolkit.HTMLView;
import org.rapla.plugin.abstractcalendar.IntervalChooserPanel;

public class WobView extends RaplaGUIComponent implements SwingCalendarView, Printable {
	JPanel test = new JPanel();
	JPanel groupPanel = new JPanel();
	IntervalChooserPanel dateChooser;
	CalendarModel model;
	private JScrollPane editorScrollPane;
	private JTextPane htmlEditorPane;
	private JComboBox sortBy = new JComboBox(new String[] {"Personen","Veranstaltungstypen"});
	boolean editable;
    JButton saveAsFile = new JButton();
    
	public WobView(RaplaContext context, CalendarModel model, boolean editable) throws RaplaException {
		super( context );
		this.editable = editable;
		this.model = model;
		groupPanel.add( new JLabel("Sortiere nach "));
		groupPanel.add( sortBy );
        groupPanel.add( saveAsFile );
        saveAsFile.setAction( createSaveAction());
        
		dateChooser = new IntervalChooserPanel(context,model);
		htmlEditorPane = new HTMLView();
		htmlEditorPane.setEditable(false);
        dateChooser.addDateChangeListener( new DateChangeListener() {
            public void dateChanged( DateChangeEvent evt )
            {
                try {
                    update(  );
                    scrollToStart();
                } catch (RaplaException ex ){
                    showException( ex, getComponent());
                }
            }
        });
        editorScrollPane = new JScrollPane(htmlEditorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(600, 500));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));
        editorScrollPane.setAlignmentX(0);
        editorScrollPane.setAlignmentY(0);
        
        test.setLayout( new BorderLayout());
        if ( editable )
		{
            test.add( groupPanel, BorderLayout.NORTH);
        	test.add( editorScrollPane, BorderLayout.CENTER);
		} 
		else
		{
            test.setBounds(0,0, 800, 1200);
        	test.add( htmlEditorPane, BorderLayout.CENTER);
		}
        
        sortBy.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					update();
				} catch (RaplaException ex) {
					showException( ex, getComponent() );
				}
			}
        	
        });
        update();
	}

    private Action createSaveAction() {
        AbstractAction action = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent evt) {
                try {
                    final PreviewCreator previewCreator;
                    if ( WobExportPlugin.sortByPersons ) 
                    {
                        previewCreator = new PersonPreviewCreator( getContext());
                    }
                    else
                    {
                        previewCreator = new LecturePreviewCreator( getContext());
                    }
                    final WobHtmlExporter preview = new WobHtmlExporter(getContext());
                    Date startDate = model.getStartDate();
                    Date endDate = model.getEndDate();
                    preview.calc(startDate, endDate, model, previewCreator);
                    preview.saveFile();
                } catch (Exception x) {
                    showException(x, getMainComponent());
                }
            }
        };
        action.putValue( Action.SMALL_ICON,getIcon("icon.save"));
        action.putValue( Action.NAME,"Speichern als Html");
        return action;
    }

	public void update() throws RaplaException {
		WobExportPlugin.sortByPersons = "Personen".equals( sortBy.getSelectedItem());
	    htmlEditorPane.setText( createWob());
        dateChooser.update();
    	scrollToStart();
	}
	
	private String createWob() throws RaplaException {
		PreviewCreator creator;
		if ( WobExportPlugin.sortByPersons )
		{
			creator = new PersonPreviewCreator(getContext());
		}
		else
		{
			creator = new LecturePreviewCreator(getContext());
		}
		return creator.export( model.getStartDate(), model.getEndDate(), model.getReservations());
	}

	public JComponent getDateSelection() {
		return dateChooser.getComponent();
	}

	public void scrollToStart() {
        SwingUtilities.invokeLater( new Runnable() {
        	public void run()
        	{
        		editorScrollPane.getVerticalScrollBar().setValue(0);
        	}
        });
	}

	public JComponent getComponent() {
		return test;
	}

    RTFRenderer renderer ;
    
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if ( pageIndex == 0)
        {
            renderer = new RTFRenderer();
            renderer.setpane( htmlEditorPane);
        }
        return renderer.print( graphics, pageFormat, pageIndex);
    }

}
