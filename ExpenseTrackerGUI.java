import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExpenseTrackerGUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private JLabel totalLabel;
    private final String FILE_NAME = "expenses.csv";

    public ExpenseTrackerGUI() {
        frame = new JFrame("Smart Expense Tracker");
        frame.setSize(800, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(30, 30, 30));

        // Table Model
        String[] columns = {"Date", "Category", "Amount (₹)"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setBackground(new Color(60, 60, 60));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);

        // Total Expense Label
        totalLabel = new JLabel("Total: ₹0", SwingConstants.CENTER);
        totalLabel.setForeground(Color.YELLOW);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Load previous data
        loadExpenses();

        // Panel for input fields
        JPanel panel = new JPanel(new GridLayout(2, 3, 5, 5));
        panel.setBackground(new Color(50, 50, 50));

        amountField = new JTextField();
        String[] categories = {"Food", "Transport", "Entertainment", "Bills", "Shopping", "Health", "Other"};
        categoryBox = new JComboBox<>(categories);
        JButton addButton = new JButton("Add Expense");
        JButton clearButton = new JButton("Clear Data");

        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        clearButton.setBackground(new Color(178, 34, 34));
        clearButton.setForeground(Color.WHITE);

        JLabel amountLabel = new JLabel("Amount:", SwingConstants.RIGHT);
        JLabel categoryLabel = new JLabel("Category:", SwingConstants.RIGHT);
        amountLabel.setForeground(Color.WHITE);
        categoryLabel.setForeground(Color.WHITE);

        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(categoryLabel);
        panel.add(categoryBox);
        panel.add(addButton);
        panel.add(clearButton);

        // Button Actions
        addButton.addActionListener(this::addExpense);
        clearButton.addActionListener(e -> clearExpenses());

        // Layout
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(totalLabel, BorderLayout.NORTH);
        frame.setVisible(true);
    }

    private void addExpense(ActionEvent e) {
        String amount = amountField.getText().trim();
        String category = (categoryBox.getSelectedItem() != null) ? (String) categoryBox.getSelectedItem() : "Other";
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (amount.isEmpty() || !amount.matches("\\d+(\\.\\d{1,2})?")) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid numeric amount!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.addRow(new Object[]{date, category, amount});
        saveExpense(date, category, amount);
        updateTotal();
        amountField.setText("");
    }

    private void saveExpense(String date, String category, String amount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(date + "," + category + "," + amount);
            writer.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error saving data!", "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadExpenses() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[2].matches("\\d+(\\.\\d{1,2})?")) {
                    model.addRow(data);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error loading data!", "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        updateTotal();
    }

    private void clearExpenses() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to clear all data?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                writer.write(""); // Clears file
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error clearing data!", "File Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            model.setRowCount(0);
            updateTotal();
        }
    }

    private void updateTotal() {
        if (model == null) return; // Prevents NullPointerException
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                total += Double.parseDouble(model.getValueAt(i, 2).toString());
            } catch (NumberFormatException | NullPointerException ex) {
                System.err.println("Skipping invalid amount: " + model.getValueAt(i, 2));
            }
        }
        if (totalLabel != null) {
            totalLabel.setText("Total: ₹" + String.format("%.2f", total));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}
