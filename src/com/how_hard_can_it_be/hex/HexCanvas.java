package com.how_hard_can_it_be.hex;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A hex-based canvas on which to draw, like hex paper.
 * <p>
 * Fun facts about hexes:
 * <ul>
 * <li>Let <var>h</var> = hex height
 * <li><var>r</var>, the distance from hex center to hex vertex,<br>
 * = <var>h</var> / (2 * sin( pi / 3))
 * <li>For a 1-yard hex, <var>r</var> = 0.5774, and the distance from vertex
 * to opposing vertex is 1.1548.
 * <li>For purposes of horizontal division (yards per hex, moving horizontally), a single
 * hex is 0.866 yards.  So, 100 yard x 100 yard map will have 100 hexes in a vertical direction
 * and a little over 115 hexes (staggered) in a horizontal direction.
 * <li>In other words, if point <var>B</var> is directly east of point <var>A</var> by 100 yards,
 * the path from <var>A</var> to <var>B</var> will cross 115 1-yard hexes (115 yards), making <var>B</var>
 * 15% farther from <var>A</var> than it appears to be. 
 * <li>By contrast, on a square grid in which each square is 1 yard across and diagnoal movement
 * is allowed, if point <var>B</var> is 141 yards directly southeast of point <var>A</var>,
 * the path from <var>A</var> to <var>B</var> will cross 100 squares (100 yards, counting by squares),
 * making point <var>B</var> almost 30% closer to point <var>A</var> than it appears to be.
 * </ul>
 * <p>
 * See {@link #drawHexes(Graphics2D)} to know what rh, lh and lh' are.
 * <p>
 * <blockquote>
 * <img src="doc-files/hex-grid.png"/>
 * </blockquote> 
 * @author John
 */
public class HexCanvas extends JPanel implements Scrollable
{
   /**
    * Initial canvas size.
    */
   private static final Dimension SIZE = new Dimension( 2000, 2000);

   /**
    * The initial scale for the canvas. We intend to draw a hex height of 1
    * (yard) magnified by 50, to get (I hope) 50 pixels.
    */
   private static final int INITIAL_PIXELS_PER_HEX = 50;
   
   private static final Font HEX_LABEL_FONT = 
      new Font( Font.SANS_SERIF, Font.ITALIC, 10);
   
   private static final Color HEX_LINE_COLOR = Color.RED;
   
   /**
    * Circle color.
    */
   private static final Color 
      TOP_LEFT_COLOR = new Color( 0.3f, 0.3f, 0.3f),
      BOTTOM_RIGHT_COLOR = new Color( 1.0f, 1.0f, 0.7f);

   /**
    * # of subdivisions into which to divide Canvas. One circle will be
    * painted in the center of each subdivision.
    */
   private static final int N_SUBDIVS = 21;
   
   /**
    * The diameter of a circle as a fraction of the width of its subdivision.
    */
   private static final float CIRCLE_SUBDIV_FRACTION = 0.67f;

   /**
    * Construct with hex height of 1 yard.
    */
   public HexCanvas()
   {
      this( new Dimension( 115, 100), 1.0f);
   }

   /**
    * @param aMapDim
    *           Dimensions of map, in hexes.
    * @param aHexHeight
    *           Height of a single hex (from one flat side to the opposite
    *           flat side) in yards or meters. All other measurements will be
    *           in terms of this figure.
    */
   public HexCanvas( Dimension aMapDim, float aHexHeight)
   {
      _mapDim = aMapDim;
      _hexPaints = new Paint[_mapDim.width][_mapDim.height];
      _hexHeight = aHexHeight;
      initializeVertices();
   }
   
   private void initializeVertices()
   {
      int nHexesVert = _mapDim.height;
      int n = 3 * nHexesVert - 1;  // 2 hexes require 5 vertices on each side.
      if (n % 2 == 1) n++; // Making stepping by 2 easier.

      _vertices = new Point2D.Float[_mapDim.width + 1][ n]; 
      
      // Algorithm:
      // Proceed by columns.  First, establish two columns of vertices.
      // The lefthand column contains coordinates (in pixels) of the 3 vertices
      // on the left eadge of each hex in a column.  The righthand column
      // contains coordinates for the right three vertices.
      // Given two columns of vertices in vertical arrays lh and rh, draw lines as follows:
      //
      // Part A:
      //
      // lh[0] --> rh[0]  \___ top row of hexes
      // lh[0] --> lh[1]  /
      // lh[2] --> lh[1]  \
      // lh[2] --> rh[2]   >-- interior rows of hexes  
      // lh[2] --> lh[3]  /
      // lh[4] --> lh[3]  \___ bottom row of hexes
      // lh[4] --> rh[4]  /
      //
      // Part B:
      //
      // Then, drop the first (lefthand) array, make the righthand array the new
      // lefthand array and compute a new righthand array containing the right-side
      // vertices of the 2nd column of hexes.  Connect the two arrays with lines
      // as follows:
      //
      // lh'[1] --> lh'[0]  \
      // lh'[1] --> rh'[1]   >-- as for interior rows
      // lh'[1] --> lh'[2]  /
      // lh'[3] --> lh'[2]  \
      // lh'[3] --> rh'[3]   >-- as for interior rows
      // lh'[3] --> lh'[4]  /
      //
      // Then, drop the lefthand array (lh'), make the righthand array (rh')
      // the next lefthand array, compute a new righthand array, and proceed
      // with part (A) again.
      //
      // Vertex computations proceed as follows, using h for hex height and r
      // for hex "radius" (center to vertex distance):
      //
      // lh[0] = (r/2, 0)
      // lh[1] = (0, h/2)
      // lh[2] = (r/2, h)
      // lh[3] = (0, 3h/2)
      // lh[4] = (r/2, 2h)
      // etc.
      // rh[0] = (3r/2, 0)
      // rh[1] = (2r, h/2)
      // rh[2] = (3r/2, h)
      // rh[3] = (2r, 3h/2)
      // rh[4] = (3r/2, 2h)
      // etc.
      // rh'[0] = (7r/2, 0)
      // rh'[1] = (3r, h/2)
      // rh'[2] = (7r/2, h)
      // rh'[3] = (3r, 3h/2)
      // rh'[4] = (7r/2, 2h)
      // etc.
      // We proceed by multiples of r/2 and h/2.

      _hexHeightInPix = _hexHeight * INITIAL_PIXELS_PER_HEX; 
      float h = _hexHeightInPix;
      float h2 = h / 2;
      _hexRadiusInPix = (float) (h / (2 * Math.sin( Math.PI/3)));
      float r = _hexRadiusInPix;
      float r2 = r / 2;

      Point2D.Float[] lh;
      Point2D.Float[] rh;  // rh col WILL have bottom-row hexes.
      Point2D.Float[] rhp; // rhp = rh'

      int nColumnPairs = _mapDim.width / 2;

      for (int cp = 0; cp < nColumnPairs; cp++)
      {
         lh = _vertices[cp * 2];
         rh = _vertices[cp * 2 + 1];
         rhp = _vertices[(cp + 1) * 2];

         // Do two points in each column at a time, since they're staggered
         // from each other. 

         for (int i = 0; i < n; i+=2)
         {
            if (cp == 0)
            {
               lh[ i] = new Point2D.Float( r2 + cp * 3 * r, i * h2);
               lh[ i + 1] = new Point2D.Float( cp * 3 * r, (i + 1) * h2);
            }
            rh[ i] = new Point2D.Float( 3 * r2 + cp * 3 * r, i * h2);
            rh[ i + 1] = new Point2D.Float( 2 * r + cp * 3 * r, (i + 1) * h2);
            rhp[ i] = new Point2D.Float( 7 * r2 + cp * 3 * r, i * h2);
            rhp[ i + 1] = new Point2D.Float( 3 * r + cp * 3 * r,
               (i + 1) * h2);
         }
      }
   }
   
   public Dimension getPreferredSize()
   {
      Dimension retval;
      retval = new Dimension( 
         (int) (SIZE.width * _zoomFactor ), (int) (SIZE.height * _zoomFactor ));
      return retval;
   }
   
   public void paintComponent( Graphics aG)
   {
      super.paintComponent( aG);

      Graphics2D g2 = (Graphics2D) aG;
      
      AffineTransform xform = g2.getTransform();
      g2.scale( _zoomFactor, _zoomFactor);

      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
    
      drawHexes( g2);
      paintHexes( g2);
      
//      drawCircles( g2);
//      drawAxes( g2);
      
      drawHighlightShapes( g2);
      
      g2.setTransform( xform);
   }

   private void drawHighlightShapes( Graphics2D g2)
   {
      g2.setStroke( new BasicStroke( 2.0f));
      g2.setPaint( Color.MAGENTA);
      
      for ( Shape shape : _highlightShapes)
      {
         g2.draw( shape);
      }
   }

   private void drawHexes( Graphics2D ag2)
   {
      int nColumnPairs = _mapDim.width / 2;
      Point2D.Float[] lh;
      Point2D.Float[] rh;  // rh col WILL have bottom-row hexes.
      Point2D.Float[] rhp; // rhp = rh'

      int nHexesVert = _mapDim.height;
      int n = 3 * nHexesVert - 1;  // 2 hexes require 5 vertices on each side.
      if (n % 2 == 1) n++; // Making stepping by 2 easier.

      for (int cp = 0; cp < nColumnPairs; cp++)
      {
         lh = _vertices[cp * 2];
         rh = _vertices[cp * 2 + 1];
         rhp = _vertices[(cp + 1) * 2];
         Point2D.Float[] lh2d, rh2d; // left-, right-hand vertices to draw.
         lh2d = lh;
         rh2d = rh;
         for (int k = 0; k < 2; k++) // for each column in pair (k is column in pair)...
         {
            for (int i = k; i < n; i += 2)
            {
               Line2D ln;
               if (i > 0)
               {
                  ln = new Line2D.Float( lh2d[ i], lh2d[ i - 1]);
                  ag2.draw( ln);
               }
               ln = new Line2D.Float( lh2d[ i], rh2d[ i]);
               ag2.draw( ln);
               
//               String hexLabel = "" 
//                  + ((i - k + 1) * pow10 + (2 * columnPair + k));
//               Rectangle2D labelBounds = 
//                  HEX_LABEL_FONT.getStringBounds( hexLabel, frc);
//               Point2D.Float labelStart = new Point2D.Float();
//               labelStart.x = (float) (lh2d[i].x
//                     + (rh2d[i].x - lh2d[i].x - labelBounds.getWidth()) / 2.0f);
//               
//               labelStart.y = (float) (lh2d[i].y + labelBounds.getHeight());
//               ag2.drawString( hexLabel, labelStart.x, labelStart.y);
               
               if (i < n - 1)
               {
                  ln = new Line2D.Float( lh2d[ i], lh2d[ i + 1]);
                  ag2.draw( ln);
               }
            }
            lh2d = rh;
            rh2d = rhp;
         }
      }
   }
   
   private void paintHexes( Graphics2D ag2)
   {
      ag2.setStroke( new BasicStroke( 1.0f));
      
      for (int x = 0; x < _mapDim.width; x++)
      {
         for (int y = 0; y < _mapDim.height; y++)
         {
            if (null == _hexPaints[x] || null == _hexPaints[x][y])
               ;
            else
            {
               ag2.setPaint( _hexPaints[x][y]);
               Shape hex = hexShapeAtHexPoint( new HexPoint(x,y));
               ag2.fill( hex);
               ag2.setPaint( HEX_LINE_COLOR);
               ag2.draw( hex);
            }
         }
      }
   }
   
   /**
    * Draw the colored circles.
    * @param aG
    */
   private void drawCircles( Graphics2D aG2)
   {
      aG2.setStroke( new BasicStroke( 2.0f));
      
      Dimension subdivDim = new Dimension();
      subdivDim.width = getWidth() / N_SUBDIVS;
      subdivDim.height = getHeight() / N_SUBDIVS;

      Dimension ovalSize = new Dimension();
      ovalSize.width = (int) (getSize().width / N_SUBDIVS * CIRCLE_SUBDIV_FRACTION);
      ovalSize.height = (int) (getSize().height / N_SUBDIVS * CIRCLE_SUBDIV_FRACTION);
      
      int deltaRed = 
         (BOTTOM_RIGHT_COLOR.getRed() - TOP_LEFT_COLOR.getRed()) / (N_SUBDIVS - 1);
      int deltaGreen = 
         (BOTTOM_RIGHT_COLOR.getGreen() - TOP_LEFT_COLOR.getGreen()) / (N_SUBDIVS - 1);
      int deltaBlue =
         (BOTTOM_RIGHT_COLOR.getBlue() - TOP_LEFT_COLOR.getBlue()) / (2 * N_SUBDIVS - 1);
      
      for (int i = 0; i < N_SUBDIVS; i++)
      {
         for (int j = 0; j < N_SUBDIVS; j++)
         {
            Point topLeft = new Point(); // top left corner of oval.
            topLeft.x = (int) (subdivDim.width * (1 - CIRCLE_SUBDIV_FRACTION) / 2 + i * subdivDim.width);
            topLeft.y = (int) (subdivDim.height * (1 - CIRCLE_SUBDIV_FRACTION) / 2 + j * subdivDim.height);
            // aG2.drawOval( topLeft.x, topLeft.y, ovalSize.width, ovalSize.height);

            Ellipse2D.Float ellipse = new Ellipse2D.Float( 
               topLeft.x,
               topLeft.y,
               ovalSize.width,
               ovalSize.height);

            int red, green, blue;
            if (i < N_SUBDIVS - 1)
               red = TOP_LEFT_COLOR.getRed() + i * deltaRed;
            else
               red = BOTTOM_RIGHT_COLOR.getRed();
            
            if (j < N_SUBDIVS - 1)
               green = TOP_LEFT_COLOR.getGreen() + j * deltaGreen;
            else
               green = BOTTOM_RIGHT_COLOR.getGreen();

            if ((i + j) < 2 * N_SUBDIVS - 1)
               blue = TOP_LEFT_COLOR.getBlue() + (i + j) * deltaBlue;
            else
               blue = BOTTOM_RIGHT_COLOR.getBlue();

            // First fill, then stroke outline.
            Color fillColor = new Color( red, green, blue);
            aG2.setPaint( fillColor);
            aG2.fill( ellipse);

            aG2.setPaint( Color.BLACK);
            aG2.draw( ellipse);
         }
      }
   }
   
   /**
    * Draws the x- and y-axes.
    * 
    * @param aG
    */
   private void drawAxes( Graphics2D aG2)
   {
      aG2.setStroke( new BasicStroke( 4.0f));
      aG2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
      Line2D.Float xAxis = new Line2D.Float(0, -getHeight(), 0, getHeight());
      Line2D.Float yAxis = new Line2D.Float( -getWidth(), 0, getWidth(), 0);
      aG2.draw( xAxis);
      aG2.draw( yAxis);
      
      // x-axis ticks
      for (int i = -20; i < 21; i++)
      {
         int tickSize = getHeight()/20;
         int x = i * getWidth()/20;
         Line2D.Float tickMark = new Line2D.Float( x, -tickSize, x, tickSize);
         aG2.draw( tickMark);
      }
      // y-axis ticks
      for (int i = -20; i < 21; i++)
      {
         int tickSize = getWidth()/20;
         int y = i * getHeight()/20;
         Line2D.Float tickMark = new Line2D.Float( -tickSize, y, tickSize, y);
         aG2.draw( tickMark);
      }
   }
   
   /**
    * Returns the hex containing the given point.
    * 
    * @param aPoint
    * @return
    */
   public HexPoint hexAtXY( Point aPoint)
   {
      float x = aPoint.x / _zoomFactor;
      float y = aPoint.y / _zoomFactor;
      Point2D.Float clickPointScaled = new Point2D.Float( x, y);
      
      float h = _hexHeightInPix;
      float r = _hexRadiusInPix;
      
      // Width of column pair at widest point.
      float colPairWidth = 3 * r;
      int colPair;

      if (x < r/2)
         colPair = 0;
      else
         colPair = (int) Math.floor( (x - r/2) / colPairWidth);

      float rowPairHeight = 2.0f * h;
      
      int rowPair;
      if (y < h/2)
         rowPair = 0;
      else
         rowPair = (int) Math.floor( (y - h/2) / rowPairHeight);

      highlightClickBound( h, r, colPairWidth, colPair, rowPairHeight,
         rowPair);
      
       Rectangle2D.Float clickBound = 
          colRowPairBound( h, r, colPairWidth, colPair, rowPairHeight, rowPair);
      
       HexPoint[] candidateHexes;
       HexPoint hp = new HexPoint( colPair * 2, rowPair * 2);
       
       float xOffset = clickPointScaled.x - clickBound.x;
       if (xOffset < r)
       {
          // Must be in first col (3 hexes)
           candidateHexes = new HexPoint[] { 
              hp,
              new HexPoint( hp.x, hp.y + 1),
              new HexPoint( hp.x, hp.y + 2)
          };
       }
       else if (1.5f * r < xOffset && xOffset < 2.5f * r)
       {
          // Must be in 2nd col (2 hexes)
         candidateHexes = new HexPoint[] {
             new HexPoint( hp.x + 1, hp.y),
             new HexPoint( hp.x + 1, hp.y + 1)
         };
       }
       else if (xOffset < 1.5f * r)
       {
          // Must be in 1st or 2nd columns
          candidateHexes = new HexPoint[] {
              hp,
              new HexPoint( hp.x, hp.y + 1),
              new HexPoint( hp.x, hp.y + 2),
              new HexPoint( hp.x + 1, hp.y),
              new HexPoint( hp.x + 1, hp.y + 1)
          };
       }
       else
       {
          // Must be in 2nd or 3rd columns
          candidateHexes = new HexPoint[] {
              new HexPoint( hp.x + 1, hp.y),
              new HexPoint( hp.x + 1, hp.y + 1),
              new HexPoint( hp.x + 2, hp.y),
              new HexPoint( hp.x + 2, hp.y + 1),
              new HexPoint( hp.x + 2, hp.y + 2)
          };
       }
       
      HexPoint retval = null;
      
      for (HexPoint hpi : candidateHexes) // HexPoint Iterator
      {
         Shape s = hexShapeAtHexPoint( hpi);
         if (s.contains( clickPointScaled))
         {
            retval = hpi;
            break;
         }
      }
      if (retval == null) retval = hp;
      
      fillHex( retval, Color.YELLOW);
      
      return retval;
   }

   /**
    * Fills the indicated hex w/the indicated Paint (which could be a Color).
    * 
    * @param aHexPoint
    *           Offset from top left hex, which is (0,0). (1,0) is the top hex
    *           in the 2nd column from the left, and (0,1) is the 2nd hex in
    *           the leftmost column.
    * @param aPaint
    */
   public void fillHex( HexPoint aHexPoint, Paint aPaint)
   {
      Shape hex = hexShapeAtHexPoint( aHexPoint);
      _hexPaints[ aHexPoint.x][ aHexPoint.y] = aPaint;
      
      repaint( hex.getBounds());
   }

   /**
    * Returns a hex Shape corresponding to the given hex point.
    * @param aHexPoint
    */
   private Shape hexShapeAtHexPoint( Point aHexPoint)
   {
      Path2D.Float hex;
      
      Point2D.Float[] lh = _vertices[ aHexPoint.x];
      Point2D.Float[] rh = _vertices[ aHexPoint.x + 1];
      
      // Index of top left vertex of hex. "% 2" to account for staggered hexes
      // in alternating columns.
      int i = aHexPoint.y * 2 + aHexPoint.x % 2;
      
      hex = new Path2D.Float( Path2D.WIND_NON_ZERO, 5);
      hex.moveTo( lh[ i].x, lh[i].y);
      hex.lineTo( rh[i].x, rh[i].y);
      hex.lineTo( rh[i+1].x, rh[i+1].y);
      hex.lineTo( rh[i+2].x, rh[i+2].y);
      hex.lineTo( lh[i+2].x, lh[i+2].y);
      hex.lineTo( lh[i+1].x, lh[i+1].y);
      hex.closePath();
      
      return hex;
   }

   /**
    * Temporary method while writing getHexAtXY().
    * @param h
    * @param r
    * @param colPairWidth
    * @param colPair
    * @param rowPairHeight
    * @param rowPair
    */
   private void highlightClickBound( float h, float r, float colPairWidth,
      int colPair, float rowPairHeight, int rowPair)
   {
      Rectangle2D.Float hitBoundingBox = 
         colRowPairBound( h, r, colPairWidth, colPair, rowPairHeight, rowPair);
      
      Rectangle2D.Float prevHighlight = (Rectangle2D.Float) popHighlightShape();
      if (prevHighlight == null)
         ;
      else
         repaint( 
            (int) ((prevHighlight.x) * _zoomFactor),
            (int) ((prevHighlight.y) * _zoomFactor),
            (int) ((prevHighlight.width) * _zoomFactor),
            (int) ((prevHighlight.height) * _zoomFactor));
      
      pushHighlightShape( hitBoundingBox);
      
      repaint( 
         (int) ((hitBoundingBox.x)  * _zoomFactor), 
         (int) ((hitBoundingBox.y)  * _zoomFactor), 
         (int) ((hitBoundingBox.width)  * _zoomFactor), 
         (int) ((hitBoundingBox.height)  * _zoomFactor));
   }

   /**
    * @param h
    * @param r
    * @param colPairWidth
    * @param colPair
    * @param rowPairHeight
    * @param rowPair
    * @return
    */
   private Rectangle2D.Float colRowPairBound( float h, float r, float colPairWidth,
      int colPair, float rowPairHeight, int rowPair)
   {
      return new Rectangle2D.Float(
         colPair == 0 ? 0 : r / 2 + colPair * colPairWidth, 
         rowPair == 0 ? 0 : h / 2 + rowPair * rowPairHeight, 
         colPairWidth + (colPair == 0 ? r/2 : 0), 
         rowPairHeight + (rowPair == 0 ? h/2 : 0));
   }

   /**
    * Adds the given shape to a list of shapes to be drawn in some sort of
    * "highlight" style.
    * 
    * @param aShape
    */
   public void pushHighlightShape( Shape aShape)
   {
      _highlightShapes.add( aShape);
   }

   /**
    * Pops most recently pushed highlight shape from list and returns it. If
    * list is empty, has no effect.
    * 
    * @return
    */
   public Shape popHighlightShape()
   {
      Shape retval;
      if (_highlightShapes.size() == 0)
         retval = null;
      else
      {
         int i = _highlightShapes.size() - 1;
         retval = _highlightShapes.get( i);
         _highlightShapes.remove(i);
      }
      return retval;
      
   }


   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Dimension getPreferredScrollableViewportSize()
   {
      Dimension retval = new Dimension( 
         (int) (500 /* * _zoomFactor */), (int) (500 /* * _zoomFactor */));
      return retval;
   }

   @Override
   public int getScrollableBlockIncrement( Rectangle aVisibleRect, int anOrientation, int aDirection)
   {
      int retval;
      switch (anOrientation)
      {
         case SwingConstants.HORIZONTAL:
            retval = (int) (0.9 * aVisibleRect.width);
            break;
         case SwingConstants.VERTICAL:
            retval = (int) (0.9 * aVisibleRect.height);
            break;
         default:
            throw new IllegalArgumentException( "Unexpected orientation: " + anOrientation);
      }
      return retval;
   }

   @Override
   public boolean getScrollableTracksViewportHeight()
   {
      return false;
   }

   @Override
   public boolean getScrollableTracksViewportWidth()
   {
      return false;
   }

   @Override
   public int getScrollableUnitIncrement( Rectangle aVisibleRect, int anOrientation, int aDirection)
   {
      int retval;
      switch (anOrientation)
      {
         case SwingConstants.HORIZONTAL:
            retval = (getWidth() - aVisibleRect.width) / 30;
            break;
         case SwingConstants.VERTICAL:
            retval = (getHeight() - aVisibleRect.height) / 30;
            break;
         default:
            throw new IllegalArgumentException( "Unexpected orientation: " + anOrientation);
      }
      return retval;
   }

   /**
    * The factor by which to zoom this canvas when drawing it (1.0 == no zoom).
    * @return
    */
   public float getZoomFactor()
   {
      return _zoomFactor;
   }

   public void setZoomFactor( float zoomFactor)
   {
      _zoomFactor = zoomFactor;
   }

   private float _zoomFactor = 1.0f;

   /**
    * Height of a single hex in yards (or meters, if you want to be metric).
    */
   private float _hexHeight;
   
   /**
    * The dimensions of the map, in hexes.
    */
   private Dimension _mapDim;
   
   /**
    * Vertices, by column (major index selects column; minor, n-th vertex in column.
    */
   Point2D.Float _vertices[][];
   
   /**
    * Height of a single hex in pixels.
    */
   float _hexHeightInPix;
   
   /**
    * Distance from hex center to a vertex.
    */
   float _hexRadiusInPix;
   
   List<Shape> _highlightShapes = new LinkedList<Shape>();

   /**
    * The paints with which to fill each hex.  May be null.  Coordinates are
    * the same as for {@link HexPoint}.
    */
   private Paint[][] _hexPaints;

}
