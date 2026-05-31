package frame;

import util.BarcodeUtil;
import util.CartUtil;
import util.FoodUtil;
import util.OrderUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class SummaryFrame extends JFrame {

    private JPanel listPanel;
    private JLabel totalLabel;
    private JLabel itemLabel;

    public SummaryFrame() {

        setTitle("Summary");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(createContent());
        refreshRows();
        setVisible(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(24, 24));
        root.setBackground(UIUtil.APP_BG);
        root.setBorder(new EmptyBorder(24, 42, 24, 42));

        JLabel header = UIUtil.titleLabel("ORDER SUMMARY", 34);
        header.setForeground(UIUtil.BRAND_RED);
        root.add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setBackground(UIUtil.APP_BG);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 200)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        root.add(createTotalsPanel(), BorderLayout.EAST);
        root.add(createActionsPanel(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createTotalsPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 5),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel title = UIUtil.titleLabel("PAYMENT", 22);
        panel.add(title, BorderLayout.NORTH);

        JPanel totals = new JPanel(new GridLayout(4, 1, 0, 12));
        totals.setOpaque(false);

        itemLabel = UIUtil.titleLabel("", 18);
        totalLabel = UIUtil.titleLabel("", 28);

        totals.add(new JLabel("Order Type: " + CartUtil.getOrderType(), SwingConstants.CENTER));
        totals.add(itemLabel);
        totals.add(new JSeparator());
        totals.add(totalLabel);

        panel.add(totals, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionsPanel() {

        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);

        JButton back = UIUtil.lightButton("BACK TO MENU");
        back.setPreferredSize(new Dimension(180, 48));
        back.addActionListener(e -> {
            dispose();
            new MenuFrame(CartUtil.getOrderType());
        });

        JButton clear = UIUtil.actionButton("CANCEL ORDER", UIUtil.RED);
        clear.addActionListener(e -> {
            CartUtil.clearCart();
            dispose();
            new CustomerFrame();
        });

        JButton order = UIUtil.actionButton("PLACE ORDER", UIUtil.BRAND_YELLOW);
        order.setForeground(UIUtil.INK);
        order.addActionListener(e -> placeOrder());

        JPanel right = new JPanel(new GridLayout(1, 2, 14, 0));
        right.setOpaque(false);
        right.add(clear);
        right.add(order);

        panel.add(back, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private void refreshRows() {

        listPanel.removeAll();

        if (CartUtil.getCart().isEmpty()) {
            JLabel empty = UIUtil.titleLabel("Your cart is empty.", 20);
            empty.setBorder(new EmptyBorder(50, 0, 50, 0));
            listPanel.add(empty);
        } else {
            for (Map.Entry<String, Integer> entry : CartUtil.getCart().entrySet()) {
                listPanel.add(createRow(entry.getKey(), entry.getValue()));
                listPanel.add(Box.createVerticalStrut(12));
            }
        }

        itemLabel.setText("Items: " + CartUtil.getTotalUnits());
        totalLabel.setText(UIUtil.money(CartUtil.getSubtotalWithVat()));

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createRow(String food, int qty) {

        double price = FoodUtil.getPriceWithVat(food);
        double subtotal = price * qty;

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        row.setPreferredSize(new Dimension(0, 92));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 2),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 6));
        text.setOpaque(false);

        JLabel name = new JLabel(food);
        name.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel priceText = new JLabel(UIUtil.money(price) + " each");
        priceText.setFont(new Font("Arial", Font.PLAIN, 14));

        text.add(name);
        text.add(priceText);

        JPanel controls = new JPanel(new GridLayout(1, 3, 8, 0));
        controls.setOpaque(false);
        controls.setPreferredSize(new Dimension(160, 42));

        JButton minus = UIUtil.lightButton("-");
        JLabel quantity = UIUtil.titleLabel(String.valueOf(qty), 16);
        JButton plus = UIUtil.lightButton("+");

        minus.addActionListener(e -> {
            CartUtil.removeItem(food);
            refreshRows();
        });

        plus.addActionListener(e -> {
            CartUtil.addItem(food);
            refreshRows();
        });

        controls.add(minus);
        controls.add(quantity);
        controls.add(plus);

        JLabel subtotalText = UIUtil.titleLabel(UIUtil.money(subtotal), 18);
        subtotalText.setPreferredSize(new Dimension(150, 40));

        row.add(text, BorderLayout.CENTER);
        row.add(controls, BorderLayout.WEST);
        row.add(subtotalText, BorderLayout.EAST);

        return row;
    }

    private void placeOrder() {

        if (CartUtil.getCart().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }

        String barcode = BarcodeUtil.generateBarcodeID();

        if (!OrderUtil.saveOrder(barcode)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save the order. Please check the database connection."
            );
            return;
        }

        CartUtil.clearCart();
        dispose();
        new BarcodeFrame(barcode);
    }
}
