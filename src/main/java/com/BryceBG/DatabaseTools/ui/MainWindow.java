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
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
        
        
        //search field
        JTextField searchFld = new JTextField(30);
        searchPanel.add(searchFld);
        
        
        //result pane
        JTextPane resultPane = new JTextPane();
        resultPane.setPreferredSize(new Dimension(200, 200));
        contentPane.add(resultPane);


        
        
        //1 way of doing action listener
        ActionListener tempBtnActLstn = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String resourceToSearch = searchFld.getText();
				String result = Utils.readResourceToString(resourceToSearch);
				
				resultPane.setText(result);
				
//				System.exit(0); //exit when button is clicked				
			}
        };
        
        
        
        
        
        JButton tempBtn = new JButton("Click me!");
        //another way of adding action listener
//        tempBtn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//            	System.exit(0); //exit when button is clicked
//             }
//          });
        tempBtn.addActionListener(tempBtnActLstn);
        
        searchPanel.add(tempBtn);
        
        
        
        //DEBUG
//        JOptionPane.showMessageDialog(mainFrame, Utils.getConfigString("app.dbpass", null));
        
		
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
	

