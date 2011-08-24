package com.how_hard_can_it_be.hexgrid;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * A winged edge of a hex.
 * @author John.Lusk@how-hard-can-it-be.com
 *
 */
public class Edge
{
   /**
    * Index into the 2nd dimension of {@see #myEdge}.
    */
   public static final int 
      COUNTERCLOCKWISE = 0, 
      CLOCKWISE = 1; 

   /**
    * Index into {@see #myface}.
    */
   public static final int
      LEFT = 0,
      RIGHT = 1;
   
   public Edge( Point2D.Double v0, Point2D.Double v1)
   {
      this(v0, v1, null, null, null, null, null, null);
   }
   
   /**
    * Creates a new edge running from vertex v0 to v1.
    * @param v0
    * @param v1
    * @param f0 Left face, the face that lies to the left while travelling
    *    from v0 to v1.
    * @param f1 Right face
    * @param e0plus Next edge from v0 which is farther in the 
    *    counterclockwise (+) direction than edge e0minus.
    * @param e0minus Next edge from v0 which is farther in the clockwise (-)
    *    direction than e0plus.
    * @param e1plus Next edge from v1.
    * @param e1minus Next edge from v1.
    */
   public Edge( Point2D.Double v0, Point2D.Double v1,
      Hex f0, Hex f1,
      Edge e0plus, Edge e0minus, 
      Edge e1plus, Edge e1minus)
   {
      myVertex[0] = v0;
      myVertex[1] = v1;
      myFace[LEFT] = f0;
      myFace[RIGHT] = f1;
      myEdge[0][COUNTERCLOCKWISE] = e0plus;
      myEdge[0][CLOCKWISE] = e0minus;
      myEdge[1][COUNTERCLOCKWISE] = e1plus;
      myEdge[1][CLOCKWISE] = e1minus;
   }

   public String toString()
   {
      return String.format( "%s\t[(%7.4g, %7.4g) --> (%7.4g, %7.4g)]",
         super.toString(), getVertex0().x, getVertex0().y, getVertex1().x, getVertex1().y);
   }
   
   public void setLeftFace( Hex aFace)
   {
      if (myFace[LEFT] == null)
      myFace[LEFT] = aFace;
      else 
         throw new IllegalStateException( 
            "Attempt to set face when one is already set");
   }
   
   public void setRightFace( Hex aFace)
   {
      if (myFace[RIGHT] == null)
      myFace[RIGHT] = aFace;      
      else 
         throw new IllegalStateException( 
            "Attempt to set face when one is already set");
   }
   
   public void setEdge0Plus( Edge anEdge)
   {
      if (myEdge[0][COUNTERCLOCKWISE] == null)
      myEdge[0][COUNTERCLOCKWISE] = anEdge;
      else 
         throw new IllegalStateException( 
            "Attempt to set edge when one is already set");
   }
   
   public void setEdge0Minus( Edge anEdge)
   {
      if (myEdge[0][CLOCKWISE] == null)
      myEdge[0][CLOCKWISE] = anEdge;
      else 
         throw new IllegalStateException( 
            "Attempt to set edge when one is already set");
   }
   
   public void setEdge1Plus( Edge anEdge)
   {
      if (myEdge[1][COUNTERCLOCKWISE] == null)
      myEdge[1][COUNTERCLOCKWISE] = anEdge;
      else 
         throw new IllegalStateException( 
            "Attempt to set edge when one is already set");
   }
   
   public void setEdge1Minus( Edge anEdge)
   {
      if (myEdge[1][CLOCKWISE] == null)
      myEdge[1][CLOCKWISE] = anEdge;
      else 
         throw new IllegalStateException( 
            "Attempt to set edge when one is already set");
   }

   public Point2D.Double getVertex0()
   {
      return myVertex[0];
   }
   
   public Point2D.Double getVertex1()
   {
      return myVertex[1];
   }
   
   public Hex getLeftFace()
   {
      return myFace[LEFT];
   }
   
   public Hex getRightFace()
   {
      return myFace[RIGHT];      
   }
   
   public Edge getEdge0Plus( )
   {
      return myEdge[0][COUNTERCLOCKWISE];
   }
   
   public Edge getEdge0Minus( )
   {
      return myEdge[0][CLOCKWISE];
   }
   
   public Edge getEdge1Plus()
   {
      return myEdge[1][COUNTERCLOCKWISE];
   }
   
   public Edge getEdge1Minus()
   {
      return myEdge[1][CLOCKWISE];
   }
   
   private Point2D.Double[] myVertex = new Point2D.Double[2];
   private Hex[] myFace = new Hex[2];
   private Edge[][] myEdge = new Edge[2][2];
}
