package electricity.billing.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerForm {

    private JPanel panel; 
    private JTextField nameField, addressField, meterField;
    private JComboBox<String> typeCombo;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerForm() {
        initComponents();
        loadCustomerData();
    }

  
    public JPanel getPanel() {
        return panel;
    }

    private void initComponents() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(StyleConstants.HEADING_FONT);
        titleLabel.setForeground(StyleConstants.PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

    
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        StyleConstants.styleCard(formPanel);

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Address:"));
        addressField = new JTextField();
        formPanel.add(addressField);

        formPanel.add(new JLabel("Meter Number:"));
        meterField = new JTextField();
        formPanel.add(meterField);

        formPanel.add(new JLabel("Connection Type:"));
        typeCombo = new JComboBox<>(new String[]{"Residential", "Commercial", "Industrial"});
        formPanel.add(typeCombo);

      
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Customer");
        JButton clearButton = new JButton("Clear");

        StyleConstants.styleButton(addButton);
        StyleConstants.styleButton(clearButton);
        clearButton.setBackground(StyleConstants.ACCENT_COLOR);

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        formPanel.add(new JLabel()); 
        formPanel.add(buttonPanel);

        
        String[] columns = {"ID", "Name", "Meter No", "Type", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(customerTable);

        addButton.addActionListener(e -> addCustomer());
        clearButton.addActionListener(e -> clearForm());

       
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);

       
        JButton totalBtn = new JButton("Show Total Customers");
        StyleConstants.styleButton(totalBtn);
        totalBtn.addActionListener(e -> showTotalCustomers());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        bottomPanel.add(totalBtn);

     
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addCustomer() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO customers (name, address, meter_number, connection_type) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, addressField.getText());
            pstmt.setString(3, meterField.getText());
            pstmt.setString(4, (String) typeCombo.getSelectedItem());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Customer added successfully!");
                clearForm();
                loadCustomerData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("");
        addressField.setText("");
        meterField.setText("");
        typeCombo.setSelectedIndex(0);
    }

    private void loadCustomerData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM customers ORDER BY customer_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("meter_number"),
                    rs.getString("connection_type"),
                    rs.getString("address")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error loading customers: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTotalCustomers() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM customers");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt(1);
                JOptionPane.showMessageDialog(panel,
                        "Total Customers: " + total,
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
