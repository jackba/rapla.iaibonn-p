package org.rapla.plugin.wobviews;

/** Found in the web without copyright */
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.JTextPane;
import javax.swing.text.View;

public class RTFRenderer implements Printable 
{
    //This external class that does all the printing of all the rtf document pages
 
    int currentPage = -1;               
    JTextPane jtextPane = new JTextPane();            
    double pageEndY = 0;              
    double pageStartY = 0;      
    boolean scaleWidthToFit = true;
    int currentIndex = 0;
 
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
    {
        //This function is the main printable overided function
 
        double scale = 1.0;
        Graphics2D graphics2D;
        View rootView;
        graphics2D = (Graphics2D) graphics;
 
        jtextPane.setSize((int)pageFormat.getImageableWidth(),Integer.MAX_VALUE);
        jtextPane.validate();
//    I think the error is somwhere here

        rootView = jtextPane.getUI().getRootView(jtextPane);
 
        if((scaleWidthToFit) && (jtextPane.getMinimumSize().getWidth() >
        pageFormat.getImageableWidth()))
        {
            scale = pageFormat.getImageableWidth()/jtextPane.getMinimumSize().getWidth();
            graphics2D.scale(scale, scale);
        }
 
        //The below four command lines shows that the content is clipped 
        //to the size of the printable page
 
        graphics2D.setClip((int)(pageFormat.getImageableX()/scale),
        (int)(pageFormat.getImageableY()/scale),
        (int)(pageFormat.getImageableWidth()/scale),
        (int)(pageFormat.getImageableHeight()/scale));
 
        //The below if statement is to check to see if there is a new page to render
 
        if(pageIndex > currentPage)
        {
            currentPage = pageIndex;
            pageStartY += pageEndY;
            pageEndY = graphics2D.getClipBounds().getHeight();
        }
 
        graphics2D.translate(graphics2D.getClipBounds().getX(),
        graphics2D.getClipBounds().getY());
 
        Rectangle allocation = new Rectangle(0, (int)-pageStartY,
        (int)(jtextPane.getMinimumSize().getWidth()),
        (int)(jtextPane.getPreferredSize().getHeight()));
 
        //The below if else statements return PAGE_EXISTS only if the class 
        //sees that there are some contents in the document by calling the printView class 
 
        if(printView(graphics2D,allocation,rootView))
        {
            return PAGE_EXISTS;
        }
 
        else
        {
            pageStartY = 0;
            pageEndY = 0;
            currentPage = -1;
            currentIndex = 0;
            return NO_SUCH_PAGE;
        }
 
    }
 
    protected boolean printView(Graphics2D graphics2D, Shape allocation, View view)
    {
        //This function paints the page if it exists
 
        boolean pageExists = false;
        Rectangle clipRectangle = graphics2D.getClipBounds();
        Shape childAllocation;
        View childView;
 
        if(view.getViewCount() > 0)
        {
 
            for(int i = 0;i<view.getViewCount();i++)
            {
 
                childAllocation = view.getChildAllocation(i,allocation);
                if (childAllocation != null)
                {
                    childView = view.getView(i);
 
                    if(printView(graphics2D,childAllocation,childView)) 
                    {
                        pageExists = true;
                    }
 
                }
 
            }
 
        }
 
        else 
        {
            //The below if statement checks if there are pages currently to paint
 
            if(allocation.getBounds().getMaxY() >= clipRectangle.getY())
            {
                pageExists = true;
 
                if((allocation.getBounds().getHeight() > clipRectangle.getHeight()) &&
                (allocation.intersects(clipRectangle)))
                {
                    view.paint(graphics2D,allocation);
                }
 
                else
                {
 
                    if(allocation.getBounds().getY() >= clipRectangle.getY())
                    {
 
                        if(allocation.getBounds().getMaxY() <= clipRectangle.getMaxY() - 15)
                        {
                            view.paint(graphics2D,allocation);
                        }
 
                        else
                        {
 
                            if(allocation.getBounds().getY() < pageEndY)
                            {
                                pageEndY = allocation.getBounds().getY();
                            }
 
                        }
 
                    }
 
                }
 
            }
 
        }
 
        return pageExists;
    }
 
    public void setpane(JTextPane TextPane1)
    {
        //This function gets the JTextPane
 
        jtextPane.setContentType("text/rtf");
        jtextPane = TextPane1;
    }
 
}

