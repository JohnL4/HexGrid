package com.how_hard_can_it_be.hexgrid;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A grid of hexes.  This grid is intended to be immutable once set up, but tokens can be placed on it and moved around.
 * 
 * @see <a href="doc-files/coordinate-transformation.pdf">doc-files/coordinate-transformation.pdf</a>
 * @author John.Lusk@how-hard-can-it-be.com
 *
 */
public class HexGrid
{
   /**
    * Dimensions given in x-y coordinate space, in units of hex diameters (from
    * center of edge to center of opposite edge).  For GURPS, hex diameter = 1.0 (yard).
    */
   public HexGrid(int aWidth, int aHeight, double aHexDiameter)
   {
      myNumHorizHexes = (int) Math.ceil( aWidth / aHexDiameter);
      myNumVertHexes = (int) Math.ceil( aHeight / aHexDiameter / Math.sin(Math.PI/3));
      myHexes = new Hex[myNumHorizHexes][myNumVertHexes];
      myHexRadius = aHexDiameter / 2;
      myHexSide = myHexRadius / Math.cos( Math.PI/6);
      
      
//      myH2toR2 = new Matrix(new double[][] {{1.0, 0.5}, {0.0, Math.sqrt( 3.0)/2.0}});
//      myR2toH2 = myH2toR2.inverse();
      
      myH2toR2Xform = new AffineTransform( 1, 0, 0.5, Math.sqrt(3.0)/2.0, 0, 0);
      try
      {
         myR2toH2Xform = myH2toR2Xform.createInverse();
      }
      catch (NoninvertibleTransformException exc)
      {
         // Should never happen (obviously).
         exc.printStackTrace();
      }
      
//      System.out.println( "h2toR2:");
//      myH2toR2.print( new DecimalFormat( "##.####"), 8);
//      System.out.println( "r2toH2:");
//      myR2toH2.print( new DecimalFormat( "##.####"), 8);
      
      setupHexes();
   }

   public String toString()
   {
      String crlf = System.getProperty("line.separator");
      StringBuffer retval = new StringBuffer();
      retval.append( "HexGrid:").append( crlf);
      for (int i = 0; i < myNumHorizHexes; i++)
      {
         for (int j = 0; j < myNumVertHexes; j++)
         {
            retval
               .append( String.format( "Hex (%d, %d): ", i, j))
               .append( myHexes[i][j].toString())
               .append( crlf);
         }
         retval.append( crlf);
      }
      return retval.toString();
   }

   /**
    * Returns the bounding rectangle for this hexgrid.
    * @return
    */
   public Rectangle2D.Double getBounds()
   {
      double minX, minY, maxX, maxY;
      
      minX = myHexes[0][0].getEdge( 2).getVertex1().x;
      minY = myHexes[0][0].getEdge( 4).getVertex0().y; // Runs backwards.
      
      maxX = myHexes[getNumHorizHexes()-1][1].getEdge( 0).getVertex0().x;  // Row 1 sticks out farther to the right than does row 0.
      maxY = myHexes[0][getNumVertHexes()-1].getEdge( 1).getVertex1().y;

      return new Rectangle2D.Double( minX, minY, (maxX - minX), (maxY - minY));
   }
   
   /**
    * The array of hexes.  First dimension is row.
    * @return
    */
   public Hex[][] getHexes()
   {
      return myHexes;
   }

   /**
    * Number of hexes in a single row of hexes array.
    * @return
    */
   public int getNumHorizHexes()
   {
      return myNumHorizHexes;
   }
   
   /**
    * Number of rows in hexes array.
    * @return
    */
   public int getNumVertHexes()
   {
      return myNumVertHexes;
   }
   
   /**
    * Returns the (i,j) index into {@see #myHexes} of the hex containing the given test point.  The (i,j) index 
    * indicates the i-th hex in the j-th row, so it's a little like an (x,y) coordinate.
    * <strong>NOTE</strong> that the returned index may, in fact, be invalid for indexing into {@see #myHexes},
    * which would indicate that the test point is not inside any hex.
    * @param aTestPoint
    * @return
    */
   public Point hexContaining( Point2D.Double aTestPoint)
   {
//      Matrix mat_H2 = myR2toH2.times( new Matrix( new double[] { aTestPoint.x, aTestPoint.y}, 2));
//      Point2D.Double pt_H2 = new Point2D.Double( mat_H2.get(0,0), mat_H2.get(1,0));
      
      // alt: xform
      Point2D testPt_H2 = myR2toH2Xform.transform( aTestPoint, null);
      
      Point candidatePt_H2[] = new Point[]
            {
               new Point( (int) Math.floor( testPt_H2.getX()), (int) Math.floor( testPt_H2.getY())),
               new Point( (int) Math.ceil( testPt_H2.getX()), (int) Math.ceil( testPt_H2.getY())),
               new Point( (int) Math.floor( testPt_H2.getX()), (int) Math.ceil( testPt_H2.getY())),
               new Point( (int) Math.ceil( testPt_H2.getX()), (int) Math.floor( testPt_H2.getY())),
            };
      double rsq[] = new double[4]; // r squared
      for (int i = 0; i < 4; i++)
      {
//         Matrix mat_R2 = myH2toR2.times(  new Matrix( new double[] {candidatePt_H2[i].x, candidatePt_H2[i].y}, 2));
         
         // Point2D.Double cand_R2 = new Point2D.Double( mat_R2.get( 0,0), mat_R2.get( 1,0));

         // alt: xform
         Point2D cand_R2 = myH2toR2Xform.transform( candidatePt_H2[i], null);
         
         double dx, dy;
         dx = aTestPoint.x - cand_R2.getX();
         dy = aTestPoint.y - cand_R2.getY();
         rsq[i] = dx * dx + dy * dy;
      }
      int bestPtIx = 0;
      double bestRadius = rsq[0];
      for (int i = 1; i < 4; i++)
      {
         if (rsq[i] < bestRadius)
         {
            bestRadius = rsq[i];
            bestPtIx = i;
         }
      }
      // At this point, we have the closest hex, but we need to adjust the (u,v) coordinates to myHexes(i,j) coordinates.
      int u = candidatePt_H2[ bestPtIx].x;
      int v = candidatePt_H2[ bestPtIx].y;
      Point retval = new Point( u + v/2, v); // integer division ==> truncation, which is what we want.

      return retval;
   }
   
   private void setupHexes()
   {
      // Build by rows, so i (column, "x" coordinate) will vary faster than j (row, "y" coordinate).
      for (int j = 0; j < myNumVertHexes; j++ )
      {
         for (int i = 0; i < myNumHorizHexes; i++)
         {
            myHexes[i][j] = makeHex(i,j);
         }
      }
   }

   /**
    * Makes i-th hex on row j.  ((i,j) are like (x,y) coordinates.)
    * @param i
    * @param j
    * @return
    */
   private Hex makeHex(int i, int j)
   {
      // System.out.printf( "makeHex( %d, %d)%s", i, j, Const.CRLF);
      Hex newHex;
//      int u,v; // Actual hex coordinates, in H2-space.
//      u = i - j/2;
//      v = j;
      Point2D.Double v0, v1, v2, v3, v4;
      Edge e0, e1, e2, e3;
      
      v0 = new Point2D.Double( 
         ((j % 2) + 2 * i + 1) * myHexRadius,
         (3 * j - 1) * myHexSide / 2);
      v1 = new Point2D.Double(
         ((j % 2) + 2 * i + 1) * myHexRadius,
         (3 * j + 1) * myHexSide / 2);
      v2 = new Point2D.Double(
         ((j % 2) + 2 * i) * myHexRadius,
         (3 * j + 2) * myHexSide / 2);

      if (i == 0)
      {
         // Left edge of map ==> make new edge (new vertices)
         v3 = new Point2D.Double(
            ((j % 2) + 2 * i - 1) * myHexRadius,
            (3 * j + 1) * myHexSide / 2);
         v4 = new Point2D.Double(
            ((j % 2) + 2 * i - 1) * myHexRadius,
            (3 * j - 1) * myHexSide / 2);
         // Edge runs "backwards", from lower point (v4) UP to higher point (v3), 
         // same as it would if it were the first edge of the leftward neighbor hex.
         e3 = new Edge(v4, v3);  
      }
      else
      {
         // There is a hex to the left.  Use its first edge.
         e3 = myHexes[i-1][j].getEdge(0);
         v3 = e3.getVertex1();   // v1 of leftward neighbor's first edge, which was created running "backwards" (v0 to v1).
         v4 = e3.getVertex0();
      }
      
      e0 = new Edge(v0, v1);
      e1 = new Edge(v1, v2);
      e2 = new Edge(v2, v3);
      
//      e0.setEdge1Plus( e1);
//      e1.setEdge0Minus( e0);
//      
//      e1.setEdge1Plus( e2);
//      e2.setEdge0Minus( e1);

      // At this point, we have the first four edges of the new hex. (The fourth edge may be part of a 
      // neighboring hex.)
      // We need to add the other edges, either creating them, if this is an
      // edge hex, or getting them from neighboring hexes.
      
      newHex = new Hex( e0);
      newHex.addEdge( e1).addEdge( e2).addEdge( e3);
      
      // 5th edge
      Point2D.Double v5;
      Edge e4;
      if (((i == 0) && (j % 2) == 0) || (j == 0))
      {
         // Need to make 5th edge; there is no southwest neighbor.
         v5 = new Point2D.Double(
            ((j % 2) + 2 * i) * myHexRadius,
            (3 * j - 2) * myHexSide / 2);
         e4 = new Edge( v5, v4);  // Runs backwards.
      }
      else
      {
         // Obtain 5th edge from southwest neighbor.
         int swNeighborHorizCoord = ((j % 2) == 0 ? i - 1 : i);
         Hex swNeighbor = myHexes[swNeighborHorizCoord][j-1];
         e4 = swNeighbor.getEdge( 1);
         v5 = e4.getVertex0();   // Edge runs backwards.
      }
      newHex.addEdge( e4);
      
      // 6th, and last, edge
      Edge e5;
      if ((j == 0) || ((i == myNumHorizHexes - 1) && (j % 2) == 1))
      {
         // No southeast neighbor; must create edge.
         e5 = new Edge( v0, v5); // Edge Runs backwards.
      }
      else
      {
         // Use southeast neighbor's 3rd edge (edge 2, 0-based)
         int seNeighborHorizCoord = i + (j % 2);   // TODO: this is wrong.  x-offset for SE neighbor is sometimes 0, sometimes 1.  It's (j % 2).
         Hex seNeighbor = myHexes[seNeighborHorizCoord][j-1];
         e5 = seNeighbor.getEdge( 2);
      }
      newHex.addEdge( e5);
      
      return newHex;
   }

   private int myNumHorizHexes, myNumVertHexes;
   /**
    * Distance from the center of a hex to the middle of one of its edges.
    */
   private double myHexRadius;
   /**
    * Length of a side of a hex.
    */
   private double myHexSide;
   private Hex[][] myHexes;
   
   /**
    * Translate from hex space (H2) to Cartesian space (R2) and back again.
    */
//   private Matrix myH2toR2, myR2toH2;  

   /**
    * Change-of-basis vectors, not really graphical transforms.  AffineTransforms should be able to handle this function,
    * though, w/out introducting a dependency on a 3rd-party library for matrix ops.
    */
   private AffineTransform myH2toR2Xform, myR2toH2Xform;
}
