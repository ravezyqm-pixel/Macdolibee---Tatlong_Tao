package frame;

import util.DashboardUtil;
import util.FoodUtil;
import util.OrderRecord;
import util.OrderUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.ArrayList;

public class AdminFrame extends JFrame {

    private final String userRole;
    private final boolean adminAccess;

    public AdminFrame() {
        this("admin");
    }

    public AdminFrame(String userRole) {

        this.userRole = normalizeRole(userRole);
        adminAccess = this.userRole.equalsIgnoreCase("admin");

        UIUtil.closeOtherWindows(AdminFrame.class, this);

        setTitle(adminAccess ? "Admin Dashboard" : "Employee Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        OrderUtil.backfillMissingSubTotals();

        add(createMainPanel());
        setVisible(true);
    }

    private JPanel createMainPanel() {

        JPanel root = new JPanel(new BorderLayout(24, 0));
        root.setBackground(UIUtil.APP_BG);
        root.setBorder(new EmptyBorder(24, 24, 12, 24));

        root.add(createFoodContainer(), BorderLayout.WEST);
        root.add(createCenterArea(), BorderLayout.CENTER);
        root.add(createToolsPanel(), BorderLayout.EAST);
        root.add(createFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createFoodContainer() {

        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setPreferredSize(new Dimension(275, 0));
        container.setBackground(UIUtil.PANEL_GRAY);
        container.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UIUtil.titleLabel("FOOD CONTAINER", 16);
        title.setForeground(Color.WHITE);
        container.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        ArrayList<String> categories = FoodUtil.getCategories();

        if (categories.isEmpty()) {
            JLabel empty = new JLabel(
                    "<html><center>No food categories found.<br>Check the foods table.</center></html>",
                    SwingConstants.CENTER
            );
            empty.setForeground(Color.WHITE);
            buttons.add(empty);
        } else {
            for (String category : categories) {
                JButton button = UIUtil.adminButton(category);
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
                button.addActionListener(e -> new AdminFoodViewerFrame(category));

                buttons.add(button);
                buttons.add(Box.createVerticalStrut(12));
            }
        }

        JScrollPane scroll = new JScrollPane(buttons);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        container.add(scroll, BorderLayout.CENTER);

        return container;
    }

    private JPanel createCenterArea() {

        JPanel center = new JPanel(new BorderLayout(0, 24));
        center.setOpaque(false);

        center.add(createRecordPanel(), BorderLayout.NORTH);
        center.add(createOrderTablePanel(), BorderLayout.CENTER);

        return center;
    }

    private JPanel createRecordPanel() {

        JPanel record = new JPanel(new BorderLayout());
        record.setPreferredSize(new Dimension(0, 225));
        record.setBackground(UIUtil.RECORD_GRAY);
        record.setBorder(new EmptyBorder(24, 40, 36, 40));

        JLabel title = UIUtil.titleLabel("RECORD", 16);
        title.setForeground(UIUtil.BRAND_RED_DARK);
        record.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);

        cards.add(createStatCard("TOTAL DISH", DashboardUtil.getTotalFoods()));
        cards.add(Box.createHorizontalStrut(120));
        cards.add(createStatCard("TOTAL ORDERS", DashboardUtil.getTotalOrders()));

        record.add(cards, BorderLayout.CENTER);

        return record;
    }

    private JPanel createStatCard(String label, int count) {

        JPanel card = new JPanel(null);
        card.setPreferredSize(new Dimension(225, 113));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(UIUtil.BRAND_RED, 2));

        JLabel text = UIUtil.titleLabel(label, 16);
        text.setBounds(0, 24, 225, 24);

        JLabel value = UIUtil.titleLabel(String.valueOf(count), 16);
        value.setBounds(0, 60, 225, 24);

        card.add(text);
        card.add(value);

        return card;
    }

    private JPanel createOrderTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 6),
                new EmptyBorder(18, 30, 30, 30)
        ));

        JLabel title = UIUtil.titleLabel("ORDER DATA TABLE", 16);
        title.setForeground(UIUtil.BRAND_RED_DARK);
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {
                "NUM",
                "ID",
                "UNIT",
                "SUB TOTAL",
                "PRODUCT LIST",
                "BARCODE",
                "TIME",
                "STATUS",
                "ACTION"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int num = 1;

        for (OrderRecord record : DashboardUtil.getOrderRecords()) {
            model.addRow(new Object[]{
                    num++,
                    record.getId(),
                    record.getUnitCount(),
                    UIUtil.money(record.getSubTotal()),
                    record.getProductList(),
                    record.getBarcode(),
                    record.getOrderTime(),
                    record.getStatus(),
                    isCompleted(record.getStatus()) ? "" : "COMPLETE"
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(UIUtil.BRAND_YELLOW_LIGHT);
        table.setGridColor(UIUtil.BRAND_YELLOW);
        table.setSelectionBackground(UIUtil.BUTTON_BLUE_DARK);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if (row < 0 || column != table.getColumnModel().getColumnIndex("ACTION")) {
                    return;
                }

                int modelRow = table.convertRowIndexToModel(row);
                String action = String.valueOf(model.getValueAt(modelRow, 8));

                if (!"COMPLETE".equalsIgnoreCase(action)) {
                    return;
                }

                int orderId = Integer.parseInt(String.valueOf(model.getValueAt(modelRow, 1)));

                if (OrderUtil.completeOrder(orderId)) {
                    model.setValueAt("COMPLETED", modelRow, 7);
                    model.setValueAt("", modelRow, 8);
                } else {
                    JOptionPane.showMessageDialog(
                            AdminFrame.this,
                            "Could not complete order #" + orderId + ". Please check the database."
                    );
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(new CompleteButtonRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(55);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(320);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(128, 145, 160)));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private boolean isCompleted(String status) {
        return status != null && status.equalsIgnoreCase("COMPLETED");
    }

    private static class CompleteButtonRenderer extends JButton implements TableCellRenderer {

        CompleteButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String text = value == null ? "" : value.toString();
            setText(text);
            setEnabled(!text.isEmpty());

            if (text.isEmpty()) {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder());
            } else {
                setForeground(Color.WHITE);
                setBackground(UIUtil.BRAND_RED);
                setBorder(BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 2));
            }

            return this;
        }
    }

    private JPanel createToolsPanel() {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(275, 0));

        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setPreferredSize(new Dimension(275, 325));
        panel.setBackground(UIUtil.PANEL_GRAY);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UIUtil.titleLabel("TOOLS", 16);
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new GridLayout(2, 1, 0, 14));

        JButton dashboard = UIUtil.adminButton("DASHBOARD");
        dashboard.setEnabled(adminAccess);
        dashboard.setToolTipText(adminAccess
                ? "Open dashboard"
                : "Dashboard is available for admin accounts only.");
        dashboard.addActionListener(e -> {
            if (!adminAccess) {
                showAdminOnlyMessage("Dashboard");
                return;
            }
            dispose();
            new DashboardFrame();
        });

        JButton stat = UIUtil.adminButton("RESTAURANT STAT");
        stat.setEnabled(adminAccess);
        stat.setToolTipText(adminAccess
                ? "Open restaurant stat"
                : "Restaurant Stat is available for admin accounts only.");
        stat.addActionListener(e -> {
            if (!adminAccess) {
                showAdminOnlyMessage("Restaurant Stat");
                return;
            }
            dispose();
            new RestaurantStatFrame();
        });

        buttons.add(dashboard);
        buttons.add(stat);

        panel.add(buttons, BorderLayout.CENTER);

        wrapper.add(panel, BorderLayout.NORTH);

        return wrapper;
    }

    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIUtil.BRAND_RED);
        footer.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel label = UIUtil.titleLabel(
                adminAccess
                        ? "MACDOLIBEE ADMIN PANEL"
                        : "MACDOLIBEE EMPLOYEE PANEL",
                21
        );
        label.setForeground(UIUtil.BRAND_YELLOW);
        footer.add(label, BorderLayout.CENTER);

        JButton logout = UIUtil.actionButton("LOGOUT", UIUtil.BRAND_RED_DARK);
        logout.setPreferredSize(new Dimension(130, 38));
        logout.addActionListener(e -> {
            UIUtil.closeAllWindowsExcept(this);
            dispose();
            new LoginFrame();
        });

        JButton refresh = UIUtil.actionButton("REFRESH", UIUtil.BRAND_YELLOW);
        refresh.setForeground(UIUtil.INK);
        refresh.setPreferredSize(new Dimension(130, 38));
        refresh.addActionListener(e -> {
            dispose();
            new AdminFrame(userRole);
        });

        footer.add(refresh, BorderLayout.WEST);
        footer.add(logout, BorderLayout.EAST);

        return footer;
    }

    private String normalizeRole(String role) {

        if (role == null || role.trim().isEmpty()) {
            return "employee";
        }

        if (role.equalsIgnoreCase("admin")) {
            return "admin";
        }

        return "employee";
    }

    private void showAdminOnlyMessage(String feature) {
        JOptionPane.showMessageDialog(
                this,
                feature + " is only available for admin accounts."
        );
    }
}
