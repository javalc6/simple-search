/*
IMPORTANT NOTICE, please read:

This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE,
please read the enclosed file license.txt or http://www.gnu.org/licenses/licenses.html

Note that this software is freeware and it is not designed, licensed or intended
for use in mission critical, life support and military purposes.

The use of this software is at the risk of the user.
*/
package search;
import javax.swing.*;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;

class ReplaceValue {
	String searchStr, replaceStr;
	public ReplaceValue(String s1, String r1) {
		searchStr = s1;
		replaceStr = r1;
	}
}

class ReplaceDialog extends JDialog
                   implements PropertyChangeListener {


    private JTextField searchField = new JTextField(10);
    private JTextField replaceField = new JTextField(10);
	private JCheckBox cbCaseSens = new JCheckBox("case sensitive");

    private JOptionPane optionPane;

    private String btnString1 = "Replace All";
    private String btnString2 = "Cancel";

	private SearchPreferences prefs;
	private ReplaceValue retValue;


    /** Creates the reusable dialog. */
    public ReplaceDialog(Frame aFrame, SearchPreferences preferences) {
        super(aFrame, true);
        setTitle("Replace in files");

		prefs = preferences;

        //Create an array of the text and components to be displayed.
		Object[] components = {cbCaseSens, "Replace:", searchField, "with:", replaceField};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(components,
                                    JOptionPane.PLAIN_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);

        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {

                    optionPane.setValue(Integer.valueOf(
                                        JOptionPane.CLOSED_OPTION));
            }
        });


		setResizable(false);
        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
    }

    /** This method show the dialog */
    public ReplaceValue showDialog() {
        searchField.setText(prefs.searchStr);
//		cbCaseSens.setSelected(prefs.caseSensitive);
		cbCaseSens.setSelected(true);
		retValue = null;
        setVisible(true);
		return retValue;
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
         && (e.getSource() == optionPane)
         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
				if (!cbCaseSens.isSelected()) {
					JOptionPane.showMessageDialog(this, "Case insensitive replace is not supported in this version of Search");
					return;
				}
				if (searchField.getText().length() > 0) { // at least one char is needed in search field!!!
					retValue = new ReplaceValue(searchField.getText(),replaceField.getText());
					setVisible(false); // value ok, exit
				} else JOptionPane.showMessageDialog(this, "Please specify the string to be replaced");
            } else { //user closed dialog or clicked cancel
                setVisible(false);
            }
        }
    }

}
