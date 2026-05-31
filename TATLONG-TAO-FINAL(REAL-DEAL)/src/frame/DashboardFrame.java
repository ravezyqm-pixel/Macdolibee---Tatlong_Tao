package frame;

import util.DashboardUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DashboardFrame extends JFrame {

    public DashboardFrame() {

        UIUtil.closeOtherWindows(DashboardFrame.class, this);

        setTitle("Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(createContent());
        setVisible(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtil.CREAM);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel dashboard = new JPanel(new BorderLayout(0, 14));
        dashboard.setPreferredSize(new Dimension(920, 720));
        dashboard.setBackground(Color.WHITE);
        dashboard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 6),
                new EmptyBorder(16, 16, 16, 16)
        ));

        dashboard.add(createHeader(), BorderLayout.NORTH);
        dashboard.add(createGrid(), BorderLayout.CENTER);
        dashboard.add(createBottom(), BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(dashboard, new GridBagConstraints());

        root.add(wrapper, BorderLayout.CENTER);

        return root;
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setPreferredSize(new Dimension(560, 124));
        header.setBackground(UIUtil.BRAND_RED);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 4),
                new EmptyBorder(18, 10, 18, 10)
        ));

        JLabel dashboard = new JLabel("DASHBOARD", SwingConstants.CENTER);
        dashboard.setFont(new Font("Arial", Font.BOLD, 26));
        dashboard.setForeground(Color.WHITE);

        JLabel brand = new JLabel("MACDOLIBEE", SwingConstants.CENTER);
        brand.setFont(new Font("Arial", Font.BOLD, 26));
        brand.setForeground(UIUtil.BRAND_YELLOW);

        header.add(dashboard);
        header.add(brand);

        return header;
    }

    private JPanel createGrid() {

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setPreferredSize(new Dimension(880, 500));
        grid.setBackground(Color.WHITE);

        JPanel countPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        countPanel.setOpaque(false);
        countPanel.setPreferredSize(new Dimension(880, 185));
        countPanel.setMinimumSize(new Dimension(880, 185));

        countPanel.add(createMetricCard(
                "DAILY ORDER COUNT",
                DashboardUtil.getDailyOrderCount(),
                "ORDERS TODAY"
        ));

        countPanel.add(createMetricCard(
                "WEEKLY ORDER COUNT",
                DashboardUtil.getWeeklyOrderCount(),
                "ORDERS THIS WEEK"
        ));

        countPanel.add(createMetricCard(
                "MONTHLY ORDER COUNT",
                DashboardUtil.getMonthlyOrderCount(),
                "ORDERS THIS MONTH"
        ));

        JPanel listPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listPanel.setOpaque(false);
        listPanel.setPreferredSize(new Dimension(880, 300));
        listPanel.setMinimumSize(new Dimension(880, 300));

        listPanel.add(createBestFoodsTableCard(
                "5 BEST FOODS",
                new Dimension(435, 300)
        ));

        listPanel.add(createUsersTableCard(
                "USERS AND ADMINS",
                new Dimension(435, 300)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;
        grid.add(countPanel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.weighty = 1;
        grid.add(listPanel, gbc);

        return grid;
    }

    private JPanel createMetricCard(String title, int value, String label) {
        return createMetricCard(title, String.valueOf(value), label);
    }

    private JPanel createMetricCard(String title, String value, String label) {

        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 3),
                new EmptyBorder(16, 12, 16, 12)
        ));

        JLabel titleLabel = cardTitle(title, 16);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, value.length() > 6 ? 28 : 44));
        valueLabel.setForeground(UIUtil.BRAND_RED);

        JLabel detailLabel = new JLabel(label, SwingConstants.CENTER);
        detailLabel.setFont(new Font("Arial", Font.BOLD, 13));
        detailLabel.setForeground(UIUtil.INK);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(detailLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTextCard(String title, String body) {
        return createTextCard(title, body, new Dimension(277, 196));
    }

    private JPanel createTextCard(String title, String body, Dimension size) {

        JPanel card = createBaseCard(size);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 3),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = cardTitle(title, 16);

        JTextArea bodyArea = new JTextArea(body);
        bodyArea.setEditable(false);
        bodyArea.setOpaque(false);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setLineWrap(true);
        bodyArea.setFont(new Font("Arial", Font.BOLD, 15));
        bodyArea.setForeground(UIUtil.INK);

        JScrollPane scroll = new JScrollPane(bodyArea);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createUsersTableCard(String title, Dimension size) {

        JPanel card = createBaseCard(size);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 3),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = cardTitle(title, 16);

        String[] columns = {"ID", "USER", "ROLE"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshUsersTable(model);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(UIUtil.BRAND_RED);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(UIUtil.BRAND_RED);
        table.setSelectionBackground(UIUtil.BRAND_YELLOW);
        table.setSelectionForeground(UIUtil.INK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtil.BRAND_RED, 1));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(createUserCrudButtons(table, model), BorderLayout.SOUTH);

        return card;
    }

    private JPanel createUserCrudButtons(JTable table, DefaultTableModel model) {

        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.setOpaque(false);

        JButton add = UIUtil.lightButton("ADD");
        JButton edit = UIUtil.lightButton("EDIT");
        JButton delete = UIUtil.actionButton("DELETE", UIUtil.BRAND_RED_DARK);

        add.addActionListener(e -> showUserDialog(false, table, model));
        edit.addActionListener(e -> showUserDialog(true, table, model));
        delete.addActionListener(e -> deleteSelectedUser(table, model));

        panel.add(add);
        panel.add(edit);
        panel.add(delete);

        return panel;
    }

    private void refreshUsersTable(DefaultTableModel model) {

        model.setRowCount(0);
        ArrayList<Object[]> rows = DashboardUtil.getUsersAndAdminsRows();

        if (rows.isEmpty()) {
            model.addRow(new Object[]{"-", "No users found", "-"});
            return;
        }

        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    private void showUserDialog(
            boolean editMode,
            JTable table,
            DefaultTableModel model
    ) {

        int row = table.getSelectedRow();

        if (editMode && row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }

        int modelRow = row < 0 ? -1 : table.convertRowIndexToModel(row);
        int id = editMode
                ? Integer.parseInt(model.getValueAt(modelRow, 0).toString())
                : 0;
        String currentUsername = editMode
                ? model.getValueAt(modelRow, 1).toString()
                : "";
        String currentRole = editMode
                ? model.getValueAt(modelRow, 2).toString()
                : "Employee";

        JTextField usernameField = new JTextField(currentUsername);
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Employee"});
        roleBox.setSelectedItem(currentRole);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username"));
        form.add(usernameField);
        form.add(new JLabel(editMode ? "New Password" : "Password"));
        form.add(passwordField);
        form.add(new JLabel("Role"));
        form.add(roleBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editMode ? "Edit User" : "Add User",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        Object selectedRole = roleBox.getSelectedItem();
        String role = selectedRole == null ? "Employee" : selectedRole.toString();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.");
            return;
        }

        if (!editMode && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password.");
            return;
        }

        boolean success = editMode
                ? DashboardUtil.updateUser(id, username, password, role)
                : DashboardUtil.addUser(username, password, role);

        if (success) {
            refreshUsersTable(model);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save user. Check duplicate usernames or database connection."
            );
        }
    }

    private void deleteSelectedUser(JTable table, DefaultTableModel model) {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String idText = model.getValueAt(modelRow, 0).toString();

        if ("-".equals(idText)) {
            return;
        }

        int id = Integer.parseInt(idText);
        String username = model.getValueAt(modelRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete user " + username + "?",
                "Delete User",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION && DashboardUtil.deleteUser(id)) {
            refreshUsersTable(model);
        }
    }

    private JPanel createBestFoodsTableCard(String title, Dimension size) {

        JPanel card = createBaseCard(size);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 3),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = cardTitle(title, 16);

        String[] columns = {"#", "FOOD", "SOLD"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ArrayList<Object[]> rows = DashboardUtil.getBestFoodRows();

        if (rows.isEmpty()) {
            model.addRow(new Object[]{"-", "No completed sales yet", "-"});
        } else {
            for (Object[] row : rows) {
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(UIUtil.BRAND_RED);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(UIUtil.BRAND_RED);
        table.setSelectionBackground(UIUtil.BRAND_YELLOW);
        table.setSelectionForeground(UIUtil.INK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtil.BRAND_RED, 1));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBaseCard() {
        return createBaseCard(new Dimension(277, 196));
    }

    private JPanel createBaseCard(Dimension size) {

        JPanel card = new JPanel(null);
        card.setPreferredSize(size);
        card.setBackground(UIUtil.BRAND_YELLOW_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 3),
                BorderFactory.createLineBorder(Color.WHITE, 5)
        ));

        return card;
    }

    private JLabel cardTitle(String text, int size) {

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(UIUtil.BRAND_RED);

        return label;
    }

    private JPanel createBottom() {

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);

        JButton back = UIUtil.actionButton("BACK TO ADMIN", UIUtil.BRAND_RED);
        back.setPreferredSize(new Dimension(160, 42));
        back.addActionListener(e -> {
            dispose();
            new AdminFrame();
        });

        bottom.add(back);

        return bottom;
    }
}
