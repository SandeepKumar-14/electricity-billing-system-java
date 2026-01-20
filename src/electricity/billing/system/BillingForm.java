package electricity.billing.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class BillingForm extends JPanel {

    private JTextField txtCustomerId;
    private JTextArea txtBillDetails;
    private JTable tableBills;
    private DefaultTableModel model;
    private Connection conn;

    public BillingForm() {
        setLayout(new BorderLayout());
        initDB();
        initComponents();
        loadAllBills();
    }

    private void initDB() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {

        // ******** LEFT PANEL ********
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Billing Management"));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Customer ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Customer ID:"), gbc);

        gbc.gridx = 1;
        txtCustomerId = new JTextField(10);
        formPanel.add(txtCustomerId, gbc);

        // Buttons (Generate / Refresh)
        JButton btnGenerate = new JButton("Generate Bill");
        JButton btnRefresh = new JButton("Refresh Bills");

        btnGenerate.setBackground(new Color(46, 134, 193));
        btnGenerate.setForeground(Color.WHITE);

        btnRefresh.setBackground(new Color(40, 167, 69));
        btnRefresh.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnGenerate);
        buttonPanel.add(btnRefresh);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(buttonPanel, gbc);

        // Bill Details TextArea
        txtBillDetails = new JTextArea(10, 30);
        txtBillDetails.setEditable(false);
        txtBillDetails.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtBillDetails.setForeground(new Color(44, 62, 80));
        txtBillDetails.setBorder(BorderFactory.createTitledBorder("Bill Details:"));
        JScrollPane detailsPane = new JScrollPane(txtBillDetails);

        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(detailsPane, BorderLayout.CENTER);

        // ******** TABLE (RIGHT SIDE) ********
        model = new DefaultTableModel(new Object[]{
                "Bill ID", "Customer ID", "Bill Date", "Units", "Total Amount", "Status"}, 0);

        tableBills = new JTable(model);
        JScrollPane tablePane = new JScrollPane(tableBills);
        tablePane.setBorder(BorderFactory.createTitledBorder("All Bills"));

        // Add both panels
        add(leftPanel, BorderLayout.WEST);
        add(tablePane, BorderLayout.CENTER);

        // Buttons actions
        btnGenerate.addActionListener(e -> generateBill());
        btnRefresh.addActionListener(e -> loadAllBills());

        // ******** BOTTOM PANEL: Pending Bills Count ********
        JButton btnPending = new JButton("Show Pending Bills Count");
        btnPending.setBackground(new Color(230, 126, 34));
        btnPending.setForeground(Color.WHITE);
        btnPending.setFocusPainted(false);

        btnPending.addActionListener(e -> showPendingBills());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(btnPending);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ******** LOAD BILLS ********
    public void loadAllBills() {
        try {
            model.setRowCount(0);

            if (conn == null || conn.isClosed()) {
                conn = DBConnection.getConnection();
            }

            String query  =
                    "SELECT b.bill_id, b.customer_id, b.bill_date, " +
                            "b.units_consumed AS units, " +
                            "b.total_amount, " +
                            "b.payment_status AS status " +
                            "FROM bills b " +
                            "ORDER BY b.bill_date DESC";


            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("bill_id"),
                        rs.getInt("customer_id"),
                        rs.getDate("bill_date"),
                        rs.getDouble("units"),
                        "₹" + rs.getDouble("total_amount"),
                        rs.getString("status")
                });
            }

            txtBillDetails.setText("Bills reloaded successfully.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading bills: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ******** GENERATE BILL ********
    private void generateBill() {
        String custText = txtCustomerId.getText().trim();
        if (custText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Customer ID", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId;
        try {
            customerId = Integer.parseInt(custText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Customer ID format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
        	String query =
        	        "SELECT r.* FROM meter_readings r " +
        	        "LEFT JOIN bills b ON r.reading_id = b.reading_id " +
        	        "WHERE r.customer_id = ? AND b.reading_id IS NULL " +
        	        "ORDER BY r.reading_date DESC " +
        	        "LIMIT 1";


            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                txtBillDetails.setText("All readings for this customer are already billed.\nNo new readings available.");
                return;
            }

            int readingId = rs.getInt("reading_id");
            double unitsConsumed = rs.getDouble("units_consumed");
            LocalDate billDate = LocalDate.now();

            double ratePerUnit = 6.25;
            double fixedCharge = 50.00;
            double energyCharge = unitsConsumed * ratePerUnit;
            double taxAmount = 0.10 * (energyCharge + fixedCharge);
            double totalAmount = energyCharge + fixedCharge + taxAmount;

            String insert =
                    "INSERT INTO bills " +
                            "(customer_id, reading_id, bill_date, due_date, units_consumed, " +
                            "rate_per_unit, energy_charge, fixed_charge, tax_amount, total_amount, payment_status) " +
                            "VALUES (?, ?, ?, DATE_ADD(?, INTERVAL 15 DAY), ?, ?, ?, ?, ?, ?, 'Pending')";


            PreparedStatement ps2 = conn.prepareStatement(insert);
            ps2.setInt(1, customerId);
            ps2.setInt(2, readingId);
            ps2.setDate(3, Date.valueOf(billDate));
            ps2.setDate(4, Date.valueOf(billDate));
            ps2.setDouble(5, unitsConsumed);
            ps2.setDouble(6, ratePerUnit);
            ps2.setDouble(7, energyCharge);
            ps2.setDouble(8, fixedCharge);
            ps2.setDouble(9, taxAmount);
            ps2.setDouble(10, totalAmount);
            ps2.executeUpdate();

            txtBillDetails.setText(String.format(
                    "✅ Bill Generated Successfully\n" +
                    "--------------------------------------\n" +
                    "Customer ID: %d\n" +
                    "Reading ID : %d\n" +
                    "Units: %.2f\n" +
                    "Rate: ₹%.2f/unit\n" +
                    "Energy Charge: ₹%.2f\n" +
                    "Fixed Charge: ₹%.2f\n" +
                    "Tax: ₹%.2f\n" +
                    "--------------------------------------\n" +
                    "Total Amount: ₹%.2f\n" +
                    "Status: Pending",
                    customerId, readingId, unitsConsumed, ratePerUnit,
                    energyCharge, fixedCharge, taxAmount, totalAmount
            ));


            loadAllBills();

        } catch (SQLException ex) {
            txtBillDetails.setText("Error generating bill: " + ex.getMessage());
        }
    }

   
    private void showPendingBills() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM bills WHERE payment_status = 'Pending'"
            );

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                JOptionPane.showMessageDialog(
                        this,
                        "Total Pending Bills: " + count,
                        "Pending Bills",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error fetching pending bills: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getPanel() {
        return this;
    }
}
