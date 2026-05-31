package frame;

import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BarcodeFrame extends JFrame {

    public BarcodeFrame(String barcode) {

        setTitle("Barcode");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIUtil.CREAM);

        JPanel receipt = new JPanel(new BorderLayout(0, 24));
        receipt.setPreferredSize(new Dimension(700, 470));
        receipt.setBackground(Color.WHITE);
        receipt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 8),
                new EmptyBorder(32, 32, 32, 32)
        ));

        JLabel title = new JLabel(
                "<html><center>MACDOLIBEE<br>THANK YOU</center></html>",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(UIUtil.BRAND_RED);

        JLabel barcodeLabel = new JLabel(barcode, SwingConstants.CENTER);
        barcodeLabel.setFont(new Font("Arial", Font.BOLD, 34));
        barcodeLabel.setOpaque(true);
        barcodeLabel.setBackground(UIUtil.BRAND_YELLOW_LIGHT);
        barcodeLabel.setBorder(BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 3));

        JButton back = UIUtil.actionButton("RETURN", UIUtil.BRAND_RED);
        back.setPreferredSize(new Dimension(150, 50));
        back.addActionListener(e -> {
            dispose();
            new CustomerFrame();
        });

        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setOpaque(false);
        action.add(back);

        receipt.add(title, BorderLayout.NORTH);
        receipt.add(barcodeLabel, BorderLayout.CENTER);
        receipt.add(action, BorderLayout.SOUTH);

        root.add(receipt);

        add(root);
        setVisible(true);
    }
}
