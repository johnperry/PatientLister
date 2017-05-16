/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.lister;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.rsna.util.*;
import org.rsna.ui.*;

public class PatientLister extends JFrame implements ActionListener {

    String windowTitle = "MIRCdocument Patient List Utility";
    ColorPane textPane;
    JLabel currentDoc;
    JPanel footer;
    JCheckBox includeDiagnosis;
    JCheckBox copyToServerROOT;

    public static void main(String args[]) {
        new PatientLister();
    }

    public PatientLister() {
		super();
		setTitle(windowTitle);
		JPanel panel = new JPanel(new BorderLayout());
		getContentPane().add(panel,BorderLayout.CENTER);

		//Make the text pane and put it in a scroll pane
		textPane = new ColorPane();
		textPane.setContentType("text/plain");
		JScrollPane jsp = new JScrollPane();
		jsp.setViewportView(textPane);
		panel.add(jsp,BorderLayout.CENTER);

		//Make a footer bar to display the current document.
		footer = new JPanel();
		footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
		includeDiagnosis = new JCheckBox("Include Diagnosis");
		copyToServerROOT = new JCheckBox("Copy to Server ROOT");
		JButton start = new JButton("Start");
		start.addActionListener(this);
		footer.add(includeDiagnosis);
		footer.add(Box.createHorizontalStrut(20));
		footer.add(copyToServerROOT);
		footer.add(Box.createHorizontalGlue());
		footer.add(start);
		panel.add(footer,BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        pack();
        centerFrame();
        setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		processDocuments();
	}

    private void centerFrame() {
        Toolkit t = getToolkit();
        Dimension scr = t.getScreenSize ();
        setSize(scr.width/2, scr.height/2);
        setLocation (new Point ((scr.width-getSize().width)/2,
                                (scr.height-getSize().height)/2));
    }

	public void processDocuments() {

		//Fix up the footer
		footer.removeAll();
		footer.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		currentDoc = new JLabel(" ");
		footer.add(currentDoc);

		//Get the root of the tree to search
		File dir = new File("/JavaPrograms/CTP/mircsite");
		if (!dir.exists()) dir = new File(System.getProperty("user.dir"));
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle("Select the TFS site's mircsite directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			dir = chooser.getSelectedFile();
		}
		if ((dir != null) && dir.exists() && dir.isDirectory()) {
			//Launch the thread
			ListerThread listerThread =
				new ListerThread(new File(dir, "storage"), 
								textPane, 
								currentDoc, 
								includeDiagnosis.isSelected(),
								copyToServerROOT.isSelected());
			listerThread.start();
		}
	}
}
