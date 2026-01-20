package electricity.billing.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class MeterReadingForm {

    private JPanel panel;
    private JTextField customerIdField, currentReadingField;
    private JTable readingsTable;
    private DefaultTableModel tableModel;
    private JTextArea resultArea;

    public MeterReadingForm() {
        initComponents();
        loadReadingsData();
    }

    public JPanel getPanel() {
        return panel;
    }

    private void initComponents() {

        
        panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Meter Reading Management");
        titleLabel.setFont(StyleConstants.HEADING_FONT);
        titleLabel.setForeground(StyleConstants.PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

     
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);

        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

       
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        StyleConstants.styleCard(formPanel);

        formPanel.add(new JLabel("Customer ID:"));
        customerIdField = new JTextField();
        formPanel.add(customerIdField);

        formPanel.add(new JLabel("Current Reading:"));
        currentReadingField = new JTextField();
        formPanel.add(currentReadingField);

       
        JButton recordButton = new JButton("Record Reading");
        JButton clearButton = new JButton("Clear");
        JButton checkButton = new JButton("Check Previous Reading");
        JButton refreshButton = new JButton("Refresh Data");

        StyleConstants.styleButton(recordButton);
        StyleConstants.styleButton(clearButton);
        StyleConstants.styleButton(checkButton);
        StyleConstants.styleButton(refreshButton);

        clearButton.setBackground(StyleConstants.ACCENT_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(recordButton);
        buttonPanel.add(checkButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        formPanel.add(new JLabel());
        formPanel.add(buttonPanel);

       
        JPanel resultsPanel = new JPanel(new BorderLayout());
        StyleConstants.styleCard(resultsPanel);

        JLabel resultsLabel = new JLabel("Reading Results:");
        resultArea = new JTextArea(8, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setForeground(Color.BLACK);
        resultArea.setBackground(new Color(250, 250, 250));
        resultArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane resultsScroll = new JScrollPane(resultArea);

        resultsPanel.add(resultsLabel, BorderLayout.NORTH);
        resultsPanel.add(resultsScroll, BorderLayout.CENTER);

       
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(resultsPanel, BorderLayout.CENTER);

      
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel tableLabel = new JLabel("Recent Meter Readings");
        tableLabel.setFont(StyleConstants.SUBHEADING_FONT);
        tableLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        String[] columns = {"Reading ID", "Customer ID", "Date", "Previous", "Current", "Units"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        readingsTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(readingsTable);

        rightPanel.add(tableLabel, BorderLayout.NORTH);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

       
        JButton todayBtn = new JButton("Show Today’s Readings Count");
        StyleConstants.styleButton(todayBtn);
        todayBtn.setBackground(new Color(41, 128, 185));
        todayBtn.setForeground(Color.WHITE);

        todayBtn.addActionListener(e -> showTodaysReadingsCount());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.add(todayBtn);

        // Add everything to main panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // CONNECT BUTTONS
        recordButton.addActionListener(e -> recordReading());
        clearButton.addActionListener(e -> clearForm());
        checkButton.addActionListener(e -> checkPreviousReading());
        refreshButton.addActionListener(e -> loadReadingsData());
    }

    
    private void recordReading() {
        String custText = customerIdField.getText().trim();
        String currText = currentReadingField.getText().trim();

        if (custText.isEmpty() || currText.isEmpty()) {
            showResult("❌ Please fill in all fields!", Color.RED);
            return;
        }

        try {
            int customerId = Integer.parseInt(custText);
            double current = Double.parseDouble(currText);

            if (!customerExists(customerId)) {
                showResult("❌ Customer not found!", Color.RED);
                return;
            }

            double previous = getPreviousReading(customerId);
            if (current < previous) {
                showResult("❌ Current reading cannot be less than previous (" + previous + ")", Color.RED);
                return;
            }

            double units = current - previous;

            if (insertMeterReading(customerId, current, previous, units)) {
                showResult("✅ Reading recorded.\nPrevious: " + previous +
                        "\nCurrent: " + current +
                        "\nUnits: " + units, new Color(0, 128, 0));
                clearForm();
                loadReadingsData();
            }

        } catch (NumberFormatException e) {
            showResult("❌ Invalid input! Please enter valid numbers.", Color.RED);
        }
    }

    private boolean insertMeterReading(int customerId, double current, double previous, double units) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "INSERT INTO meter_readings (customer_id, reading_date, current_reading, previous_reading, units_consumed) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setDouble(3, current);
            ps.setDouble(4, previous);
            ps.setDouble(5, units);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            showResult("❌ Error recording reading: " + e.getMessage(), Color.RED);
            return false;
        }
    }

    private boolean customerExists(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT customer_id FROM customers WHERE customer_id = ?");
            ps.setInt(1, id);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            showResult("❌ Database error: " + e.getMessage(), Color.RED);
            return false;
        }
    }

    private double getPreviousReading(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT current_reading FROM meter_readings WHERE customer_id = ? ORDER BY reading_date DESC LIMIT 1"
            );
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("current_reading");

        } catch (SQLException ignored) {}

        return 0.0;
    }

    private void checkPreviousReading() {
        String text = customerIdField.getText().trim();
        if (text.isEmpty()) {
            showResult("❌ Enter Customer ID first!", Color.RED);
            return;
        }

        try {
            int id = Integer.parseInt(text);
            double prev = getPreviousReading(id);

            if (prev == 0.0)
                showResult("ℹ️ No previous reading found.", Color.BLUE);
            else
                showResult("📊 Previous Reading: " + prev, Color.BLUE);

        } catch (NumberFormatException e) {
            showResult("❌ Invalid Customer ID!", Color.RED);
        }
    }

    private void clearForm() {
        customerIdField.setText("");
        currentReadingField.setText("");
        resultArea.setText("");
    }

    private void showResult(String msg, Color c) {
        resultArea.setForeground(c);
        resultArea.setText(msg);
    }

    private void loadReadingsData() {
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
        	String sql = 
        	        "SELECT reading_id, customer_id, reading_date, previous_reading, current_reading, units_consumed " +
        	        "FROM meter_readings " +
        	        "ORDER BY reading_date DESC LIMIT 20";


            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("reading_id"),
                        rs.getInt("customer_id"),
                        rs.getDate("reading_date"),
                        String.format("%.2f", rs.getDouble("previous_reading")),
                        String.format("%.2f", rs.getDouble("current_reading")),
                        String.format("%.2f", rs.getDouble("units_consumed"))
                });
            }

        } catch (SQLException e) {
            showResult("❌ Error loading readings: " + e.getMessage(), Color.RED);
        }
    }


    private void showTodaysReadingsCount() {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM meter_readings WHERE reading_date = CURDATE()"
            );

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                JOptionPane.showMessageDialog(panel,
                        "Today's Total Readings: " + count,
                        "Today's Readings", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            showResult("❌ Error counting today’s readings: " + e.getMessage(), Color.RED);
        }
    }
}
