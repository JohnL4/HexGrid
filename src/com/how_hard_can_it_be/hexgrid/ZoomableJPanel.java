package com.how_hard_can_it_be.hexgrid;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

public abstract class ZoomableJPanel extends JPanel
{
   /**
    * Preferred initial height of this panel.
    */
   public static final int INITIAL_HEIGHT = 600;
   /**
    * Preferred initial width of this panel.
    */
   public static final int INITIAL_WIDTH = 800;

   public ZoomableJPanel()
   {
   }
   
   /**
    * 
    * @param anInitialRectangle The rectangle we're interested in showing in this panel.  May be null, in which case
    *    a rectangle of size of {@value #INITIAL_WIDTH}x{@value #INITIAL_HEIGHT} will be used.
    * @param anInitialZoomFactor The initial zoom factor (for very small or large rectangles).  (Use 1.0 for no zooming.)
    */
   public ZoomableJPanel( Rectangle2D.Double anInitialRectangle, double anInitialZoomFactor)
   {
      myRectangle = anInitialRectangle;
      myZoomFactor = anInitialZoomFactor;
   }
   
   public Dimension getPreferredSize()
   {
      Dimension retval;
      Rectangle2D.Double dim = getRectangle();
      double zf = getZoomFactor();
      retval = new Dimension(
         (int) Math.round( dim.width * zf),
         (int) Math.round( dim.height * zf));
      return retval;
   }
   
//   /**
//    * The translation to be applied, to scroll points onto the canvas before scaling.  May be null.
//    * (Not the same as using the scrollbars on the panel, because, without translation, only non-negative coordinates
//    * are displayed, and you can't scroll out of the first quadrant of the Cartesian plane.)
//    */
//   public Rectangle2D.Double getTranslate()
//   {
//      return myTranslate;
//   }
//
//   /**
//    * @param aTranslate The translation to be applied before scaling.  May be null.
//    */
//   public void setTranslate( Rectangle2D.Double aTranslate)
//   {
//      myTranslate = aTranslate;
//   }

   /**
    * The factor by which to zoom the canvas during display.
    * @return
    */
   public double getZoomFactor()
   {
      return myZoomFactor;
   }

   public void setZoomFactor( double aZoomFactor)
   {
      myZoomFactor = aZoomFactor;
   }

   /**
    * Returns reference to last transform used to paint this panel.  Do not modify it.
    * @return
    */
   public AffineTransform getLastTransform()
   {
      return myLastXform;
   }
   
   /**
    * The bounding rectangle of the figure this zoomable panel will display.  Return value should not be modified.
    * @return
    */
   protected Rectangle2D.Double getRectangle()
   {
      if (myRectangle == null)
         myRectangle = new Rectangle2D.Double(0, 0, INITIAL_WIDTH, INITIAL_HEIGHT);
      return myRectangle;
   }
   
   /**
    * Saves previous AffineTransform and Stoke, calls {@see #paintZoomableContents(Graphics2D)}, restores transform
    * and stroke.
    */
   protected void paintComponent( Graphics aG)
   {
      super.paintComponent( aG);
   
      Graphics2D g2 = (Graphics2D) aG;
   
      AffineTransform prevXform = g2.getTransform();  // If you inspect this transform, it's not the identity, and I can't explain that.
      Stroke prevStroke = g2.getStroke();
      
      // Doesn't work:
//      AffineTransform newXform = AffineTransform.getTranslateInstance( -myRectangle.x, -myRectangle.y);
//      newXform.scale( getZoomFactor(), getZoomFactor());
//      g2.setTransform( newXform);

      // 'Nuther attempt:
      AffineTransform xform = AffineTransform.getScaleInstance( getZoomFactor(), getZoomFactor());
      xform.translate( -getRectangle().x, -getRectangle().y);
      myLastXform = xform;
      
      // Whatever the transform was initially, scale it up to the current zoom factor and translate it to expose the
      // points outside of the first quadrant that we would normally never see.
//      g2.scale( getZoomFactor(), getZoomFactor());
//      g2.translate( -myRectangle.x, -myRectangle.y);
      g2.transform( xform);   // TODO: explain that transforms are applied in a last-in, first-out order.  Transform composition is explained in Graphics2D.transform() and G2D.translate().
      // myLastXform = g2.getTransform();
      
      paintZoomableContents( g2);
   
      g2.setTransform( prevXform);
      g2.setStroke( prevStroke);
   }

   protected abstract void paintZoomableContents( Graphics2D g2);

   private double myZoomFactor = 1.0f;
   private Rectangle2D.Double myRectangle;

   /**
    * Last (most-recent) transform used to paint.
    */
   private AffineTransform myLastXform;
}
