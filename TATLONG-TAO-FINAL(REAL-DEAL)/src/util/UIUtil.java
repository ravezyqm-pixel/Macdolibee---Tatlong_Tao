package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class UIUtil {

    public static final Color APP_BG = new Color(255, 248, 232);
    public static final Color PANEL_GRAY = new Color(188, 28, 36);
    public static final Color RECORD_GRAY = new Color(244, 193, 37);
    public static final Color BUTTON_BLUE = new Color(255, 255, 255);
    public static final Color BUTTON_BLUE_DARK = new Color(255, 225, 80);
    public static final Color INK = new Color(25, 31, 38);
    public static final Color GREEN = new Color(188, 28, 36);
    public static final Color RED = new Color(136, 20, 27);
    public static final Color BRAND_RED = new Color(188, 28, 36);
    public static final Color BRAND_RED_DARK = new Color(136, 20, 27);
    public static final Color BRAND_YELLOW = new Color(244, 193, 37);
    public static final Color BRAND_YELLOW_LIGHT = new Color(255, 234, 137);
    public static final Color CREAM = new Color(255, 248, 232);

    private UIUtil() {
    }

    public static JLabel titleLabel(String text, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(INK);
        return label;
    }

    public static JButton adminButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setForeground(INK);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(BRAND_YELLOW, 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(224, 82));
        return button;
    }

    public static JButton actionButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setFocusPainted(false);
        return button;
    }

    public static JButton lightButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(INK);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(BRAND_YELLOW, 2));
        button.setFocusPainted(false);
        return button;
    }

    public static JPanel brandHeader(String left, String center, String right) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BRAND_RED);
        header.setBorder(new EmptyBorder(18, 30, 18, 30));

        JLabel leftLabel = new JLabel(left, SwingConstants.LEFT);
        leftLabel.setFont(new Font("Arial", Font.BOLD, 30));
        leftLabel.setForeground(BRAND_YELLOW);

        JLabel centerLabel = new JLabel(center, SwingConstants.CENTER);
        centerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        centerLabel.setForeground(Color.WHITE);

        JLabel rightLabel = new JLabel(right, SwingConstants.RIGHT);
        rightLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightLabel.setForeground(Color.WHITE);

        header.add(leftLabel, BorderLayout.WEST);
        header.add(centerLabel, BorderLayout.CENTER);
        header.add(rightLabel, BorderLayout.EAST);

        return header;
    }

    public static void closeOtherWindows(Class<?> frameClass, Window currentWindow) {
        for (Window window : Window.getWindows()) {
            if (window != currentWindow && frameClass.isInstance(window)) {
                window.dispose();
            }
        }
    }

    public static void closeAllWindowsExcept(Window currentWindow) {
        for (Window window : Window.getWindows()) {
            if (window != currentWindow) {
                window.dispose();
            }
        }
    }

    public static String money(int amount) {
        return "PHP " + amount;
    }

    public static String money(double amount) {
        return "PHP " + String.format("%.2f", amount);
    }

    public static JLabel foodImageLabel(String imagePath, int width, int height) {

        JLabel label = titleLabel("NO IMAGE", 15);
        label.setOpaque(true);
        label.setBackground(new Color(235, 239, 242));
        label.setPreferredSize(new Dimension(width, height));

        if (imagePath == null || imagePath.trim().isEmpty()) {
            return label;
        }

        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            return label;
        }

        ImageIcon icon = new ImageIcon(imageFile.getPath());

        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            return label;
        }

        Image scaled = icon.getImage().getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        label.setText("");
        label.setIcon(new ImageIcon(scaled));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}
