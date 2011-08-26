package com.how_hard_can_it_be.hexgrid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class HexGridCanvas extends ZoomableJPanel
{
   private static final Color[] FOREGROUND_COLORS = { Color.RED, new Color(0, 127, 0), Color.BLUE };
   private static final Color[] BACKGROUND_COLORS = { new Color(0xFFCEC8), new Color(0xDCCEFF), 
      new Color(0xFFF7C8), new Color(0xC8FFD4)};

   /**
    * How many times {@see #paintZoomableContents(Graphics2D)} has been called.
    */
   private static int ourPaintCount = 0;

   public HexGridCanvas( HexGrid aHexGrid) // Hex[][] aHexesArray, int aNumHorizHexes, int aNumVertHexes)
   {
      super( aHexGrid.getBounds(), 
         getInitialZoomFactor( aHexGrid)); // aHexesArray, aNumHorizHexes, aNumVertHexes));
      myHexGrid = aHexGrid;
//      myHexes = aHexesArray;
//      myNumHorizHexes = aNumHorizHexes;
//      myNumVertHexes = aNumVertHexes;
   }
   
   public HexGrid getHexGrid()
   {
      return myHexGrid;
   }
   
   /**
    * Paints the given hex indexed by aHexIx the given color.  If hex indexes are out of range, does nothing.
    * @param aHexIx
    */
   public void paintHex( Point aHexIx, Color aColor)
   {
      if (0 <= aHexIx.x && aHexIx.x < myHexGrid.getNumHorizHexes()
            && 0 <= aHexIx.y && aHexIx.y < myHexGrid.getNumVertHexes())
      {
         Hex hex = myHexGrid.getHexes()[ aHexIx.x][ aHexIx.y];
         hex.setBackgroundColor( aColor);
         Rectangle2D.Double hexBox = hex.getBounds2D();
         Point2D.Double lower = new Point2D.Double( hexBox.x, hexBox.y);
         Point2D.Double upper = new Point2D.Double( hexBox.x + hexBox.width,
            hexBox.y + hexBox.height);
         Point2D.Double[] xformedPts = new Point2D.Double[2];
         getLastTransform().transform( new Point2D.Double[] { lower, upper },
            0, xformedPts, 0, 2);
         int x, y, width, height;
         x = (int) Math.floor( xformedPts[ 0].x);
         y = (int) Math.floor( xformedPts[ 0].y);
         width = (int) (Math.ceil( xformedPts[ 1].x) - x);
         height = (int) (Math.ceil( xformedPts[ 1].y) - y);
         repaint( x, y, width, height);
      }
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
      // TODO: get clip region and only paint that.
      Rectangle clipRect = aG2.getClipBounds();
      
      aG2.setPaint( BACKGROUND_COLORS[ourPaintCount++ % BACKGROUND_COLORS.length]);
      aG2.fill( clipRect);
      
      HexGrid hg = getHexGrid();
      Point lowerLeft = hg.hexContaining( new Point2D.Double( clipRect.getMinX(), clipRect.getMinY()));
      lowerLeft.translate( -1, -1);
      Point upperRight = hg.hexContaining( new Point2D.Double( clipRect.getMaxX(), clipRect.getMaxY()));
      upperRight.translate( 1, 1);

//      lowerLeft = new Point(0,0);
//      upperRight = new Point(hg.getNumHorizHexes()-1, hg.getNumVertHexes()-1);
      
      System.out.printf( "painting, clip rect = (%7.4g, %7.4g) --> (%7.4g, %7.4g);\thex rect = (%d, %d) --> (%d, %d)%s", 
         clipRect.getMinX(), clipRect.getMinY(), clipRect.getMaxX(), clipRect.getMaxY(),
         lowerLeft.x, lowerLeft.y, upperRight.x, upperRight.y,
         Const.CRLF);
      
      drawHexes( aG2, lowerLeft, upperRight); 
   }

   /**
    * Draws the indicated hexes.  Corner points are inclusive.  Points are indexes into hexes array.
    * @param g2
    * @param aLowerLeftCorner
    * @param anUpperRightCorner
    */
   private void drawHexes( Graphics2D g2, Point aLowerLeftCorner, Point anUpperRightCorner)
   {
      Hex[][] hexes = myHexGrid.getHexes();
      int nh = myHexGrid.getNumHorizHexes();
      int nv = myHexGrid.getNumVertHexes();
      
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setStroke( new BasicStroke((float) (1.5 / getZoomFactor())));
      
      Point lowerLeft = new Point( Math.max( 0, aLowerLeftCorner.x), Math.max( 0, aLowerLeftCorner.y));
      Point upperRight = new Point( Math.min( nh-1, anUpperRightCorner.x), Math.min( nv-1, anUpperRightCorner.y));
      for (int j = lowerLeft.y; j <= upperRight.y; j++)
      {
         for (int i = lowerLeft.x; i <= upperRight.x; i++)
         {
            g2.setPaint( FOREGROUND_COLORS[i % FOREGROUND_COLORS.length]);
            Hex hex = hexes[i][j];
            for (int k = 0; k < 6; k++)
            {
               Edge e = hex.getEdge(k);
               g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
            }
            Color hexColor = hex.getBackgroundColor();
            if (hexColor != null)
            {
               Path2D.Double hexPath = new Path2D.Double();
               Edge e = hex.getEdge(0);
               Point2D.Double v = e.getVertex0();
               hexPath.moveTo(v.x, v.y);
               v = e.getVertex1();
               hexPath.lineTo( v.x, v.y);
               e = hex.getEdge( 1);
               v = e.getVertex1();
               hexPath.lineTo( v.x, v.y);
               e = hex.getEdge( 2);
               v = e.getVertex1();
               hexPath.lineTo( v.x, v.y);

               e = hex.getEdge( 3);
               v = e.getVertex0();  // Runs backwards
               hexPath.lineTo( v.x, v.y);
               e = hex.getEdge( 4);
               v = e.getVertex0();
               hexPath.lineTo( v.x, v.y);
               hexPath.closePath();
               g2.setPaint( hexColor);
               g2.fill( hexPath);
            }
         }
      }
   }

   private HexGrid myHexGrid;
   
//   private Hex[][] myHexes;
//   private int myNumHorizHexes, myNumVertHexes;
}
