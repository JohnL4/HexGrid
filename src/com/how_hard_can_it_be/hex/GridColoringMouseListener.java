package com.how_hard_can_it_be.hex;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Listens to mouse clicks and tells HexCanvas to repaint one or more of its
 * hexes.
 * 
 * @author John
 */
public class GridColoringMouseListener extends MouseAdapter
{
   public void mouseClicked( MouseEvent anEvt)
   {
      HexCanvas hc = (HexCanvas) anEvt.getComponent();
      float zf = hc.getZoomFactor();
      
      HexPoint hexClicked = hc.hexAtXY( anEvt.getPoint()); 

      System.out.println( "Got event " + anEvt + "\n\t(x = " + anEvt.getX()/zf
         + ", y = " + anEvt.getY()/zf 
         + "), hex = (" + hexClicked.x + ", " + hexClicked.y + ")");
   }
}
