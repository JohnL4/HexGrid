package com.how_hard_can_it_be.hexgrid;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * Stupid canvas for playing around w/affine transforms.
 * 
 * @author John.Lusk@how-hard-can-it-be.com
 *
 */
public class GiantArrowCanvas extends ZoomableJPanel
{
//   public Dimension getPreferredSize()
//   {
//      return new Dimension(800, 600);
//   }
   
   protected void paintZoomableContents( Graphics2D g2)
   {
      drawArrow( g2);
   }
   
   /**
    * @param g2
    */
   void drawArrow( Graphics2D g2)
   {
      g2.setStroke(new BasicStroke(5.0f));

      g2.drawLine( 30, 30, 770, 570);
      
      // Arrowhead
      int ra = 40;   // "radius" of arrowhead
      double alpha;  // angle of arrow, from horizontal
      double beta;   // angle of arrowhead sides, from arrow itself
      beta = 15 * Math.PI/180;
      
      double arrowLength = Math.sqrt( 740*740 + 540*540);
      alpha = Math.asin( 540/arrowLength);
      
      g2.drawLine( 770, 570, 
         (int) Math.round( 770 - ra * Math.cos(alpha + beta)),
         (int) Math.round( 570 - ra * Math.sin(alpha + beta)));
      g2.drawLine( 770, 570, 
         (int) Math.round( 770 - ra * Math.cos(alpha - beta)),
         (int) Math.round( 570 - ra * Math.sin(alpha - beta)));
   }
   
}
