import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.swing.*;

public class UI extends JPanel implements ActionListener, KeyListener {

    // Decimal format
    private static final DecimalFormat oph = new DecimalFormat("#.00"); // orders/hr

    // Time Variables
    private static int hr = 0, min = 0;
    private static long totalSecClocked = 0, sec = 0;
    private static long secondsTillCI = -1;
    public static boolean recheckTimeTill = false; // In case computer goes to sleep
    public static boolean recheckTime = false; // Increase time accuracy

    // Labels
    private static final JTextArea stats =
        new JTextArea("Time: 00:00:00\nOrders: 0 (.00/hr)\nNeeded: 0, 0 left");

    // Stats
    private static double orders = 0;
    public static boolean inBreak = true;
    public static boolean freeze = true; // Ignore entering/leaving break and changing orders
    public static boolean clockInTimePassed = false;

    public static LocalTime clockInTime = LocalTime.parse("00:00"),  clockOutTime = LocalTime.parse("00:00"),
            breakInTime = LocalTime.parse("00:00"), breakOutTime = LocalTime.parse("00:00");
    public static int target = 0; // Target orders/hr
    private static long ordersNeeded = 0;

    // Colors
    public static Color textColor = new Color(240, 240, 240);
    public static Color buttonColor = new Color(80, 80, 80);
    public static Color bg = new Color(64, 64, 64);

    public UI() { // Set UI's properties

        JButton clockInOut = new JButton("Enter Break");
        JButton addOrder = new JButton("Add order"); // Add Order button

        setFocusable(true);
        addKeyListener(this);

        stats.setEditable(false);
        stats.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        addOrder.addActionListener(this);
        addOrder.setPreferredSize(new Dimension(100, 45));
        clockInOut.addActionListener(this);
        clockInOut.setPreferredSize(new Dimension(110, 45));

        // Set colors
        clockInOut.setBackground(buttonColor);
        clockInOut.setForeground(textColor);
        addOrder.setBackground(buttonColor);
        addOrder.setForeground(textColor);
        stats.setBackground(bg);
        stats.setForeground(textColor);
 
        // Add components
        setBackground(bg);
        add(clockInOut);
        add(addOrder);
        add(stats);

        getStats();
        
    }

    public void actionPerformed(ActionEvent e) {

        String b = e.getActionCommand();

        if (b.equals("Add order")) {

            changeOrders(1);

        } else if (b.equals("Enter Break") || b.equals("Leave Break")) {

            enterLeaveBreak();

        }

        this.requestFocus(); /* Get focus back on the UI panel every time an action is performed,
                                it's a workaround as buttons get the focus when clicked. */
        
    }

    public static void tick() { // Change time values

        totalSecClocked++;
        sec++;

        while (sec > 59) {
            min++;
            sec -= 60;
        }

        while (min > 59) {
            hr++;
            min -= 60;
        }

        getStats();

    }

    private static void getStats() {

        if (clockInTimePassed) { // Get stats =======

            StringBuilder sb = new StringBuilder();

            sb.append("Time: ");
            // Get time into human readable format
            if (hr < 10) sb.append("0");
            sb.append(hr);
            sb.append(":");
            if (min < 10) sb.append("0");
            sb.append(min);
            sb.append(":");
            if (sec < 10) sb.append("0");
            sb.append(sec);

            // Add other stats
            sb.append("\nOrders: ");
            sb.append((int)orders);
            sb.append(" (");
            sb.append(oph.format((orders*3600)/totalSecClocked));
            sb.append("/hr)\nNeeded: ");
            sb.append(ordersNeeded);
            sb.append(", ");
            if (orders < ordersNeeded) { sb.append((int) (ordersNeeded - orders));
            } else sb.append("0");
            sb.append(" left");

            stats.setText(sb.toString());

        } else if (Window.coChosen) { // Get "Time till clock in" =======

            secondsTillCI -= 1;
            long seconds = secondsTillCI;
            int hours = 0;
            int minutes = 0;

            StringBuilder sb = new StringBuilder();

            while (seconds > 59) {

                minutes++;
                seconds -= 60;

            }
            while (minutes > 59) {

                hours++;
                minutes -= 60;

            }

            if (hours < 10) sb.append("0");
            sb.append(hours);
            sb.append(":");
            if (minutes < 10) sb.append("0");
            sb.append(minutes);
            sb.append(":");
            if (seconds < 10) sb.append("0");
            sb.append(seconds);

            stats.setText("Time until clocked in:\n" + sb);

        }

    }

	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        // ======= Shortcuts =======
        if (key == 8 || key == 40) changeOrders(-1); // Remove orders with BckSpc & Down Arrow
        if (key == 48)  { // Enter/leave break with 0
            enterLeaveBreak();
        }
        if (key == 38) changeOrders(1); // Add orders with up arrow

        getStats();
		
	}

	public void keyReleased(KeyEvent e) {}

    private void enterLeaveBreak() { // Enter/Leave break

        if (!freeze) {
            if (!inBreak) { Window.enterBreakWnd.setVisible(true);
            } else Window.leaveBreakWnd.setVisible(true);
            updateButtons();
        }

    }

    private void updateButtons() { // Update buttons

        Window.packNow = true;

    }

    private void changeOrders(int amount) { // Change orders

        if (!inBreak) {
            orders += amount;
            if (orders < 0) orders = 0;
            getStats();
        }

        Window.packNow = true; // Call the Window to pack itself

    }

    public static void getTime() { // See if clock-in time has passed, if so get the difference
        
        if (clockInTime.compareTo(LocalTime.now()) <= 0 || recheckTime) {

            freeze = false;
            inBreak = false;
            clockInTimePassed = true;
            totalSecClocked = clockInTime.until(LocalTime.now(), ChronoUnit.SECONDS) - 1;
            sec = totalSecClocked;
            min = 0;
            hr = 0;
            tick();
            ordersNeeded = Math.round(target * ((double)clockInTime.until(clockOutTime, ChronoUnit.MINUTES) / 60) - 1);
            recheckTime = false;

        } else {

            if (secondsTillCI == -1 || recheckTimeTill) { // Set secondsTillCI to difference in time
                secondsTillCI = LocalTime.now().until(clockInTime, ChronoUnit.SECONDS) + 1;
                recheckTimeTill = false;
            }
            getStats();

        }

        Window.packNow = true;

    }

}
