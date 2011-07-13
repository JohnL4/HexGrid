package com.how_hard_can_it_be;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

import com.how_hard_can_it_be.hex.GridColoringMouseListener;
import com.how_hard_can_it_be.hex.HexCanvas;
import com.how_hard_can_it_be.hex.Zoomer;

public class HexGrid
{
   public static void main(String[] argv)
   {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
         public void run() {
             createAndShowGui();
         }
     });
   }
   
   private static void createAndShowGui()
   {
      JFrame topFrame = new JFrame( "Hex Grid");
      topFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
      
      HexCanvas hc = new HexCanvas();
//      hc.setBorder( new LineBorder( Color.RED, 4));
      hc.setBorder( new LineBorder( Color.RED, 1));
      
      JScrollPane sp = new JScrollPane( hc);
      topFrame.add( sp, BorderLayout.CENTER);

      JMenuBar menuBar = makeMenuBar( hc);
      topFrame.setJMenuBar( menuBar);

      hc.addMouseListener( new GridColoringMouseListener());
      
      topFrame.pack();
      topFrame.setVisible( true);
   }

   private static JMenuBar makeMenuBar( HexCanvas aHexCanvas)
   {
      JMenuBar retval = new JMenuBar();
      retval.setOpaque( true);
      
      // http://colorschemedesigner.com/#0X62hcso8vywV
      
      // retval.setBackground( new Color( 0xc0a776 ));
      
      // retval.setPreferredSize( new Dimension( 200, 20));
      
      JMenu viewMenu = new JMenu("View");
      retval.add( viewMenu);
      
      viewMenu.setMnemonic( KeyEvent.VK_V);
      
      Zoomer zoomer = new Zoomer( aHexCanvas);
      
      JMenuItem zoomInMI = new JMenuItem( Zoomer.LABEL_ZOOM_IN);
      viewMenu.add( zoomInMI);
      zoomInMI.setMnemonic( KeyEvent.VK_I);
      zoomInMI.setAccelerator( 
         KeyStroke.getKeyStroke( KeyEvent.VK_EQUALS, 
            ActionEvent.CTRL_MASK /* | ActionEvent.SHIFT_MASK */));
      
      zoomInMI.addActionListener( zoomer);
      
      JMenuItem zoomOutMI = new JMenuItem( Zoomer.LABEL_ZOOM_OUT);
      viewMenu.add( zoomOutMI);
      zoomOutMI.setMnemonic( KeyEvent.VK_O);
      zoomOutMI.setAccelerator( 
         KeyStroke.getKeyStroke( KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
      zoomOutMI.addActionListener( zoomer);
      
      JMenuItem zoomToMI = new JMenuItem( Zoomer.LABEL_ZOOM_TO);
      viewMenu.add( zoomToMI);
      zoomToMI.setMnemonic( KeyEvent.VK_T);
      zoomToMI.setAccelerator( 
         KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      zoomToMI.addActionListener( zoomer);
      
      return retval;
   }

}
