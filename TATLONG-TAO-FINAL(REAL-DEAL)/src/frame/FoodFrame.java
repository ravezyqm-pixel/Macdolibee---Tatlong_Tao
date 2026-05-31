package frame;

import util.CartUtil;
import util.FoodItem;
import util.FoodUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class FoodFrame extends JFrame {

    private final String selectedCategory;
    private final String orderType;
    private JTextArea orderSummary;
    private JLabel subtotalLabel;
    private JLabel countLabel;

    public FoodFrame(String category) {
        this(category, CartUtil.getOrderType());
    }

    public FoodFrame(String category, String orderType) {

        this.selectedCategory = category;
        this.orderType = orderType;
        CartUtil.setOrderType(orderType);

        setTitle("Food - " + category);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(createContent());
        refreshOrderSummary();
        setVisible(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIUtil.APP_BG);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createCategoryRail(), BorderLayout.WEST);
        root.add(createFoodGrid(), BorderLayout.CENTER);
        root.add(createOrderPanel(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeader() {

        return UIUtil.brandHeader("MACDOLIBEE", selectedCategory, orderType);
    }

    private JPanel createCategoryRail() {

        JPanel rail = new JPanel();
        rail.setPreferredSize(new Dimension(185, 0));
        rail.setBackground(Color.WHITE);
        rail.setBorder(new EmptyBorder(18, 14, 18, 14));
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));

        ArrayList<String> categories = FoodUtil.getCategories();

        for (String category : categories) {
            JButton button = UIUtil.lightButton(category);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

            if (category.equalsIgnoreCase(selectedCategory)) {
                button.setBackground(UIUtil.BRAND_YELLOW);
            }

            button.addActionListener(e -> {
                if (!category.equalsIgnoreCase(selectedCategory)) {
                    dispose();
                    new FoodFrame(category, orderType);
                }
            });

            rail.add(button);
            rail.add(Box.createVerticalStrut(10));
        }

        return rail;
    }

    private JScrollPane createFoodGrid() {

        JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));
        grid.setBackground(UIUtil.APP_BG);
        grid.setBorder(new EmptyBorder(24, 24, 24, 24));

        ArrayList<FoodItem> foods = FoodUtil.getFoodItemsByCategory(selectedCategory);

        if (foods.isEmpty()) {
            JLabel empty = UIUtil.titleLabel("No food found for this category", 20);
            JPanel holder = new JPanel(new GridBagLayout());
            holder.setBackground(UIUtil.APP_BG);
            holder.add(empty);
            return new JScrollPane(holder);
        }

        for (FoodItem food : foods) {
            grid.add(createFoodCard(food));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel createFoodCard(FoodItem food) {

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(270, 260));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 2),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel price = new JLabel(
                UIUtil.money(food.getPriceWithVat()),
                SwingConstants.RIGHT
        );
        price.setFont(new Font("Arial", Font.BOLD, 15));
        price.setForeground(UIUtil.INK);
        card.add(price, BorderLayout.NORTH);

        JLabel image = UIUtil.foodImageLabel(food.getImagePath(), 220, 110);
        card.add(image, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);

        JLabel name = UIUtil.titleLabel(food.getName(), 15);
        bottom.add(name, BorderLayout.NORTH);

        JPanel controls = new JPanel(new GridLayout(1, 3, 8, 0));
        controls.setOpaque(false);

        JButton minus = UIUtil.lightButton("-");
        JLabel qty = UIUtil.titleLabel(String.valueOf(CartUtil.getQuantity(food.getName())), 16);
        JButton plus = UIUtil.lightButton("+");

        minus.addActionListener(e -> {
            CartUtil.removeItem(food.getName());
            qty.setText(String.valueOf(CartUtil.getQuantity(food.getName())));
            refreshOrderSummary();
        });

        plus.addActionListener(e -> {
            CartUtil.addItem(food.getName());
            qty.setText(String.valueOf(CartUtil.getQuantity(food.getName())));
            refreshOrderSummary();
        });

        controls.add(minus);
        controls.add(qty);
        controls.add(plus);

        bottom.add(controls, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createOrderPanel() {

        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setPreferredSize(new Dimension(0, 150));
        panel.setBackground(UIUtil.BRAND_RED);
        panel.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("MY ORDER - " + orderType, SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(UIUtil.BRAND_YELLOW);
        panel.add(title, BorderLayout.WEST);

        orderSummary = new JTextArea();
        orderSummary.setEditable(false);
        orderSummary.setLineWrap(true);
        orderSummary.setWrapStyleWord(true);
        orderSummary.setFont(new Font("Arial", Font.PLAIN, 13));
        orderSummary.setBackground(Color.WHITE);
        orderSummary.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(orderSummary);
        scroll.setPreferredSize(new Dimension(520, 120));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(340, 120));

        JPanel totals = new JPanel(new GridLayout(2, 1, 0, 4));
        totals.setOpaque(false);

        countLabel = new JLabel();
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font("Arial", Font.BOLD, 15));

        subtotalLabel = new JLabel();
        subtotalLabel.setForeground(Color.WHITE);
        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 18));

        totals.add(countLabel);
        totals.add(subtotalLabel);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancel = UIUtil.actionButton("CANCEL ORDER", UIUtil.BRAND_RED_DARK);
        cancel.addActionListener(e -> {
            CartUtil.clearCart();
            dispose();
            new CustomerFrame();
        });

        JButton done = UIUtil.actionButton("DONE", UIUtil.BRAND_YELLOW);
        done.setForeground(UIUtil.INK);
        done.addActionListener(e -> {
            if (CartUtil.getCart().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please add an item first.");
                return;
            }

            dispose();
            new SummaryFrame();
        });

        actions.add(cancel);
        actions.add(done);

        right.add(totals, BorderLayout.NORTH);
        right.add(actions, BorderLayout.SOUTH);

        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private void refreshOrderSummary() {

        if (orderSummary == null || subtotalLabel == null || countLabel == null) {
            return;
        }

        if (CartUtil.getCart().isEmpty()) {
            orderSummary.setText("No items selected.");
        } else {
            StringBuilder text = new StringBuilder();

            for (Map.Entry<String, Integer> entry : CartUtil.getCart().entrySet()) {
                double price = FoodUtil.getPriceWithVat(entry.getKey());
                double subtotal = price * entry.getValue();

                text.append(entry.getValue())
                        .append(" x ")
                        .append(entry.getKey())
                        .append(" - ")
                        .append(UIUtil.money(subtotal))
                        .append("\n");
            }

            orderSummary.setText(text.toString());
        }

        countLabel.setText("ITEMS: " + CartUtil.getTotalUnits());
        subtotalLabel.setText("SUBTOTAL: " + UIUtil.money(CartUtil.getSubtotalWithVat()));
    }
}
