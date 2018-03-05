/*
IMPORTANT NOTICE, please read:

This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE,
please read the enclosed file license.txt or http://www.gnu.org/licenses/licenses.html

Note that this software is freeware and it is not designed, licensed or intended
for use in mission critical, life support and military purposes.

The use of this software is at the risk of the user.
*/
/*

class SearchFiles (used by Search.java)

search for a string in all files included in directory

29-11-2006 version 0.1.4: corrected handling of filename

*/ 
package search;

import java.util.*;
import java.io.*;

public class SearchFiles extends Thread {
		LinkedList<File> files = new LinkedList<>();
		LinkedList<Found> save = new LinkedList<>();
		String fname;
		String searchStr;
		String pattern;
		SearchList search;
		Match match;
		boolean searchSubfolders, casesens;

		// constructor
		 public SearchFiles(SearchList search, SearchPreferences prefs) {
			this.search = search;
			if (prefs.caseSensitive) searchStr = prefs.searchStr;
			else searchStr = prefs.searchStr.toLowerCase();
			searchSubfolders = prefs.searchSubfolders;
			casesens = prefs.caseSensitive;
			pattern = translateToRegex(prefs.pattern).toLowerCase();
			files.clear();
			files.add(new File(prefs.dir));
			match = prefs.match;
		 }

		class Found
		{	String filepath;
			int line;
			public Found(String filepath, int line) {
				this.filepath = filepath;
				this.line = line;
			}
		};

		 public void run() {
			int num = 0;
			while ((!isInterrupted())&&(files.size() > 0))
			{
				num++;
				File f = files.poll();
				File [] af = f.listFiles();
				if (af != null) 
						for (int i = 0; i < af.length; i++) {
							if (af[i].isDirectory()&&searchSubfolders)
							{  files.add(af[i]);
							} else if ((af[i].getName().toLowerCase().matches(pattern))&& match.matchFile(af[i]))
								{   int line = -1;
									if ((searchStr.length() == 0)||((line=checkFile(af[i].getPath()))!=-1)) {
										synchronized (save) {
											save.add(new Found(af[i].getPath(),line));
										}
// please note that the SwingUtilities.invokeLater() is needed to avoid strange problems in the main window! DO NOT REMOVE
										javax.swing.SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												Found f;
												synchronized (save) {
													while ((f = save.poll()) != null)
													{	search.addItem(f.filepath,f.line);
													}
												}
											}
										});
									}
								}
						}
				if (num % 10 == 0) yield();
			}
			if (files.size() == 0)
			{	
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						search.noItem();
					}
				});
			}
		 }

	boolean checkExt(String s) {
		int i = s.lastIndexOf('.');
		if (i == -1)
		{  return (false);
		}
		String ext = s.substring(i+1).toLowerCase();
		return (ext.matches(pattern));
	}


	int checkFile(String fname) {
		if ((searchStr == null)||(searchStr.length() == 0)) return -1;
		try {
            LineNumberReader in =
                   new LineNumberReader(new FileReader(fname));
            String s;
            while((s=in.readLine())!=null) {
				if (casesens ? s.contains(searchStr) : s.toLowerCase().contains(searchStr)) {	
					in.close();
					return(in.getLineNumber());
				}
			}
			in.close();
			return -1;
		}
		catch (IOException e)
		{	return -1;
		}
	} // end of checkfile()

	String translateToRegex(String pattern) {
		if ((pattern == null)||(pattern.length() == 0)) return pattern;
		String escape = "\\+-{}[]()^$|."; // note: ? and * are treated in different way; don't change the content
		for (int i=0; i < escape.length() ; i++) pattern = pattern.replace(""+escape.charAt(i),"\\"+escape.charAt(i));
		return pattern.replace("?",".").replace("*",".*");
	}

}
