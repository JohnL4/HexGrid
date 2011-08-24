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
      Rectangle2D.Double dim = getInitialRectangle();
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
    * Does not clone Dimension (which is mutable).  Probably a Bad Idea to modify the mutable object this returns.
    * @return
    */
   protected Rectangle2D.Double getInitialRectangle()
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
   
      AffineTransform prevXform = g2.getTransform();
      Stroke prevStroke = g2.getStroke();

      g2.scale( getZoomFactor(), getZoomFactor());
      g2.translate( -myRectangle.x, -myRectangle.y);
   
      paintZoomableContents( g2);
   
      g2.setTransform( prevXform);
      g2.setStroke( prevStroke);
   }

   protected abstract void paintZoomableContents( Graphics2D g2);

   private double myZoomFactor = 1.0f;
   private Rectangle2D.Double myRectangle;

}
