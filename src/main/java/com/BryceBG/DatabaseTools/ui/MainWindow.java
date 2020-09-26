package com.BryceBG.DatabaseTools.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.BryceBG.DatabaseTools.utils.Utils;

public final class MainWindow implements Runnable {
	
    private static JFrame mainFrame;
	private JPanel searchPanel;


	@Override
	public void run() {
        pack();
        mainFrame.setVisible(true);
		
	}
	
    public MainWindow() {
        mainFrame = new JFrame("Library Management System v" + Utils.getThisJarVersion());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new GridBagLayout());

        createUI(mainFrame.getContentPane()); //TODO implement this
        pack();

        setupHandlers();//TODO implement this

        //TODO  (might want to get rid of this)
        Thread shutdownThread = new Thread();
        Runtime.getRuntime().addShutdownHook(shutdownThread);



    }

	private void setupHandlers() {
		// TODO Auto-generated method stub
		
	}

	private void createUI(Container contentPane) {
		// TODO Auto-generated method stub
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.ipadx = 2;
        gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.ipady = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        
        
        searchPanel = new JPanel();
        searchPanel.setPreferredSize(new Dimension(300, 250));
        contentPane.add(searchPanel);
        
        JButton tempBtn = new JButton("Click me!");
        tempBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0); //exit when button is clicked
             }
          });
        
        searchPanel.add(tempBtn);
        
		
	}
	
    private void pack() {
        SwingUtilities.invokeLater(() -> {
            Dimension preferredSize = mainFrame.getPreferredSize();
            mainFrame.setMinimumSize(preferredSize);
            if (isCollapsed()) {
                mainFrame.setSize(preferredSize);
            }
        });
    }

    private boolean isCollapsed() {
    	//TODO revise this
        return (!searchPanel.isVisible());
    }
}
	

