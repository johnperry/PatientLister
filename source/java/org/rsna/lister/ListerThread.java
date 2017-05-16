/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.lister;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.rsna.ui.*;
import org.rsna.util.*;

public class ListerThread extends Thread {

	File baseDir;
	int baseDirPathLength = 0;
	File csvFile;
	StringBuffer csv;
	ColorPane textPane;
	JLabel currentDoc;
	int docCount;
	boolean includeDiagnosis;

    public ListerThread(File baseDir, ColorPane textPane, JLabel currentDoc, boolean includeDiagnosis) {
		this.baseDir = baseDir;
		this.baseDirPathLength = baseDir.getAbsolutePath().length();
		this.textPane = textPane;
		this.currentDoc = currentDoc;
		this.includeDiagnosis = includeDiagnosis;
		csvFile = new File("PatientList.csv");
		csv = new StringBuffer();
	}

	public void run() {
		for (File dir : baseDir.listFiles()) {
			if (dir.isDirectory() && dir.getName().matches("ss\\d+.*")) {
				docCount = 0;
				File docsdir = new File(dir, "docs");
				textPane.println(Color.black,"Processing "+dir.getAbsolutePath());
				//Process the directory tree
				processDirectory(docsdir);
				textPane.println(Color.black, docCount + " document"
						+((docCount != 1) ? "s" : "")
						+" processed.\n");
			}
		}
		textPane.println(Color.blue, "\nDone.\n\n");
		FileUtil.setText(csvFile, csv.toString());
	}

	//Walk a directory tree, looking for MIRCdocuments
	void processDirectory(File directory) {

		//Find the xml files.
		for (File file : directory.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".xml")) {
				try {
					//Found one, make sure it's a MIRCdocument
					Document xmlDoc = XmlUtil.getDocument(file);
					Element root = xmlDoc.getDocumentElement();

					if (root.getTagName().equals("MIRCdocument")) {
						docCount++;
						
						//Log key elements
						String path = file.getAbsolutePath().substring(baseDirPathLength);
						path = path.replace("\\", "/");
						textPane.println(Color.blue, path);
						String title = getElementText(root, "title");
						String category = getElementText(root, "category");
						String diagnosis = getDiagnosis(root);
						
						NodeList nl = root.getElementsByTagName("patient");
						for (int i=0; i<nl.getLength(); i++) {
							Element patient = (Element)nl.item(i);
							String species = getElementText(patient, "pt-species");
							String breed = getElementText(patient, "pt-breed");
							log(title, category, species, breed, diagnosis, path);
						}
						if (nl.getLength() == 0) log(title, category, "null", "null", diagnosis, path);

						//There is only one MIRCdocument per directory.
						//If we find a MIRCdocument, there can be no child
						//directories. Thus, if we get here, we are done
						//with this directory, so we can simply return.
						return;
					}
				}
				catch (Exception tryTheNextOne) { }
			}
		}

		//Now process any child directories
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) processDirectory(file);
		}
	}

	String getElementText(Element parent, String name) {
		Element ch = XmlUtil.getFirstNamedChild(parent, name);
		return (ch != null) ? ch.getTextContent() : "null";
	}
	
	String getDiagnosis(Element root) {
		String dx = "";
		Node child = root.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element section = (Element)child;
				if (section.getNodeName().equals("section") && section.getAttribute("heading").equals("Diagnosis")) {
					dx = section.getTextContent();
					dx = dx.replace("\"", "");
					dx = dx.replaceAll("\\s+", " ").trim();
					return dx;
				}
			}
			child = child.getNextSibling();
		}
		return dx;
	}
	
	private void log(String title, String category, String species, String breed, String diagnosis, String path) {
		csv.append("\""+title+"\",");
		csv.append("\""+category+"\",");
		csv.append("\""+species+"\",");
		csv.append("\""+breed+"\",");
		if (includeDiagnosis) csv.append("\""+diagnosis+"\",");
		csv.append(path+"\n");
	}

}
