package com.how_hard_can_it_be.hexgrid;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.how_hard_can_it_be.hexgrid.ZoomableJPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zoomer implements ActionListener
{
   private static final Logger log = LoggerFactory.getLogger(Zoomer.class);

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
//      System.out.println( "Got event " + e);
      String actionCommand = e.getActionCommand().intern();
      if (LABEL_ZOOM_IN == actionCommand)
      {
         myZoomable.setZoomFactor( (float) (myZoomable.getZoomFactor() * 1.5));
         log.debug("zoom in to {}", myZoomable.getZoomFactor());
         myZoomable.revalidate();
         Rectangle visRect = myZoomable.getVisibleRect();
         myZoomable.repaint( visRect);
      }
      else if (LABEL_ZOOM_OUT == actionCommand)
      {
         myZoomable.setZoomFactor( (float) (myZoomable.getZoomFactor() / 1.5));
         log.debug("zoom out to {}", myZoomable.getZoomFactor());
         myZoomable.revalidate();
         Rectangle visRect = myZoomable.getVisibleRect();
         myZoomable.repaint( visRect);
      }
      else if (LABEL_ZOOM_TO == actionCommand)
         log.warn("Zoom To not implemented yet");
      else
         throw new IllegalArgumentException( "Unexpected ActionCommand: \""
            + actionCommand + "\"");
      
   }

   private ZoomableJPanel myZoomable;
}
