package com.how_hard_can_it_be.hexgrid;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.text.DecimalFormat;

/**
 * A single hex.
 * @author John.Lusk@how-hard-can-it-be.com
 *
 */
public class Hex
{
   public Hex( Edge aFirstEdge)
   {
      // System.out.printf("new Hex( %s)%s", aFirstEdge.toString(), Const.CRLF);
      myEdge[0] = aFirstEdge;
      myHighestCachedEdge = 0;
   }
   
   /**
    * Returns the i-th edge of this hex, where edge 0 is the first edge (the
    * right, vertical edge) and edges 1-5 are obtained by preceding in a
    * counterclockwise direction.
    * @param i
    * @return
    */
   public Edge getEdge(int i)
   {
      Edge retval;
      if (i < 0 || i > 5)
         throw new IllegalArgumentException( 
            "Requested hex edge must be in range [0,5], was " + i);
      else if (myEdge[i] != null)
      {
         retval = myEdge[i];
      }
      else
      {
         Edge cur;
         boolean enteredViaE0minus; // True iff we entered cur from prev via cur's e0minus edge.
         cur = getEdge( i - 1);  // Crazy recursion to handle caching.
         enteredViaE0minus = enteredViaVertex0( i-1); // enteredViaEdge0Minus( i-1);
         retval = (enteredViaE0minus ? cur.getEdge1Plus() : cur.getEdge0Plus());
         myEdge[i] = retval;
         if (i > myHighestCachedEdge)
            myHighestCachedEdge = i;
      }
      return retval;
   }
   
   /**
    * Used to build a hex's "list" of edges.  Appends the given edge to the 
    * hex's "list" of edges, meaning: starting with the last entry in {@see #myEdge}, walks the list of edges in a
    * counterclockwise direction until the list runs out.  Then adds the given edge as the next edge in the 
    * counterclockwise direction from the last edge found.
    * @param anEdge
    * @return This hex, for chained calls.
    */
   public Hex addEdge( Edge anEdge)
   {
//      System.out.printf( "\tAdding edge %s\tto hex %s%s", 
//         anEdge.toString(), 
//         this.getClass().getName() + "@" + String.format("%x", this.hashCode()), 
//         Const.CRLF);
      Edge prev = myEdge[myHighestCachedEdge];
      Edge cur;
      boolean enteredViaV0;
      boolean newEdgeIsBackwards = false;
      if (myHighestCachedEdge < 1)
         enteredViaV0 = true;
      else
         enteredViaV0 = enteredViaVertex0( myHighestCachedEdge); // enteredViaEdge0Minus( myHighestCachedEdge);
      do
      {
         if (enteredViaV0)
            cur = prev.getEdge1Plus();
         else
            cur = prev.getEdge0Plus();
         if (cur == null)
         {
            // Found end of list.  Need to know how new edge relates.
            newEdgeIsBackwards = (anEdge.getVertex1().equals( enteredViaV0 ? prev.getVertex1() : prev.getVertex0()));
         }
         else
         {
            myEdge[++myHighestCachedEdge] = cur;   // Found a hex edge that wasn't in the cached array, so add it.
            enteredViaV0 = (prev == cur.getEdge0Minus());
            prev = cur;
         }
      } while (cur != null);
      // At this point, prev is last reachable edge in circuit and cur is null. enteredViaE0minus is a statement about
      // how we got into prev.
      if (enteredViaV0)
      {
         prev.setEdge1Plus( anEdge);
         // anEdge.setEdge0Minus( prev); // This is wrong:  we don't know if this edge runs backwards or not.  We only know the prev edge does NOT run backwards.
      }
      else
      {
         prev.setEdge0Plus( anEdge);
         // anEdge.setEdge1Minus( prev);
      }
      if (newEdgeIsBackwards)
         anEdge.setEdge1Minus( prev);
      else
         anEdge.setEdge0Minus( prev);
      myEdge[++myHighestCachedEdge] = anEdge;
      
      // TODO: consider moving hex-closing code from HexGrid.makeHex() to here.
      if (myHighestCachedEdge == 5)
      {
         // Last edge, close hex edge cycle.
         myEdge[5].setEdge0Plus( myEdge[0]);   // Edge runs backwards.
         myEdge[0].setEdge0Minus( myEdge[5]);
      }
      
      return this;
   }
   
   /**
    * Sets the background color of this hex.  Color may be null, in which case hex will have no background.
    * @param aColor
    */
   public void setBackgroundColor( Color aColor)
   {
      myBackgroundColor = aColor;
   }
   
   public Color getBackgroundColor()
   {
      return myBackgroundColor;
   }

   /**
    * Returns the bounding box containing this hex.
    * @return
    */
   public Rectangle2D.Double getBounds2D()
   {
      double minX, minY, maxX, maxY;
      maxX = getEdge(0).getVertex0().x;
      maxY = getEdge(2).getVertex0().y;
      minX = getEdge(2).getVertex1().x;
      minY = getEdge(4).getVertex0().y;
      Rectangle2D.Double retval = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
      return retval;
   }

   /**
    * For debugging.
    */
   public String toString()
   {
      StringBuffer retval = new StringBuffer( 6 * 16 + 5 + 10);
      Point2D.Double v[] = new Point2D.Double[6];
      v[0] = myEdge[0].getVertex0();
      for (int i = 1; i < 6; i++)
      {
         if (enteredViaVertex0( i)) // enteredViaEdge0Minus( i))
            v[i] = myEdge[i].getVertex0();
         else
            v[i] = myEdge[i].getVertex1();
      }
      for (int i = 0; i < 6; i++)
      {
         if (i > 0)
            retval.append(" ");
         retval.append( String.format("(%7.4g, %7.4g)", v[i].x, v[i].y));
      }
      return retval.toString();
   }
   
   /**
    * Returns true iff the indicated edge (in {@see #myEdge}) is entered via its e0minus neighbor edge when traversing
    * edges of this hex in counterclockwise order. Requires {@see #myEdge} to have a value at given index.
    * @param anEdgeIndex Must be in range [1..5].
    * @return
    */
   private boolean enteredViaEdge0Minus( int anEdgeIndex)
   {
      if (anEdgeIndex < 1 || anEdgeIndex > 5)
         throw new IllegalArgumentException( "Edge index must be in range [1..5], was " + anEdgeIndex);
      else
      {
         boolean retval;
         Edge cur = myEdge[anEdgeIndex];
         Edge prev = myEdge[anEdgeIndex-1];
         retval = (prev == cur.getEdge0Minus());
         return retval;
      }
   }
   
   /**
    * Returns true iff the indicated edge (in {@see #myEdge}) is entered via its vertex 0 when traversing
    * edges of this hex in counterclockwise order. Requires {@see #myEdge} to have a value at given index.
    * @param anEdgeIndex Must be in range [1..5]
    * @return
    */
   private boolean enteredViaVertex0( int anEdgeIndex)
   {
      if (anEdgeIndex < 1 || anEdgeIndex > 5)
         throw new IllegalArgumentException( "Edge index must be in range [1..5], was " + anEdgeIndex);
      else
      {
         boolean retval;
         Edge cur = myEdge[anEdgeIndex];
         Edge prev = myEdge[anEdgeIndex-1];
         retval = (cur == prev.getEdge1Plus() && cur.getVertex0().equals( prev.getVertex1()))
               || (cur == prev.getEdge0Plus() && cur.getVertex0().equals( prev.getVertex0()));
         return retval;
      }      
   }
   
   /**
    * Memoized results of {@see #getEdge(int)}, but also myEdge[0] is first edge.
    */
   private Edge[] myEdge = new Edge[6];
   
   /**
    * Index of highest known edge in {@see #myEdge}.
    */
   private int myHighestCachedEdge;
   
   private Color myBackgroundColor;
}
