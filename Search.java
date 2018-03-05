/*
IMPORTANT NOTICE, please read:

This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE,
please read the enclosed file license.txt or http://www.gnu.org/licenses/licenses.html

Note that this software is freeware and it is not designed, licensed or intended
for use in mission critical, life support and military purposes.

The use of this software is at the risk of the user.
*/
/*
search for a string in all files included in directory

usage:  java search/Search

15-03-2008 version 0.3: menu restyling, added help, new features touch and replace files

todo
- add more search criteria: set of extensions,...
- add case not sensitive option in replaceFile()
- show wait cursor when processing replaceFiles
- add handling of uncaught exceptions in threads
- add complex search criteria like: +alfa -beta
- outline matching strings in the right pane of the search window

*/ 
package search;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;



public class Search extends JFrame implements ListSelectionListener, SearchList  {
	static final String version = "0.3";
	static final String WindowTitle = "Search tools";
	static final String ABOUTMSG = WindowTitle+"\nVersion "+version+"\n\n15-03-2008\njavalc6";
	static final String preferencesFile = "search.xml";
	static final String textExtensions = "java|txt|log|bat|ini|htm|html|xhtml|css|js|php|xml";
	static final String hostingWebServer = "http://search-tools.sourceforge.net/";


	private DefaultListModel listModel;
	private ArrayList<Integer> listContent;
    private JTextArea panel;
    private JList list;
    private JSplitPane splitPane, topPane;
	private SearchPreferences prefs = new SearchPreferences();
	private	SearchDialog searchDialog = new SearchDialog(this, prefs);
	private ReplaceDialog replaceDialog;
	private ImageIcon icon = new ImageIcon(Search.class.getResource("images/search.png"), "Search");

	JMenuItem savelistItem;
	JMenuItem deleteItem;
	JMenuItem touchItem;
	JMenuItem copyItem;
	JMenuItem moveItem;
	JMenuItem replaceItem;

	CancelDialog waiting;
	JLabel status;

	private HelpWindow hWindow = new HelpWindow(545+70, 20);

    public Search() {
		super(WindowTitle);
		setLocation(100,100);
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
        });


		JMenuBar menuBar = new JMenuBar();
		menuBar.add(buildToolsMenu());
		menuBar.add(buildEditMenu());
		menuBar.add(buildHelpMenu());
		setJMenuBar(menuBar);	


		listModel = new DefaultListModel() {
			public void clear() {
				super.clear();	
				savelistItem.setEnabled(false);
				deleteItem.setEnabled(false);
				touchItem.setEnabled(false);
				copyItem.setEnabled(false);
				moveItem.setEnabled(false);
				replaceItem.setEnabled(false);
				status.setText("No files");
			}
			public Object remove(int index) {
				Object obj = super.remove(index);
				if (size() == 0) {
					savelistItem.setEnabled(false);
					deleteItem.setEnabled(false);
					touchItem.setEnabled(false);
					copyItem.setEnabled(false);
					moveItem.setEnabled(false);
					replaceItem.setEnabled(false);
					status.setText("No files");
				} else status.setText(size()+" files");
				return obj;
			}
			public void addElement(Object obj) {
				if (listModel.size() == 0)	{
					savelistItem.setEnabled(true);
					deleteItem.setEnabled(true);
					touchItem.setEnabled(true);
					copyItem.setEnabled(true);
					moveItem.setEnabled(true);
					replaceItem.setEnabled(true);
				}
				super.addElement(obj);
				status.setText(size()+" files");
			}
		}; 
		listContent = new ArrayList<>();

		list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(list);

        panel = new JTextArea();
		panel.setWrapStyleWord(true);
		panel.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane panelScrollPane = new JScrollPane(panel);

		//Provide minimum sizes for the two components in the split pane
        listScrollPane.setMinimumSize(new Dimension(100, 50));
        panelScrollPane.setMinimumSize(new Dimension(100, 50));


        //Create a split pane with the two scroll panes in it
        topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   listScrollPane, panelScrollPane);
        topPane.setOneTouchExpandable(true);
        topPane.setDividerLocation(200);
        //Provide a preferred size for the split pane
        topPane.setPreferredSize(new Dimension(600, 400));

        status = new JLabel("No files");

//Create a split pane and put "top" (a split pane)
//and JLabel instance in it.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPane, status);
// When the window is resized, only topPane is affected:
		splitPane.setResizeWeight(1);
		getContentPane().add(splitPane);
        pack();

		replaceDialog = new ReplaceDialog(this, prefs);
		replaceDialog.pack();
		
		
		setVisible(true);
	}

// class CancelDialog
	class CancelDialog extends JDialog {

		SearchFiles sf = null;

		public CancelDialog() {
			super(Search.this, "Searching...", true);
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent e) {
//				getToolkit().sync();
				sf = new SearchFiles(Search.this, prefs);
				sf.start();
				}
			});

			Container cp = getContentPane();
			cp.setLayout(new FlowLayout());
			cp.add(new JLabel("Searching..."));
			cp.add(new JLabel(icon));
			JButton button = new JButton("Cancel");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				if (sf != null) sf.interrupt();
				dispose(); // Closes the dialog
			}
			});
			cp.add(button);
			setSize(150,125);
		}
	}


    public void addItem(String key, int value) {
		listModel.addElement(key);
		listContent.add(value);
	}

    public void noItem() {
		if (waiting != null) waiting.dispose();
	}


    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;

        JList theList = (JList)e.getSource();
        if (theList.isSelectionEmpty()) {
            panel.setText(null);
        } else {
			renderText((String) theList.getSelectedValue(), listContent.get(theList.getSelectedIndex()));
            panel.revalidate();
        }
    }

	public void renderText(String fname, int line) {
		String ext;
		int i = fname.lastIndexOf('.');
		if (i == -1) ext = "";
		else ext = fname.substring(i+1).toLowerCase();

		if (ext.matches(textExtensions))
		{
			panel.setForeground(Color.black);
			try {
				LineNumberReader in = new LineNumberReader(new FileReader(fname));
				if (line != -1)	{
					String s="";
					while((s=in.readLine())!=null) {
						if (in.getLineNumber() == line) {
							break;
						}
					}
					panel.setText(s);
				} else {
// in case line == -1, all the file content is printed in the panel
					panel.read(new LineNumberReader(new FileReader(fname)), null);
				}
				in.close();
			}
			catch (IOException ex)
			{	panel.setForeground(Color.red);
				panel.setText(ex.toString());
			}
		} else {
			panel.setForeground(Color.blue);
			panel.setText("The file extension is not known as text file");
		}
	}

	protected JMenu buildToolsMenu() {
		JMenu file = new JMenu("File");
		JMenuItem search = new JMenuItem("Search...");
		savelistItem = new JMenuItem("Export filelist...");
		JMenuItem preference = new JMenuItem("Preferences...");
		JMenuItem exit = new JMenuItem("Exit");

		savelistItem.setEnabled(false);
		preference.setEnabled(false);

		// execute "Search"
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}});

		// execute "Export list"
		savelistItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savelistItem();
			}});

		// execute "Exit"
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}});

		file.add(search);
		file.add(savelistItem);
		file.addSeparator();
		file.add(preference);	
		file.addSeparator();
		file.add(exit);
		return file;
	}

// Begin "Search"
	void startSearch() { 
		if (searchDialog.showDialog()) {
			listModel.clear();
			listContent.clear();
			waiting = new CancelDialog();
			waiting.setVisible(true);
		}
	}
// End "Search"

// Begin "savelistItem"
	void savelistItem() { 
		JFileChooser fc=new JFileChooser(".");
		int returnVal = fc.showSaveDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
		   File file = fc.getSelectedFile();
			try	{
				PrintWriter out = new PrintWriter(new FileWriter(file));
				 for (Enumeration e = listModel.elements() ; e.hasMoreElements() ;) {
					 out.println(e.nextElement());
				 }
				out.close();				
			}
			catch (IOException ex)
			{	System.out.println(ex.toString());
			}
		}
	}
// End "savelistItem"

	 protected JMenu buildEditMenu() {
		JMenu tools = new JMenu("Tools");
		copyItem = new JMenuItem("Copy files to folder...");
		moveItem = new JMenuItem("Move files to folder...");
		touchItem = new JMenuItem("Touch files...");
		deleteItem = new JMenuItem("Delete files...");
		replaceItem = new JMenuItem("Replace files...");

		copyItem.setEnabled(false);
		moveItem.setEnabled(false);
		touchItem.setEnabled(false);
		deleteItem.setEnabled(false);
		replaceItem.setEnabled(false);

	// execute "copy"
		copyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyMoveFiles("Copy");
			}});

	// execute "move"
		moveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyMoveFiles("Move");
			}});

	// execute "touch"
		touchItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				touchFiles();
			}});

	// execute "delete"
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteFiles();
			}});

	// execute "replace"
		replaceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ReplaceValue val = replaceDialog.showDialog();
				if (val != null) {
					replacesFiles(val.searchStr, val.replaceStr);
				}
			}});

		tools.add(copyItem);
		tools.add(moveItem);
		tools.add(touchItem);
		tools.add(deleteItem);
		tools.add(replaceItem);
		return tools;
	 }

// cmd can assume the following values: "Move" and "Copy"
	void copyMoveFiles(String cmd) {
		JFileChooser fc = new JFileChooser(".");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(cmd+": select destination directory");
		int returnVal = fc.showDialog(null,cmd+" files");
		if(returnVal == JFileChooser.APPROVE_OPTION) {
		   String dir = fc.getSelectedFile().getPath();
			for (int i = 0; i < listModel.size() ;) {
				try	{
					Files.copyFile((String)listModel.get(i), dir);
					if (cmd.equals("Move")) {
						new File((String)listModel.get(i)).delete();
						listModel.remove(i);
					} else i++;
				}
				catch (IOException ex)	{
					status.setText(ex.toString());
					i++;
				}
			}
		}
	}

	void deleteFiles() {
		int returnVal = JOptionPane.showConfirmDialog(null, "All files will be deleted\nDelete is not recoverable\nConfirm?", "Warning", 
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (returnVal == JOptionPane.OK_OPTION) {
			for (Enumeration e = listModel.elements() ; e.hasMoreElements() ;) {
				 new File((String) e.nextElement()).delete();
			}
			listModel.clear();
		}
	}

	void touchFiles() {
		int returnVal = JOptionPane.showConfirmDialog(null, "All files will be updated\nConfirm?", "Warning", 
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (returnVal == JOptionPane.OK_OPTION) {
			long now = System.currentTimeMillis();
			for (Enumeration e = listModel.elements() ; e.hasMoreElements() ;) {
				 new File((String) e.nextElement()).setLastModified(now);
			}
		}
	}

	void replacesFiles(String searchStr, String replaceStr) {
		int returnVal = JOptionPane.showConfirmDialog(null, "All files will be updated with replace string\nConfirm?", "Warning", 
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (returnVal == JOptionPane.OK_OPTION) {
			Cursor savedCursor = getCursor();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			for (Enumeration e = listModel.elements() ; e.hasMoreElements() ;) {
				String filename = (String) e.nextElement();
//				System.out.println(filename);
				try	{
					Files.replaceFile(filename,searchStr,replaceStr);
				}
				catch (IOException ex)	{
					status.setText(ex.toString());
				}
			}
			setCursor(savedCursor);
		}
	}



	 protected JMenu buildHelpMenu() {
		JMenu help = new JMenu("Help");
		JMenuItem openHelp = new JMenuItem("Help Topics...");
		JMenuItem checkVersion = new JMenuItem("Check Version...");
		JMenuItem about = new JMenuItem("About "+WindowTitle+"...");

//		openHelp.setEnabled(false);

		openHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hWindow.setPage(Search.class.getResource("helpfile.html"));
			}});

		checkVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WebFetch fetch = new WebFetch();
				try {
					String str = fetch.fetchURL(hostingWebServer+"search.html","<span id=version>");
					if (str != null)
						if (str.equals(version))
							JOptionPane.showMessageDialog(Search.this, "No need to upgrade", "Check Version", JOptionPane.INFORMATION_MESSAGE, icon);
// In JDK1.5 there is no direct way to open the default browser, for this reason we use the BareBonesBrowserLaunch class
						else new BareBonesBrowserLaunch().openURL(hostingWebServer+"search.html");
					else
						JOptionPane.showMessageDialog(Search.this, "Technical problems\nRetry later","Error",JOptionPane.ERROR_MESSAGE);
				}
				catch (java.net.ConnectException cex) {
					JOptionPane.showMessageDialog(Search.this, "Connection cannot be setup\nProxy has to be configured?","Error",JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(Search.this, "Technical problems\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				}

			}});

	// execute "About"
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, ABOUTMSG, "About "+WindowTitle,JOptionPane.PLAIN_MESSAGE, icon);
			}});

		help.add(openHelp);
		help.addSeparator();
		help.add(checkVersion);
		help.addSeparator();
		help.add(about);
		return help;
	 }

	public static String getAppName() {
		return Search.class.getName().split("\\.")[1];
	}
		
		public static void main(String s[]) {
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception e) { }    
			new Search();
		}
	}
