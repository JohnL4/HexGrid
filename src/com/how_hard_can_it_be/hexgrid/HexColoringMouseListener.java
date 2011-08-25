package com.how_hard_can_it_be.hexgrid;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class HexColoringMouseListener extends MouseAdapter
{
   Hex prevHex;
   
   public void mouseClicked( MouseEvent anEvt)
   {
      ZoomableJPanel zp = (ZoomableJPanel) anEvt.getComponent();
      Point clickedPoint = anEvt.getPoint();
      System.out.printf( "mouse clicked @ (%d, %6d)%s", clickedPoint.x, clickedPoint.y, Const.CRLF);
      try
      {
         // Inverting the transform used to paint most recently doesn't work, because that transform starts off with
         // an odd, unexplained (to me) value.  So, instead, just make a new transform that "should" work (but actually
         // doesn't, if you use it to draw), and then invert it, so we can convert mouse coordinates (device space)
         // back to the coordinates of our hex grid (user space?).  I don't know why this works, but it seems to.
         
         AffineTransform xform = AffineTransform.getScaleInstance( zp.getZoomFactor(), zp.getZoomFactor());
         xform.translate( -zp.getRectangle().x, -zp.getRectangle().y);
         AffineTransform xformInv = xform.createInverse();
         
         Point2D.Double pt = new Point2D.Double( clickedPoint.x, clickedPoint.y);
         Point2D.Double xformedPt = new Point2D.Double(); 
         xformInv.transform( pt, xformedPt);
         
         System.out.printf( "\tmouse click in grid's x-y coordinates: (%7.4g, %7.4g)%s",
            xformedPt.x, xformedPt.y, Const.CRLF);
      }
      catch (NoninvertibleTransformException exc)
      {
         // This really ought not to happen, unless we did something really stupid like use a zoom factor of 0.
         exc.printStackTrace();
      }
   }
}
