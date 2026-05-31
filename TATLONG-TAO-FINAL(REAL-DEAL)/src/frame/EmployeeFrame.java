package frame;

import util.UIUtil;

import javax.swing.*;
import java.awt.*;

public class EmployeeFrame extends JFrame {

    public EmployeeFrame(String username) {

        UIUtil.closeOtherWindows(EmployeeFrame.class, this);

        setTitle("Employee");
        setSize(440, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(null);
        panel.setBackground(UIUtil.CREAM);

        JLabel label = new JLabel("Welcome " + username, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(UIUtil.BRAND_RED);
        label.setBounds(70, 70, 300, 30);

        JButton logout = UIUtil.actionButton("LOGOUT", UIUtil.BRAND_RED);
        logout.setBounds(150, 145, 135, 44);
        logout.addActionListener(e -> {
            UIUtil.closeAllWindowsExcept(this);
            dispose();
            new LoginFrame();
        });

        panel.add(label);
        panel.add(logout);

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }
}
