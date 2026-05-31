package frame;

import util.AuthUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        UIUtil.closeOtherWindows(LoginFrame.class, this);

        setTitle("Login");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(createPanel());
        setVisible(true);
    }

    private JPanel createPanel() {

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIUtil.CREAM);

        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(420, 500));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 6),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel brand = new JLabel("MACDOLIBEE", SwingConstants.CENTER);
        brand.setFont(new Font("Arial", Font.BOLD, 32));
        brand.setForeground(UIUtil.BRAND_RED);
        brand.setBounds(40, 30, 340, 44);

        JLabel title = new JLabel("STAFF LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(UIUtil.BRAND_RED);
        title.setBounds(50, 88, 320, 30);

        JLabel notice = new JLabel(
                "<html><center>Admin accounts get full access. Employee accounts can view and complete orders.</center></html>",
                SwingConstants.CENTER
        );
        notice.setFont(new Font("Arial", Font.PLAIN, 13));
        notice.setForeground(UIUtil.INK);
        notice.setBounds(60, 120, 300, 42);

        JTextField userField = new JTextField();
        userField.setBounds(60, 185, 300, 38);
        installTextPlaceholder(userField, "Username");

        JPasswordField passField = new JPasswordField();
        passField.setBounds(60, 240, 300, 38);
        installPasswordPlaceholder(passField, "Key");

        JButton login = UIUtil.actionButton("LOGIN", UIUtil.BRAND_RED);
        login.setBounds(60, 315, 135, 46);

        JButton customer = UIUtil.actionButton("CUSTOMER", UIUtil.BRAND_YELLOW);
        customer.setForeground(UIUtil.INK);
        customer.setBounds(215, 315, 145, 46);

        login.addActionListener(e -> handleLogin(userField, passField));

        customer.addActionListener(e -> {
            dispose();
            new CustomerFrame();
        });

        panel.add(brand);
        panel.add(title);
        panel.add(notice);
        panel.add(userField);
        panel.add(passField);
        panel.add(login);
        panel.add(customer);

        root.add(panel);

        return root;
    }

    private void handleLogin(JTextField userField, JPasswordField passField) {

        String username = userField.getText();
        String password = new String(passField.getPassword());

        if ("Username".equals(username)) {
            username = "";
        }

        if ("Key".equals(password)) {
            password = "";
        }

        String role = AuthUtil.checkLogin(username, password);

        if (role == null) {
            JOptionPane.showMessageDialog(this, "Invalid Login");
            return;
        }

        if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("employee")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Access denied. This account role is not allowed."
            );
            return;
        }

        dispose();
        new AdminFrame(role);
    }

    private void installTextPlaceholder(JTextField field, String placeholder) {

        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(UIUtil.INK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void installPasswordPlaceholder(JPasswordField field, String placeholder) {

        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(new String(field.getPassword()))) {
                    field.setText("");
                    field.setEchoChar('*');
                    field.setForeground(UIUtil.INK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).trim().isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }
}
