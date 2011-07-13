package com.how_hard_can_it_be.hex;

import java.awt.Point;

/**
 * Designates a single hex in a {@link HexCanvas}, as follows:
 * <blockquote>
 * x is the column offset from the leftmost column of hexes <br>
 * y is the hex offset from the topmost hex in the column designated by x.
 * </blockquote>
 * 
 * So, (1,0) is the topmost hex in the 2nd-from-left column, and (0,1) is the 2nd hex 
 * from the top in the leftmost column.
 * 
 * @author John
 *
 */
public class HexPoint extends Point
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public HexPoint()
   {
      super();
   }

   public HexPoint( int x, int y)
   {
      super( x, y);
   }

   public HexPoint( HexPoint aHexPoint)
   {
      this( aHexPoint.x, aHexPoint.y);
   }
}
