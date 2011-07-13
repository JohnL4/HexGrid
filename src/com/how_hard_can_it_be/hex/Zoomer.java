package com.how_hard_can_it_be.hex;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Sets zoom factor on HexCanvas.
 * 
 * @author John
 *
 */
public class Zoomer implements ActionListener
{
   /**
    * Label for menu entry or button and also ActionCommand for ActionEvent.
    */
   public static final String
      LABEL_ZOOM_IN = "Zoom In",
      LABEL_ZOOM_OUT = "Zoom Out",
      LABEL_ZOOM_TO = "Zoom To . . .";
      
   public Zoomer( HexCanvas aHexCanvas)
   {
      _hexCanvas = aHexCanvas;
   }
   
   @Override
   public void actionPerformed( ActionEvent e)
   {
      System.out.println( "Got event " + e);
      String actionCommand = e.getActionCommand().intern();
      if (LABEL_ZOOM_IN == actionCommand)
      {
         _hexCanvas.setZoomFactor( (float) (_hexCanvas.getZoomFactor() * 1.5));
         _hexCanvas.revalidate();
//         Rectangle visRect = _hexCanvas.getVisibleRect();
//         _hexCanvas.repaint( visRect);
      }
      else if (LABEL_ZOOM_OUT == actionCommand)
      {
         _hexCanvas.setZoomFactor( (float) (_hexCanvas.getZoomFactor() / 1.5));
         _hexCanvas.revalidate();
//         Rectangle visRect = _hexCanvas.getVisibleRect();
//         _hexCanvas.repaint( visRect);
      }
      else if (LABEL_ZOOM_TO == actionCommand)
         System.out.println( "Zoom To not implemented yet");
      else
         throw new IllegalArgumentException( "Unexpected ActionCommand: \""
            + actionCommand + "\"");
      
   }

   private HexCanvas _hexCanvas;
}
