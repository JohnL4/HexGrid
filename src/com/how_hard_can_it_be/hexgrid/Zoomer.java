package com.how_hard_can_it_be.hexgrid;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.how_hard_can_it_be.hexgrid.ZoomableJPanel;

public class Zoomer implements ActionListener
{
   /**
    * Label for menu entry or button and also ActionCommand for ActionEvent.
    */
   public static final String
      LABEL_ZOOM_IN = "Zoom In",
      LABEL_ZOOM_OUT = "Zoom Out",
      LABEL_ZOOM_TO = "Zoom To . . .";
      
   public Zoomer( ZoomableJPanel aZoomable)
   {
      myZoomable = aZoomable;
   }
   
   @Override
   public void actionPerformed( ActionEvent e)
   {
      System.out.println( "Got event " + e);
      String actionCommand = e.getActionCommand().intern();
      if (LABEL_ZOOM_IN == actionCommand)
      {
         myZoomable.setZoomFactor( (float) (myZoomable.getZoomFactor() * 1.5));
         myZoomable.revalidate();
         Rectangle visRect = myZoomable.getVisibleRect();
         myZoomable.repaint( visRect);
      }
      else if (LABEL_ZOOM_OUT == actionCommand)
      {
         myZoomable.setZoomFactor( (float) (myZoomable.getZoomFactor() / 1.5));
         myZoomable.revalidate();
         Rectangle visRect = myZoomable.getVisibleRect();
         myZoomable.repaint( visRect);
      }
      else if (LABEL_ZOOM_TO == actionCommand)
         System.out.println( "Zoom To not implemented yet");
      else
         throw new IllegalArgumentException( "Unexpected ActionCommand: \""
            + actionCommand + "\"");
      
   }

   private ZoomableJPanel myZoomable;
}
