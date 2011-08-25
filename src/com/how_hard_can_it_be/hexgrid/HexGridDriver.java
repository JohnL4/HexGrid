package com.how_hard_can_it_be.hexgrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

public class HexGridDriver
{

   public static void main(String[] argv)
   {
      final HexGrid hexGrid = new HexGrid(10, 10, 1.0);
      Rectangle2D.Double bounds = hexGrid.getBounds();
      System.out.printf( "Hex grid bounds, as vertices: (%7.4g, %7.4g), (%7.4g, %7.4g)%s",
         bounds.x, bounds.y, (bounds.x + bounds.width), (bounds.y + bounds.height),
         Const.CRLF);
//      System.out.println( hexGrid.toString());
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
         public void run() {
             createAndShowGui( hexGrid);
         }
     });
   }
   
   private static void createAndShowGui( HexGrid aHexGrid)
   {
      JFrame topFrame = new JFrame( "Hex Grid");
      topFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);

      ZoomableJPanel zp = new HexGridCanvas( aHexGrid); // new GiantArrowCanvas();
      zp.addMouseListener( new HexColoringMouseListener());
//      ghc.setBorder( new LineBorder( Color.RED, 1));
//      topFrame.add( gac, BorderLayout.CENTER);
      
      JScrollPane sp = new JScrollPane( zp);
      topFrame.add( sp, BorderLayout.CENTER);

      JMenuBar menuBar = makeMenuBar( zp);
      topFrame.setJMenuBar( menuBar);
//
//      hc.addMouseListener( new GridColoringMouseListener());
      
      topFrame.pack();
      topFrame.setVisible( true);
   }

   private static JMenuBar makeMenuBar( ZoomableJPanel aZoomable)
   {
      JMenuBar retval = new JMenuBar();
      retval.setOpaque( true);
      
      // http://colorschemedesigner.com/#0X62hcso8vywV
      
      // retval.setBackground( new Color( 0xc0a776 ));
      
      // retval.setPreferredSize( new Dimension( 200, 20));
      
      JMenu fileMenu = newFileMenu();
      retval.add( fileMenu);
      
      JMenu viewMenu = newViewMenu( aZoomable);
      retval.add( viewMenu);
      
      return retval;
   }

   private static JMenu newFileMenu()
   {
      JMenu retval = new JMenu( "File");
      retval.setMnemonic( KeyEvent.VK_F);
      
      JMenuItem exitMI = new JMenuItem("Exit");
      retval.add( exitMI);
      exitMI.setMnemonic( KeyEvent.VK_X);
      exitMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_X,
         ActionEvent.ALT_MASK));
      exitMI.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent anEvt) { System.exit(0); }
      });
      return retval;
   }
   
   /**
    * Makes and returns a new JMenu encapsulating "view" operations.
    * @return
    */
   private static JMenu newViewMenu( ZoomableJPanel aZoomable)
   {
      JMenu retval = new JMenu("View");
      
      retval.setMnemonic( KeyEvent.VK_V);
      
      Zoomer zoomer = new Zoomer( aZoomable);
       
      JMenuItem zoomInMI = new JMenuItem( Zoomer.LABEL_ZOOM_IN);
      retval.add( zoomInMI);
      zoomInMI.setMnemonic( KeyEvent.VK_I);
      zoomInMI.setAccelerator( 
         KeyStroke.getKeyStroke( KeyEvent.VK_EQUALS, 
            ActionEvent.CTRL_MASK /* | ActionEvent.SHIFT_MASK */));
      
      zoomInMI.addActionListener( zoomer);
       
      JMenuItem zoomOutMI = new JMenuItem( Zoomer.LABEL_ZOOM_OUT);
      retval.add( zoomOutMI);
      zoomOutMI.setMnemonic( KeyEvent.VK_O);
      zoomOutMI.setAccelerator( 
         KeyStroke.getKeyStroke( KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
      zoomOutMI.addActionListener( zoomer);
       
      JMenuItem zoomToMI = new JMenuItem( Zoomer.LABEL_ZOOM_TO);
      retval.add( zoomToMI);
      zoomToMI.setMnemonic( KeyEvent.VK_T);
      zoomToMI.setAccelerator( 
         KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      zoomToMI.addActionListener( zoomer);
      
      return retval;
   }


}
