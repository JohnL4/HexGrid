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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexGridCanvas extends ZoomableJPanel
{
   private static final Logger log = LoggerFactory.getLogger(HexGridCanvas.class);

   private static final Color[] FOREGROUND_COLORS = { new Color( 0x8F0E00), new Color( 0x8F7900), new Color( 0x200562), new Color( 0x006C18) };
   private static final Color[] BACKGROUND_COLORS = { new Color(0xFFCEC8), new Color(0xDCCEFF), new Color(0xFFF7C8), new Color(0xC8FFD4)};

   /** Partially-opaque red used for click, drag, and {@link HexGrid#hexLine} fills. */
   static final Color ourHexFill = new Color( 0x50ff0000, true);

   /** Partially-opaque grey used for the axial (incorrectly-rounded) line walk.
    * Compositing like this allows you to see where the same hex is hit twice by this algorithm (which is incorrect),
    * because it's darker.
    * */
   static final Color ourAxialLine = new Color( 0x40000000, true);

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
    * @param aColor
    * @aToggle If true, hex state will be toggled between colored and not colored.
    */
   public void paintHex( Point aHexIx, Color aColor, boolean aToggle)
   {
      if (0 <= aHexIx.x && aHexIx.x < myHexGrid.getNumHorizHexes()
            && 0 <= aHexIx.y && aHexIx.y < myHexGrid.getNumVertHexes())
      {
         Hex hex = myHexGrid.getHexes()[ aHexIx.x][ aHexIx.y];
         if (aToggle)
         {
            if (hex.getBackgroundColor() == null)
               hex.setBackgroundColor( aColor);
            else
               hex.setBackgroundColor( null);
         }
         else
         {
            Color existing = hex.getBackgroundColor();
            if (aColor == null || existing == null)
               hex.setBackgroundColor( aColor);
            else
               hex.setBackgroundColor( sourceOver( existing, aColor));
         }
         
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
    * Paints each hex on {@link HexGrid#hexLine} from {@code aStart} through {@code anEnd} with
    * {@link #ourHexFill}. Does not toggle. Indexes outside the map are skipped.
    * @param aStart
    * @param anEnd
    */
   public void paintHexLine( Point aStart, Point anEnd)
   {
      paintHexes( myHexGrid.hexLine( aStart, anEnd), ourHexFill);
   }

   /**
    * Paints each hex in {@code aHexIxs} with {@code aColor}. Does not toggle. Indexes outside the map are skipped.
    * @param aHexIxs
    * @param aColor
    */
   void paintHexes( List<Point> aHexIxs, Color aColor)
   {
      for (Point hexIx : aHexIxs)
         paintHex( hexIx, aColor, false);
   }

   /**
    * Source-over of {@code aSrc} onto {@code aDest}, straight alpha. Returns {@code aSrc} when the result is fully transparent.
    */
   private static Color sourceOver( Color aDest, Color aSrc)
   {
      float srcA = aSrc.getAlpha() / 255f;
      float dstA = aDest.getAlpha() / 255f;
      float outA = srcA + dstA * (1f - srcA);
      if (outA == 0f)
         return aSrc;
      float outR = (aSrc.getRed() / 255f * srcA + aDest.getRed() / 255f * dstA * (1f - srcA)) / outA;
      float outG = (aSrc.getGreen() / 255f * srcA + aDest.getGreen() / 255f * dstA * (1f - srcA)) / outA;
      float outB = (aSrc.getBlue() / 255f * srcA + aDest.getBlue() / 255f * dstA * (1f - srcA)) / outA;
      return new Color( clamp01( outR), clamp01( outG), clamp01( outB), clamp01( outA));
   }

   private static float clamp01( float aComponent)
   {
      if (aComponent < 0f)
         return 0f;
      if (aComponent > 1f)
         return 1f;
      return aComponent;
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
      
//      aG2.setPaint( BACKGROUND_COLORS[ourPaintCount++ % BACKGROUND_COLORS.length]);
//      aG2.fill( clipRect);
      
      HexGrid hg = getHexGrid();
      Point lowerLeft = hg.hexContaining( new Point2D.Double( clipRect.getMinX(), clipRect.getMinY()));
      lowerLeft.translate( -1, -1);
      Point upperRight = hg.hexContaining( new Point2D.Double( clipRect.getMaxX(), clipRect.getMaxY()));
      upperRight.translate( 1, 1);

//      lowerLeft = new Point(0,0);
//      upperRight = new Point(hg.getNumHorizHexes()-1, hg.getNumVertHexes()-1);
      
      log.trace("painting, clip rect = ({}, {}) --> ({}, {}); hex rect = ({}, {}) --> ({}, {})",
         clipRect.getMinX(), clipRect.getMinY(), clipRect.getMaxX(), clipRect.getMaxY(),
         lowerLeft.x, lowerLeft.y, upperRight.x, upperRight.y);
      
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
      
      int paintedHexCount = 0;
      
      for (int j = lowerLeft.y; j <= upperRight.y; j++)
      {
         for (int i = lowerLeft.x; i <= upperRight.x; i++)
         {
            g2.setPaint( 
               // FOREGROUND_COLORS[(paintedHexCount++) % FOREGROUND_COLORS.length]
               Color.BLACK
                     );
            Hex hex = hexes[i][j];
            Edge e;
            // Unconditionally paint first three edges, since they won't have been painted yet.
            for (int k = 0; k < 3; k++)
            {
               e = hex.getEdge(k);
               g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
            }
            if (i == lowerLeft.x)
            {
               // Hexes at far left of region get their left edge (4th edge) painted.
               e = hex.getEdge(3);
               g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
            }
            if (j == lowerLeft.y)
            {
               // Hexes along bottom edge of region get their bottom edges (5th, 6th edges) painted.
               e = hex.getEdge( 4);
               g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
               e = hex.getEdge( 5);
               g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
            }
            else 
            {
               // Not bottom edge, but still, maybe an edge hex.
               if (i == lowerLeft.x && (j % 2) == 0)
               {
                  // Left end of even-numbered row
                  e = hex.getEdge( 4);
                  g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
               }
               else if (i == upperRight.x && (j % 2) == 1)
               {
                  // Right end of odd-numbered row
                  e = hex.getEdge(5);
                  g2.draw( new Line2D.Double( e.getVertex0(), e.getVertex1()));
               }
            }
            Color hexColor = hex.getBackgroundColor();
            if (hexColor != null)
            {
               Path2D.Double hexPath = new Path2D.Double();
               e = hex.getEdge(0);
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
