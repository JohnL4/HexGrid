package com.how_hard_can_it_be.hexgrid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class HexGridCanvas extends ZoomableJPanel
{
   private static final Color[] COLORS = { Color.RED, new Color(0, 127, 0), Color.BLUE };
   
   public HexGridCanvas( HexGrid aHexGrid) // Hex[][] aHexesArray, int aNumHorizHexes, int aNumVertHexes)
   {
      super( aHexGrid.getBounds(), 
         getInitialZoomFactor( aHexGrid)); // aHexesArray, aNumHorizHexes, aNumVertHexes));
      myHexGrid = aHexGrid;
//      myHexes = aHexesArray;
//      myNumHorizHexes = aNumHorizHexes;
//      myNumVertHexes = aNumVertHexes;
   }
   
   /**
    * Returns initial zoom factor to make hex grid fit in initial window reasonably.
    * @param aHexArray
    * @param aNumHorizHexes
    * @param aNumVertHexes
    * @return
    */
   private static double getInitialZoomFactor( HexGrid aHexGrid) // Hex[][] aHexArray, int aNumHorizHexes, int aNumVertHexes)
   {
      double retval, 
         zoomHoriz, zoomVert,
         minX, minY, maxX, maxY;
      Rectangle2D.Double bounds = aHexGrid.getBounds();
//     Hex[][] hexes = aHexGrid.getHexes();
//     
//      minX = hexes[0][0].getEdge( 2).getVertex1().x;
//      minY = hexes[0][0].getEdge( 4).getVertex0().y; // Runs backwards.
//      
//      maxX = hexes[aHexGrid.getNumHorizHexes()-1][1].getEdge( 0).getVertex0().x;
//      maxY = hexes[0][aHexGrid.getNumVertHexes()-1].getEdge( 1).getVertex0().y;
      
      zoomHoriz = ZoomableJPanel.INITIAL_WIDTH / bounds.width;
      zoomVert = ZoomableJPanel.INITIAL_HEIGHT / bounds.height;
      
      retval = Math.min( zoomHoriz, zoomVert);
      
      return retval;
   }
   
   @Override
   protected void paintZoomableContents( Graphics2D aG2)
   {
      drawHexes( aG2);
   }

   private void drawHexes( Graphics2D g2)
   {
      Hex[][] hexes = myHexGrid.getHexes();
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setStroke( new BasicStroke((float) (1.5 / getZoomFactor())));
      for (int j = 0; j < myHexGrid.getNumVertHexes(); j++)
      {
         for (int i = 0; i < myHexGrid.getNumHorizHexes(); i++)
         {
            g2.setPaint( COLORS[i % COLORS.length]);
            Hex hex = hexes[i][j];
            for (int k = 0; k < 6; k++)
            {
               Edge e = hex.getEdge(k);
               g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
            }
         }
      }
   }

   private HexGrid myHexGrid;
   
//   private Hex[][] myHexes;
//   private int myNumHorizHexes, myNumVertHexes;
}
