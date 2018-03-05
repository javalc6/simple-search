/*
IMPORTANT NOTICE, please read:

This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE,
please read the enclosed file license.txt or http://www.gnu.org/licenses/licenses.html

Note that this software is freeware and it is not designed, licensed or intended
for use in mission critical, life support and military purposes.

The use of this software is at the risk of the user.
*/
/* 

class SearchDialog implements a dialog for the preferences of the search

used by Search.java 

21-11-2006 version 0.1.3: added search criteria related to filesize and date

*/
package search;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

class SearchDialog extends JDialog {

	private boolean apply; // check this flag before getting preferences!
	private SearchPreferences prefs;

    private JTextField tfSearchStr = new JTextField(12);
    private JTextField tfPattern = new JTextField(12);
    private JTextField tfDir = new JTextField(16);

	private JCheckBox cbCaseSens = new JCheckBox("case sensitive");
	private JCheckBox cbSubfolders = new JCheckBox("search subfolders");

	private JCheckBox cbFileLarger = new JCheckBox("filesize (KB) is larger than:");
    private JTextField tfSize = new JTextField(6);
	private int fs_larger = -1;
	private JCheckBox cbFileSmaller = new JCheckBox("filesize (KB) is smaller than:");
    private JTextField tfSize2 = new JTextField(6);
	private int fs_smaller = -1;

	private JCheckBox cbFileAfter = new JCheckBox("file created from:");
	private JCheckBox cbFileBefore = new JCheckBox("file created before:");
	private DateButton startDateButton;
    private DateButton endDateButton;
	private Date startDate;
	private Date endDate;

	private JFileChooser fc;

    /* constructor */
    public SearchDialog(Frame aFrame, SearchPreferences preferences) {
        super(aFrame, true);
		prefs = preferences;
		prefs.readXML();
		prefs.match = new AdvancedMatch();

        setTitle("Search options");
		JPanel container = new JPanel();
		container.setLayout( new BorderLayout() );

		JTabbedPane tabs = new JTabbedPane();
		JPanel mainPane = buildMainPanel();
		JPanel advPane = buildAdvancedPanel();
		tabs.addTab( "Main", null, mainPane );
		tabs.addTab( "Advanced", null, advPane );

		setResizable(false);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("Search");
		ok.addActionListener(new ActionListener() {
				   public void actionPerformed(ActionEvent e) {
					   OKPressed();
				   }});
		buttonPanel.add(ok);
		getRootPane().setDefaultButton(ok);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
				   public void actionPerformed(ActionEvent e) {
					   CancelPressed();
				   }});
		buttonPanel.add( cancel );

		container.add(tabs, BorderLayout.CENTER);
		container.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(container);

		Dimension screenSize = this.getToolkit().getScreenSize();
		this.setLocation(screenSize.width/3,screenSize.height/3);
		pack();
    }

    public void CancelPressed() {
		apply = false;
		setVisible(false);
    }

	/* OK */
    public void OKPressed() {
		try	{
			fs_larger = ( cbFileLarger.isSelected() ? Integer.parseInt(tfSize.getText())*1024 : -1);
			fs_smaller = ( cbFileSmaller.isSelected() ? Integer.parseInt(tfSize2.getText())*1024 : -1);
		}
		catch (java.lang.NumberFormatException ex)	{
			JOptionPane.showMessageDialog(this, "The values you entered for the filesize are not valid");
			return;
		}
		if ((fs_larger != -1)&&(fs_smaller != -1)&&(fs_larger >= fs_smaller))	{
			JOptionPane.showMessageDialog(this, "The range for filesize is not valid");
			return;
		}
		startDate = ( cbFileAfter.isSelected() ? startDateButton.getDate() : null);
		endDate = ( cbFileBefore.isSelected() ? endDateButton.getDate() : null);
		if ((startDate != null)&&(endDate != null)&&startDate.after(endDate)) {
			JOptionPane.showMessageDialog(this, "Invalid date range");
			return;
		}

		prefs.searchStr = tfSearchStr.getText();
		prefs.pattern = tfPattern.getText();
		prefs.dir = tfDir.getText();
		prefs.caseSensitive = cbCaseSens.isSelected();
		prefs.searchSubfolders = cbSubfolders.isSelected();
		try
		{
			prefs.writeXML();
		}
		catch (java.io.IOException ex) {}
        apply = true;
		setVisible(false);
    }

    /* show the dialog */
    public boolean showDialog() {
		apply=false;
		tfSearchStr.setText(prefs.searchStr);
        tfPattern.setText(prefs.pattern);
        tfDir.setText(prefs.dir);
		cbCaseSens.setSelected(prefs.caseSensitive);
		cbSubfolders.setSelected(prefs.searchSubfolders);
		fc.setCurrentDirectory(new java.io.File(prefs.dir));
		setVisible(true);
		return(apply);
    }

	public JPanel buildMainPanel() {
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new ColumnLayout());


// Text to search: <String>
		JPanel searchStrPanel = new JPanel();
		searchStrPanel.add(new JLabel ("Text to search:"));
		searchStrPanel.add(tfSearchStr);
		mainPane.add(searchStrPanel);

// Filepattern: <String>
		JPanel patternPanel = new JPanel();
		patternPanel.add(new JLabel ("File pattern:"));
		patternPanel.add(tfPattern);
		mainPane.add(patternPanel);
		
		fc = new JFileChooser();
		JButton btndir = new JButton("Browse...");
		btndir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				   tfDir.setText(fc.getSelectedFile().getPath());
				}
			}});
// Directory: <String> [button]
		JPanel dirPanel = new JPanel();
		dirPanel.add(new JLabel ("Directory:"));
		dirPanel.add(tfDir);
		dirPanel.add(btndir);
		mainPane.add(dirPanel);

		mainPane.add(cbCaseSens);
		mainPane.add(cbSubfolders);
		return mainPane;
	}

	public JPanel buildAdvancedPanel() {
		JPanel advPane = new JPanel();
		advPane.setLayout(new ColumnLayout());


// File larger than
		JPanel fileLargerPanel = new JPanel();
		fileLargerPanel.add(cbFileLarger);
		tfSize.setEnabled(false);
		fileLargerPanel.add(tfSize);
		cbFileLarger.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					tfSize.setEnabled(true);
				} else tfSize.setEnabled(false);
			}});
		advPane.add(fileLargerPanel);

// File smaller than
		JPanel fileSmallerPanel = new JPanel();
		fileSmallerPanel.add(cbFileSmaller);
		tfSize2.setEnabled(false);
		fileSmallerPanel.add(tfSize2);
		cbFileSmaller.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					tfSize2.setEnabled(true);
				} else tfSize2.setEnabled(false);
			}});
		advPane.add(fileSmallerPanel);

		Calendar now = Calendar.getInstance();
		GregorianCalendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		GregorianCalendar tomorrow = (GregorianCalendar)today.clone();
		tomorrow.add(Calendar.DAY_OF_MONTH,1);
// start date
		JPanel startDatePanel = new JPanel();
		startDatePanel.add(cbFileAfter);
		startDateButton = new DateButton(today.getTime());
		startDateButton.setEnabled(false);
		startDatePanel.add(startDateButton);
		cbFileAfter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					startDateButton.setEnabled(true);
				} else startDateButton.setEnabled(false);
			}});
		advPane.add(startDatePanel);
// end date
		JPanel endDatePanel = new JPanel();
		endDatePanel.add(cbFileBefore);
		endDateButton = new DateButton(tomorrow.getTime());
		endDateButton.setEnabled(false);
		endDatePanel.add(endDateButton);
		cbFileBefore.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					endDateButton.setEnabled(true);
				} else endDateButton.setEnabled(false);
			}});
		advPane.add(endDatePanel);

		return advPane;
	}

	class AdvancedMatch implements Match {
		public boolean matchFile(File f) {
			if (fs_larger != -1)	if (!(f.length() >= fs_larger)) return false;
			if (fs_smaller != -1)	if (!(f.length() < fs_smaller)) return false;
			if (startDate != null)	if (startDate.after(new Date(f.lastModified()))) return false;
			if (endDate != null)	if ((endDate.before(new Date(f.lastModified())))) return false;
			return true;
		}
	};
}

class ColumnLayout implements LayoutManager {

  int xInset = 5;
  int yInset = 5;
  int yGap = 2;

  public void addLayoutComponent(String s, Component c) {}

  public void layoutContainer(Container c) {
      Insets insets = c.getInsets();
      int height = yInset + insets.top;
      
      Component[] children = c.getComponents();
      Dimension compSize = null;
      for (int i = 0; i < children.length; i++) {
	  compSize = children[i].getPreferredSize();
	  children[i].setSize(compSize.width, compSize.height);
	  children[i].setLocation( xInset + insets.left, height);
	  height += compSize.height + yGap;
      }

  }

  public Dimension minimumLayoutSize(Container c) {
      Insets insets = c.getInsets();
      int height = yInset + insets.top;
      int width = 0 + insets.left + insets.right;
      
      Component[] children = c.getComponents();
      Dimension compSize = null;
      for (int i = 0; i < children.length; i++) {
	  compSize = children[i].getPreferredSize();
	  height += compSize.height + yGap;
	  width = Math.max(width, compSize.width + insets.left + insets.right + xInset*2);
      }
      height += insets.bottom;
      return new Dimension( width, height);
  }
  
  public Dimension preferredLayoutSize(Container c) {
      return minimumLayoutSize(c);
  }
   
  public void removeLayoutComponent(Component c) {}

}