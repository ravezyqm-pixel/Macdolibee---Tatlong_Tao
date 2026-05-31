package frame;

import util.FoodItem;
import util.FoodUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class AdminFoodViewerFrame extends JFrame {

    private final String category;
    private JPanel grid;
    private FoodItem selectedFood;

    public AdminFoodViewerFrame(String category) {

        this.category = category;
        setTitle("Admin Food Viewer - " + category);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(createContent());
        setVisible(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(UIUtil.APP_BG);
        root.setBorder(new EmptyBorder(24, 40, 24, 40));

        JLabel title = UIUtil.titleLabel(category, 34);
        title.setForeground(UIUtil.BRAND_RED);
        root.add(title, BorderLayout.NORTH);

        grid = new JPanel(new GridLayout(0, 4, 18, 18));
        grid.setBackground(UIUtil.APP_BG);

        refreshFoodGrid();

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        root.add(scroll, BorderLayout.CENTER);

        JButton back = UIUtil.lightButton("RETURN");
        back.setPreferredSize(new Dimension(150, 46));
        back.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new BorderLayout(18, 0));
        bottom.setOpaque(false);
        bottom.add(createCrudButtons(), BorderLayout.WEST);
        bottom.add(back, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    private void refreshFoodGrid() {

        selectedFood = null;
        grid.removeAll();

        ArrayList<FoodItem> foods = FoodUtil.getFoodItemsByCategory(category);

        if (foods.isEmpty()) {
            JLabel empty = UIUtil.titleLabel("No food found in this database category", 18);
            JPanel holder = new JPanel(new GridBagLayout());
            holder.setBackground(UIUtil.APP_BG);
            holder.add(empty);
            grid.add(holder);
        } else {
            for (FoodItem food : foods) {
                grid.add(createFoodCard(food));
            }
        }

        grid.revalidate();
        grid.repaint();
    }

    private JPanel createFoodCard(FoodItem food) {

        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setPreferredSize(new Dimension(240, 230));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 2),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel name = UIUtil.titleLabel(food.getName(), 16);
        card.add(name, BorderLayout.NORTH);

        JLabel image = UIUtil.foodImageLabel(food.getImagePath(), 200, 120);
        card.add(image, BorderLayout.CENTER);

        JLabel price = UIUtil.titleLabel(UIUtil.money(food.getPrice()), 16);
        card.add(price, BorderLayout.SOUTH);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedFood = food;
                markSelectedCard(card);
            }
        });

        return card;
    }

    private JPanel createCrudButtons() {

        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(330, 46));

        JButton add = UIUtil.lightButton("ADD");
        JButton edit = UIUtil.lightButton("EDIT");
        JButton delete = UIUtil.actionButton("DELETE", UIUtil.BRAND_RED_DARK);

        add.addActionListener(e -> showFoodDialog(false));
        edit.addActionListener(e -> showFoodDialog(true));
        delete.addActionListener(e -> deleteSelectedFood());

        panel.add(add);
        panel.add(edit);
        panel.add(delete);

        return panel;
    }

    private void showFoodDialog(boolean editMode) {

        if (editMode && selectedFood == null) {
            JOptionPane.showMessageDialog(this, "Please select a food to edit.");
            return;
        }

        String originalName = editMode ? selectedFood.getName() : "";
        JTextField nameField = new JTextField(editMode ? selectedFood.getName() : "");
        JTextField priceField = new JTextField(
                editMode ? String.valueOf(selectedFood.getPrice()) : ""
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Category"));
        form.add(new JLabel(category));
        form.add(new JLabel("Price"));
        form.add(priceField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editMode ? "Edit Food" : "Add Food",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a food name.");
            return;
        }

        int price;

        try {
            price = parseWholePesoPrice(priceField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a whole-number price.");
            return;
        }

        boolean success = editMode
                ? FoodUtil.updateFood(originalName, name, category, price)
                : FoodUtil.addFood(name, category, price);

        if (success) {
            refreshFoodGrid();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save food. Check duplicate names or database connection."
            );
        }
    }

    private void deleteSelectedFood() {

        if (selectedFood == null) {
            JOptionPane.showMessageDialog(this, "Please select a food to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selectedFood.getName() + "? Completed order records will not change.",
                "Delete Food",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION && FoodUtil.deleteFood(selectedFood.getName())) {
            refreshFoodGrid();
        }
    }

    private void markSelectedCard(JPanel selectedCard) {

        for (Component component : grid.getComponents()) {
            if (component instanceof JPanel) {
                ((JPanel) component).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 2),
                        new EmptyBorder(16, 16, 16, 16)
                ));
            }
        }

        selectedCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 4),
                new EmptyBorder(14, 14, 14, 14)
        ));
    }

    private int parseWholePesoPrice(String text) {

        String cleaned = text == null
                ? ""
                : text.replace("PHP", "").replace(",", "").trim();

        if (cleaned.isEmpty()) {
            throw new NumberFormatException("Empty price");
        }

        double value = Double.parseDouble(cleaned);

        if (value < 0 || Math.rint(value) != value) {
            throw new NumberFormatException("Price must be a whole number");
        }

        return (int) value;
    }
}
