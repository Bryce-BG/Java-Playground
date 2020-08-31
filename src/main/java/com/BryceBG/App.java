package com.BryceBG;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class App {
	/**
	 * TODO*
	 * add testing system (create mock DB and entries to work on)
	 * 
	 */



    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        frame.getContentPane().add(label);
        JButton exit = new JButton("exit");
        frame.getContentPane().add(exit);

        exit.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae){
        		System.exit(0);
        	}
        });
   

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        
    }




	

}
