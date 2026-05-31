package frame;

import util.CartUtil;
import util.FoodUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class MenuFrame extends JFrame {

    private final String orderType;
    private JLabel orderCount;
    private JLabel subtotal;

    public MenuFrame(String type) {

        this.orderType = type;
        CartUtil.setOrderType(type);

        setTitle("Menu - " + type);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(createContent());
        refreshOrderStrip();
        setVisible(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIUtil.APP_BG);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createCategoryGrid(), BorderLayout.CENTER);
        root.add(createOrderStrip(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeader() {

        return UIUtil.brandHeader("MACDOLIBEE", "MENU", orderType);
    }

    private JPanel createCategoryGrid() {

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setBackground(UIUtil.APP_BG);
        wrapper.setBorder(new EmptyBorder(34, 42, 34, 42));

        JLabel title = UIUtil.titleLabel("CHOOSE A MENU CATEGORY", 30);
        title.setForeground(UIUtil.BRAND_RED);
        wrapper.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 4, 18, 18));
        grid.setOpaque(false);

        ArrayList<String> categories = FoodUtil.getCategories();

        if (categories.isEmpty()) {
            JLabel empty = UIUtil.titleLabel(
                    "No categories found. Please check your foods table.",
                    18
            );
            wrapper.add(empty, BorderLayout.CENTER);
            return wrapper;
        }

        for (String category : categories) {
            JButton button = createCategoryButton(category);
            button.addActionListener(e -> {
                dispose();
                new FoodFrame(category, orderType);
            });
            grid.add(button);
        }

        wrapper.add(grid, BorderLayout.CENTER);

        return wrapper;
    }

    private JButton createCategoryButton(String category) {

        JButton button = UIUtil.adminButton(category);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(240, 120));
        return button;
    }

    private JPanel createOrderStrip() {

        JPanel strip = new JPanel(new BorderLayout(16, 0));
        strip.setBackground(UIUtil.BRAND_RED);
        strip.setBorder(new EmptyBorder(16, 28, 16, 28));

        JLabel title = new JLabel("MY ORDER - " + orderType);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(UIUtil.BRAND_YELLOW);

        JPanel totals = new JPanel(new GridLayout(1, 2, 28, 0));
        totals.setOpaque(false);

        orderCount = new JLabel();
        orderCount.setFont(new Font("Arial", Font.BOLD, 16));
        orderCount.setForeground(Color.WHITE);

        subtotal = new JLabel();
        subtotal.setFont(new Font("Arial", Font.BOLD, 16));
        subtotal.setForeground(Color.WHITE);

        totals.add(orderCount);
        totals.add(subtotal);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton back = UIUtil.actionButton("BACK", UIUtil.BRAND_RED_DARK);
        back.addActionListener(e -> {
            dispose();
            new CustomerFrame();
        });

        JButton summary = UIUtil.actionButton("VIEW ORDER", UIUtil.BRAND_YELLOW);
        summary.setForeground(UIUtil.INK);
        summary.addActionListener(e -> {
            if (CartUtil.getCart().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please add an item first.");
                return;
            }

            dispose();
            new SummaryFrame();
        });

        actions.add(back);
        actions.add(summary);

        strip.add(title, BorderLayout.WEST);
        strip.add(totals, BorderLayout.CENTER);
        strip.add(actions, BorderLayout.EAST);

        return strip;
    }

    private void refreshOrderStrip() {

        if (orderCount == null || subtotal == null) {
            return;
        }

        orderCount.setText("ITEMS: " + CartUtil.getTotalUnits());
        subtotal.setText("SUBTOTAL: " + UIUtil.money(CartUtil.getSubtotal()));
    }
}
