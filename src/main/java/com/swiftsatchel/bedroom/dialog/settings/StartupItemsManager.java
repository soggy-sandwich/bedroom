package com.swiftsatchel.bedroom.dialog.settings;

import com.swiftsatchel.bedroom.dialog.alert.ErrorDialog;
import com.swiftsatchel.bedroom.enums.ErrorType;
import com.swiftsatchel.bedroom.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

public class StartupItemsManager extends JDialog implements ActionListener, WindowListener {

    private final WindowParent parent;

    private final ArrayList<String> itemDirs = new ArrayList<>(); // List of startup items' directories
    private final DefaultListModel<String> itemNames = new DefaultListModel<>(); // List of startup items' names
    private JList<String> list; // The JList to be displayed

    public StartupItemsManager(SettingsDialog parent) {

        this.parent = parent;

        setTitle("Startup Items");
        addWindowListener(this);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(Settings.getAlwaysOnTop());
        setResizable(false);

        JPanel content = new JPanel(); // Content panel to set a background color
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.getBgColor());

        content.add(createTipTextArea());
        content.add(createList());
        content.add(createToolsPanel());
        Ops.setHandCursorOnCompsFrom(content); // Set hand cursor on needed components

        add(content);
        pack();

        // Center on parent window
        setLocation(parent.getX() + ((parent.getWidth()/2) - (getWidth()/2)),
                parent.getY() + ((parent.getHeight()/2) - (getHeight()/2)));

        setVisible(true);

    }

    private JLabel createTipTextArea() {

        JLabel label = new JLabel("Startup items open along with Bedroom.");
        label.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        return label;

    }

    private JPanel createToolsPanel() {

        // Create components
        JPanel panel = new JPanel();
        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove");

        // Customize em
        add.addActionListener(this);
        remove.addActionListener(this);

        // Add tools to panel
        panel.add(add);
        panel.add(remove);

        return panel;

    }

    private JPanel createList() {

        // Create stuffs
        JPanel panel = new JPanel();

        // Add startup items
        for (String item : Settings.getStartupItemsList()) {
            if (!item.equals("")) {
                itemDirs.add(item);
                itemNames.addElement(new File(item).getName());
            }
        }

        // Create list
        list = new JList<>(itemNames);
        list.setVisibleRowCount(7);

        // Create scroll pane
        JScrollPane sp = new JScrollPane(list);

        // Add to panel
        panel.add(sp);

        return panel;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {

            case "Add" -> addAnApp();
            case "Remove" -> removeAnApp();

        }

    }

    private void addAnApp() {

        if (itemNames.getSize() < 7) { // Add an item if under limit

            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                    // Set a filter of apps and text files (for scripts)
                    "Programs/Scripts", "exe", "app", "lnk", "txt", "docx", "odt", "rtf"));
            fc.setApproveButtonText("Add");
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                itemNames.addElement(fc.getSelectedFile().getName()); // Add name of app
                itemDirs.add(fc.getSelectedFile().toString()); // Add its directory
            }


        } else {
            new ErrorDialog(parent, ErrorType.STARTUP_ITEMS_FULL);
        }

    }

    private void removeAnApp() {

        // Check if we have something selected
        if (!list.isSelectionEmpty()) {

            int selected = list.getSelectedIndex(); // Get selected index
            removeAppFromBoth(selected);
            list.setSelectedIndex(selected);    // Keep cursor on same position
            if (list.isSelectionEmpty())
                // If where we put the cursor is empty, move it up.
                list.setSelectedIndex(selected - 1);

        }

    }

    private void removeAppFromBoth(int index) {
        itemNames.remove(index);
        itemDirs.remove(index);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Settings.saveStartupItems(itemDirs.toString());
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
