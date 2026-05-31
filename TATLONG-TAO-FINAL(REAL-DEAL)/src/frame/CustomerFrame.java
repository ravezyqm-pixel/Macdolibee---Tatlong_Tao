package frame;

import util.CartUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerFrame extends JFrame {

    public CustomerFrame() {

        UIUtil.closeOtherWindows(CustomerFrame.class, this);

        setTitle("Dine Option");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(createContent());
        setVisible(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIUtil.CREAM);

        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(560, 620));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 8),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel title = new JLabel("MACDOLIBEE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 46));
        title.setForeground(UIUtil.BRAND_RED);
        title.setBounds(80, 70, 400, 58);

        JLabel subtitle = new JLabel("CHOOSE YOUR ORDER TYPE", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.BOLD, 18));
        subtitle.setForeground(UIUtil.INK);
        subtitle.setBounds(80, 145, 400, 32);

        JButton dineIn = createChoiceButton("DINE IN");
        dineIn.setBounds(130, 240, 300, 78);

        JButton takeOut = createChoiceButton("TAKE OUT");
        takeOut.setBounds(130, 360, 300, 78);

        dineIn.addActionListener(e -> startOrder("DINE IN"));
        takeOut.addActionListener(e -> startOrder("TAKE OUT"));

        panel.add(title);
        panel.add(subtitle);
        panel.add(dineIn);
        panel.add(takeOut);

        root.add(panel);

        return root;
    }

    private JButton createChoiceButton(String text) {

        JButton button = UIUtil.actionButton(text, UIUtil.BRAND_RED);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBorder(BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 4));
        return button;
    }

    private void startOrder(String type) {

        CartUtil.clearCart();
        CartUtil.setOrderType(type);

        dispose();
        new MenuFrame(type);
    }
}
