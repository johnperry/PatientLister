/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.lister;

import org.rsna.installer.SimpleInstaller;

/**
 * The PatientLister program installer, consisting of just a
 * main method that instantiates a SimpleInstaller.
 */
public class Installer {

	static String windowTitle = "PatientLister Installer";
	static String programName = "PatientLister";
	static String introString = "<p><b>PatientLister</b> is a utility for "
								+ "creating a spreadsheet list of stored MIRCdocuments.</p>";

	public static void main(String args[]) {
		new SimpleInstaller(windowTitle,programName,introString);
	}
}
