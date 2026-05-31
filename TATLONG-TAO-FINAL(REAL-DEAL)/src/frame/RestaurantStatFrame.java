package frame;

import util.DBConnection;
import util.FoodUtil;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class RestaurantStatFrame extends JFrame {

    private DefaultTableModel foodModel;
    private DefaultTableModel ingredientModel;
    private JTable foodTable;
    private JTable ingredientTable;

    public RestaurantStatFrame() {

        UIUtil.closeOtherWindows(RestaurantStatFrame.class, this);

        setTitle("Restaurant Stat");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(24, 24));
        root.setBackground(UIUtil.CREAM);
        root.setBorder(new EmptyBorder(24, 42, 24, 42));

        root.add(UIUtil.brandHeader("MACDOLIBEE", "RESTAURANT STAT", "ADMIN"), BorderLayout.NORTH);
        root.add(createTables(), BorderLayout.CENTER);
        root.add(createBottom(), BorderLayout.SOUTH);

        add(root);
        setVisible(true);
    }

    private JPanel createTables() {

        foodModel = new DefaultTableModel(
                new String[]{"NAME", "CATEGORY", "PRICE", "VAT", "PRICE + VAT"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ingredientModel = new DefaultTableModel(
                new String[]{"NAME", "COST", "VAT", "COST + VAT"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        foodTable = new JTable(foodModel);
        ingredientTable = new JTable(ingredientModel);

        refreshFoodTable();
        refreshIngredientTable();

        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(1, 2, 22, 22));
        grid.setOpaque(false);

        grid.add(createManagedTablePanel("PRICE LIST OF FOOD", foodTable, true));
        grid.add(createManagedTablePanel("INGREDIENTS", ingredientTable, false));

        content.add(grid, BorderLayout.CENTER);
        content.add(createDetailsPanel(), BorderLayout.SOUTH);

        return content;
    }

    private JPanel createManagedTablePanel(
            String title,
            JTable table,
            boolean foodTablePanel
    ) {

        styleTable(table);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_YELLOW, 4),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel label = UIUtil.titleLabel(title, 16);
        label.setForeground(UIUtil.BRAND_RED);

        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(createCrudButtons(foodTablePanel), BorderLayout.SOUTH);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.getTableHeader().setBackground(UIUtil.BRAND_YELLOW_LIGHT);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setGridColor(UIUtil.BRAND_YELLOW);
        table.setSelectionBackground(UIUtil.BRAND_YELLOW_LIGHT);
    }

    private JPanel createCrudButtons(boolean foodButtons) {

        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.setOpaque(false);

        JButton add = UIUtil.lightButton("ADD");
        JButton edit = UIUtil.lightButton("EDIT");
        JButton delete = UIUtil.actionButton("DELETE", UIUtil.BRAND_RED_DARK);

        if (foodButtons) {
            add.addActionListener(e -> showFoodDialog(false));
            edit.addActionListener(e -> showFoodDialog(true));
            delete.addActionListener(e -> deleteSelectedFood());
        } else {
            add.addActionListener(e -> showIngredientDialog(false));
            edit.addActionListener(e -> showIngredientDialog(true));
            delete.addActionListener(e -> deleteSelectedIngredient());
        }

        panel.add(add);
        panel.add(edit);
        panel.add(delete);

        return panel;
    }

    private JPanel createDetailsPanel() {

        JPanel panel = new JPanel(new BorderLayout(18, 0));
        panel.setPreferredSize(new Dimension(0, 155));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BRAND_RED, 4),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = UIUtil.titleLabel("DETAILS", 22);
        title.setForeground(UIUtil.BRAND_RED);
        title.setPreferredSize(new Dimension(180, 0));

        JTextArea details = new JTextArea(
                "Use ADD, EDIT, and DELETE to manage foods and ingredients directly from SQL.\n"
                        + "Food edits affect future menu prices only.\n"
                        + "Completed orders remain transaction records because orders save product_list and sub_total snapshots.\n"
                        + "VAT is computed at 12%, and PRICE + VAT shows the amount after VAT."
        );
        details.setEditable(false);
        details.setOpaque(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("Arial", Font.BOLD, 15));
        details.setForeground(UIUtil.INK);

        panel.add(title, BorderLayout.WEST);
        panel.add(details, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottom() {

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);

        JButton back = UIUtil.actionButton("BACK TO ADMIN", UIUtil.BRAND_RED);
        back.setPreferredSize(new Dimension(180, 48));
        back.addActionListener(e -> {
            dispose();
            new AdminFrame();
        });

        bottom.add(back);

        return bottom;
    }

    private void refreshFoodTable() {

        foodModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT name, category, price FROM foods ORDER BY category, name";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                double price = rs.getDouble("price");
                foodModel.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("category"),
                        formatMoney(price),
                        formatMoney(computeVAT(price)),
                        formatMoney(price + computeVAT(price))
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshIngredientTable() {

        ingredientModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT name, cost FROM ingredients ORDER BY name";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                double cost = rs.getDouble("cost");
                ingredientModel.addRow(new Object[]{
                        rs.getString("name"),
                        formatMoney(cost),
                        formatMoney(computeVAT(cost)),
                        formatMoney(cost + computeVAT(cost))
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFoodDialog(boolean editMode) {

        int row = foodTable.getSelectedRow();

        if (editMode && row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a food to edit.");
            return;
        }

        int modelRow = row < 0 ? -1 : foodTable.convertRowIndexToModel(row);
        String originalName = editMode ? foodModel.getValueAt(modelRow, 0).toString() : "";
        ArrayList<String> categories = FoodUtil.getCategories();

        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No food categories found. Add a food category in the database first."
            );
            return;
        }

        JTextField nameField = new JTextField(editMode ? originalName : "");
        JComboBox<String> categoryBox = new JComboBox<>(
                categories.toArray(new String[0])
        );

        if (editMode) {
            String currentCategory = foodModel.getValueAt(modelRow, 1).toString();
            categoryBox.setSelectedItem(currentCategory.toUpperCase());
        }

        JTextField priceField = new JTextField(
                editMode ? plainWholeNumber(foodModel.getValueAt(modelRow, 2).toString()) : ""
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Category"));
        form.add(categoryBox);
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
        Object selectedCategory = categoryBox.getSelectedItem();
        String category = selectedCategory == null
                ? ""
                : selectedCategory.toString().trim().toUpperCase();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a food name.");
            return;
        }

        if (category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a category.");
            return;
        }

        int price;

        try {
            price = parseWholePesoPrice(priceField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a whole-number price.");
            return;
        }

        try {
            boolean success = editMode
                    ? FoodUtil.updateFood(originalName, name, category, price)
                    : FoodUtil.addFood(name, category, price);

            if (success) {
                refreshFoodTable();
            } else {
                JOptionPane.showMessageDialog(this, "Could not save food. Check duplicate names or database connection.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not save food. Check duplicate names or database connection.");
        }
    }

    private void deleteSelectedFood() {

        int row = foodTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a food to delete.");
            return;
        }

        int modelRow = foodTable.convertRowIndexToModel(row);
        String name = foodModel.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete " + name + "? Completed order records will not change.",
                "Delete Food",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION && FoodUtil.deleteFood(name)) {
            refreshFoodTable();
        }
    }

    private void showIngredientDialog(boolean editMode) {

        int row = ingredientTable.getSelectedRow();

        if (editMode && row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient to edit.");
            return;
        }

        int modelRow = row < 0 ? -1 : ingredientTable.convertRowIndexToModel(row);
        String originalName = editMode ? ingredientModel.getValueAt(modelRow, 0).toString() : "";

        JTextField nameField = new JTextField(editMode ? originalName : "");
        JTextField costField = new JTextField(
                editMode ? plainNumber(ingredientModel.getValueAt(modelRow, 1).toString()) : ""
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Cost"));
        form.add(costField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editMode ? "Edit Ingredient" : "Add Ingredient",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            double cost = Double.parseDouble(costField.getText().trim());

            if (name.isEmpty()) {
                throw new IllegalArgumentException();
            }

            boolean success = editMode
                    ? FoodUtil.updateIngredient(originalName, name, cost)
                    : FoodUtil.addIngredient(name, cost);

            if (success) {
                refreshIngredientTable();
            } else {
                JOptionPane.showMessageDialog(this, "Could not save ingredient. Check duplicate names or database connection.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid ingredient name and cost.");
        }
    }

    private void deleteSelectedIngredient() {

        int row = ingredientTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient to delete.");
            return;
        }

        int modelRow = ingredientTable.convertRowIndexToModel(row);
        String name = ingredientModel.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete " + name + "?",
                "Delete Ingredient",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION && FoodUtil.deleteIngredient(name)) {
            refreshIngredientTable();
        }
    }

    private double computeVAT(double amount) {
        return amount * 0.12;
    }

    private String formatMoney(double amount) {
        return "PHP " + String.format("%.2f", amount);
    }

    private String plainNumber(String money) {
        return money.replace("PHP", "").replace(",", "").trim();
    }

    private String plainWholeNumber(String money) {
        return String.valueOf(parseWholePesoPrice(money));
    }

    private int parseWholePesoPrice(String text) {

        String cleaned = plainNumber(text);

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
