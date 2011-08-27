package com.how_hard_can_it_be.hexgrid;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class HexColoringMouseListener extends MouseAdapter
{
   /**
    * Previously-clicked point.
    */
   private Point myPrevHexIx;
   private Point myPrevDraggedHexIx;
   
//   public void mousePressed( MouseEvent anEvt)
//   {
//      System.out.println( "mousePressed");
//   }
   
//   public void mouseReleased( MouseEvent anEvt)
//   {
//      System.out.println( "mouseReleased");
//   }
   
   public void mouseDragged( MouseEvent anEvt)
   {
//      System.out.println( "mouseDragged");
      try
      {
         Point hexIx = hexContainingMouseEvent( anEvt);
         if (hexIx.equals( myPrevDraggedHexIx))
            ;
         else
         {
            HexGridCanvas hgc = (HexGridCanvas) anEvt.getComponent();
            hgc.paintHex( hexIx, Color.YELLOW, true);
            myPrevDraggedHexIx = hexIx;
         }
      }
      catch (NoninvertibleTransformException exc)
      {
         // Should never happen.
         exc.printStackTrace();
      }
   }
   
   public void mouseClicked( MouseEvent anEvt)
   {
      Point clickedPoint = anEvt.getPoint();
//      System.out.printf( "mouse clicked @ (%d, %6d)%s", clickedPoint.x, clickedPoint.y, Const.CRLF);
      try
      {
         Point hexIx = hexContainingMouseEvent( anEvt);
         
//         System.out.printf( "\tcontaining hex is (%d, %d)%s", hexIx.x, hexIx.y, Const.CRLF);

//         if (myPrevHexIx != null)
//         {
//            hgc.paintHex( myPrevHexIx, null);
//         }
         HexGridCanvas hgc = (HexGridCanvas) anEvt.getComponent();
         hgc.paintHex( hexIx, Color.YELLOW, true);
         myPrevHexIx = hexIx;
      }
      catch (NoninvertibleTransformException exc)
      {
         // This really ought not to happen, unless we did something really stupid like use a zoom factor of 0.
         exc.printStackTrace();
      }
   }
   
   /**
    * Returns the index of the hex (in the hexgrid hex array) of the hex containing the mouse event.
    * @param anEvt
    * @return
    * @throws NoninvertibleTransformException Should never occur, since scale will never be zero.
    */
   private Point hexContainingMouseEvent( MouseEvent anEvt) throws NoninvertibleTransformException
   {
      HexGridCanvas hgc = (HexGridCanvas) anEvt.getComponent();

      // Inverting the transform used to paint most recently doesn't work, because that transform starts off with
      // an odd, unexplained (to me) value.  So, instead, just make a new transform that "should" work (but actually
      // doesn't, if you use it to draw), and then invert it, so we can convert mouse coordinates (device space)
      // back to the coordinates of our hex grid (user space?).  I don't know why this works, but it seems to.
      
      AffineTransform xform = AffineTransform.getScaleInstance( hgc.getZoomFactor(), hgc.getZoomFactor());
      xform.translate( -hgc.getRectangle().x, -hgc.getRectangle().y);
      AffineTransform xformInv = xform.createInverse();
      
      Point2D.Double pt = new Point2D.Double( anEvt.getPoint().x, anEvt.getPoint().y);
      Point2D.Double xformedPt = new Point2D.Double(); 
      xformInv.transform( pt, xformedPt);
      
//      System.out.printf( "\tmouse event in grid's x-y coordinates: (%7.4g, %7.4g)%s",
//         xformedPt.x, xformedPt.y, Const.CRLF);
      
      Point hexIx = hgc.getHexGrid().hexContaining( xformedPt);
      
      return hexIx;
   }
}
