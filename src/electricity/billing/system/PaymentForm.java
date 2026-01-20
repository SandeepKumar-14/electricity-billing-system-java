package electricity.billing.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class PaymentForm {

    private JPanel panel;
    private JTextField billIdField, amountField;
    private JTable paymentsTable;
    private DefaultTableModel tableModel;

    public PaymentForm() {
        initComponents();
        loadPaymentsData();
    }

    public JPanel getPanel() {
        return panel;
    }

    private void initComponents() {

        panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Payment Management");
        titleLabel.setFont(StyleConstants.HEADING_FONT);
        titleLabel.setForeground(StyleConstants.PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(420);
        splitPane.setResizeWeight(0.5);

        // ------------ LEFT PANEL ------------
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        StyleConstants.styleCard(formPanel);

        formPanel.add(new JLabel("Bill ID:"));
        billIdField = new JTextField();
        formPanel.add(billIdField);

        formPanel.add(new JLabel("Amount Paid:"));
        amountField = new JTextField();
        formPanel.add(amountField);

        JButton processPaymentButton = new JButton("Process Payment");
        JButton checkBillButton = new JButton("Check Bill Details");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh Payments");

        StyleConstants.styleButton(processPaymentButton);
        StyleConstants.styleButton(checkBillButton);
        StyleConstants.styleButton(clearButton);
        StyleConstants.styleButton(refreshButton);

        clearButton.setBackground(StyleConstants.ACCENT_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(processPaymentButton);
        buttonPanel.add(checkBillButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        formPanel.add(new JLabel());
        formPanel.add(buttonPanel);

        // ------------ BILL INFO PANEL ------------
        JPanel infoPanel = new JPanel(new BorderLayout());
        StyleConstants.styleCard(infoPanel);

        final JTextArea billInfoArea = new JTextArea(10, 30);
        billInfoArea.setEditable(false);
        billInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        billInfoArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        infoPanel.add(new JLabel("Bill Information:"), BorderLayout.NORTH);
        infoPanel.add(new JScrollPane(billInfoArea), BorderLayout.CENTER);

        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(infoPanel, BorderLayout.CENTER);

        // ------------ RIGHT PANEL (PAYMENTS TABLE) ------------
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel tableLabel = new JLabel("Payment History");
        tableLabel.setFont(StyleConstants.SUBHEADING_FONT);
        tableLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        String[] columns = {"Bill ID", "Customer ID", "Amount", "Payment Date", "Status", "Method"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        paymentsTable = new JTable(tableModel);
        paymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(paymentsTable);

        rightPanel.add(tableLabel, BorderLayout.NORTH);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        // ------------ BOTTOM PANEL: REVENUE BUTTON ------------
        JButton revenueBtn = new JButton("Show Revenue Generated");
        StyleConstants.styleButton(revenueBtn);
        revenueBtn.setBackground(new Color(41, 128, 185));
        revenueBtn.setForeground(Color.WHITE);

        revenueBtn.addActionListener(e -> showRevenueGenerated());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.add(revenueBtn);

        // ------------ ACTION LISTENERS ------------
        // Process Payment -> open realistic payment dialog
        processPaymentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openProcessPayment(billInfoArea);
            }
        });

        checkBillButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkBillDetails(billInfoArea);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearForm(billInfoArea);
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadPaymentsData();
            }
        });

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // ------------------ Open Process Payment flow ------------------
    private void openProcessPayment(final JTextArea billInfoArea) {
        String billText = billIdField.getText().trim();
        String amtText = amountField.getText().trim();

        if (billText.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Enter Bill ID to proceed.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int billId;
        try {
            billId = Integer.parseInt(billText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel, "Invalid Bill ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Fetch bill details and amount due from DB
        double totalAmount = 0.0;
        String status = "";
        int customerId = -1;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT total_amount, payment_status, customer_id FROM bills WHERE bill_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(panel, "Bill not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            totalAmount = rs.getDouble("total_amount");
            status = rs.getString("payment_status");
            customerId = rs.getInt("customer_id");

            if ("Paid".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(panel, "This bill is already paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error fetching bill: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // If user provided an amount, basic check (allow partial with confirm)
        if (!amtText.isEmpty()) {
            try {
                double entered = Double.parseDouble(amtText);
                if (entered < totalAmount) {
                    int confirm = JOptionPane.showConfirmDialog(panel,
                            String.format("Entered amount (₹%.2f) is less than total (₹%.2f). Continue as partial payment?", entered, totalAmount),
                            "Partial Payment", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Open the modal payment dialog (realistic)
        createPaymentDialog(billId, totalAmount, billInfoArea);
    }

    // ------------------ Realistic Payment Dialog ------------------
    private void createPaymentDialog(final int billId, final double totalAmount, final JTextArea billInfoArea) {
        final JDialog dialog = new JDialog((Frame) null, "Payment Gateway", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Complete Payment", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        main.add(header, BorderLayout.NORTH);

        // Left: method buttons
        JPanel left = new JPanel(new GridLayout(0, 1, 8, 8));
        left.setPreferredSize(new Dimension(160, 0));
        JButton upiBtn = new JButton("UPI");
        JButton cardBtn = new JButton("Card");
        JButton netBtn = new JButton("NetBanking");
        JButton walletBtn = new JButton("Wallet");
        JButton cashBtn = new JButton("Cash");
        left.add(upiBtn);
        left.add(cardBtn);
        left.add(netBtn);
        left.add(walletBtn);
        left.add(cashBtn);

        // Right: card layout for method-specific forms
        final JPanel right = new JPanel(new CardLayout());
        right.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // --- UPI Panel ---
        JPanel upiPanel = new JPanel(new BorderLayout(5,5));
        JPanel upiInput = new JPanel(new GridLayout(0,1,5,5));
        upiInput.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        upiInput.add(new JLabel("Enter UPI ID (example@bank)"));
        final JTextField upiField = new JTextField();
        upiInput.add(upiField);
        upiPanel.add(upiInput, BorderLayout.NORTH);

        // --- Card Panel ---
        JPanel cardPanel = new JPanel(new GridLayout(0,1,5,5));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        cardPanel.add(new JLabel("Card Number:"));
        final JTextField cardNumberField = new JTextField();
        cardPanel.add(cardNumberField);
        cardPanel.add(new JLabel("Expiry (MM/YY):"));
        final JTextField cardExpiryField = new JTextField();
        cardPanel.add(cardExpiryField);
        cardPanel.add(new JLabel("CVV:"));
        final JPasswordField cardCvvField = new JPasswordField();
        cardPanel.add(cardCvvField);

        // --- NetBanking Panel ---
        JPanel netPanel = new JPanel(new GridLayout(0,1,5,5));
        netPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        netPanel.add(new JLabel("Select Bank:"));
        final JComboBox<String> bankBox = new JComboBox<>(new String[] {"Select Bank", "SBI", "HDFC", "ICICI", "Axis", "Other"});
        netPanel.add(bankBox);

        // --- Wallet Panel ---
        JPanel walletPanel = new JPanel(new GridLayout(0,1,5,5));
        walletPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        walletPanel.add(new JLabel("Wallet Provider:"));
        final JComboBox<String> walletBox = new JComboBox<>(new String[] {"Select Wallet", "Paytm", "PhonePe", "GooglePay"});
        walletPanel.add(walletBox);
        walletPanel.add(new JLabel("Mobile Number:"));
        final JTextField walletMobileField = new JTextField();
        walletPanel.add(walletMobileField);

        // --- Cash Panel ---
        JPanel cashPanel = new JPanel(new BorderLayout());
        cashPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        cashPanel.add(new JLabel("Cash Payment will be recorded. Collect cash physically."), BorderLayout.NORTH);

        // Add to right card layout
        right.add(upiPanel, "UPI");
        right.add(cardPanel, "Card");
        right.add(netPanel, "NetBanking");
        right.add(walletPanel, "Wallet");
        right.add(cashPanel, "Cash");

        // Bottom: amount and pay button
        JPanel bottom = new JPanel(new BorderLayout(10,10));
        JPanel amtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        amtPanel.add(new JLabel("Amount to Pay: "));
        amtPanel.add(new JLabel(String.format("₹%.2f", totalAmount)));
        bottom.add(amtPanel, BorderLayout.WEST);

        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton payNowBtn = new JButton("Pay Now");
        final JButton cancelBtn = new JButton("Cancel");
        payPanel.add(cancelBtn);
        payPanel.add(payNowBtn);
        bottom.add(payPanel, BorderLayout.EAST);

        main.add(left, BorderLayout.WEST);
        main.add(right, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        dialog.getContentPane().add(main);

        // Switch card on method click
        ActionListener methodSwitch = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cmd = ((JButton) e.getSource()).getText();
                CardLayout cl = (CardLayout) (right.getLayout());
                cl.show(right, cmd);
            }
        };
        upiBtn.addActionListener(methodSwitch);
        cardBtn.addActionListener(methodSwitch);
        netBtn.addActionListener(methodSwitch);
        walletBtn.addActionListener(methodSwitch);
        cashBtn.addActionListener(methodSwitch);

        // Default to UPI
        CardLayout clDefault = (CardLayout) (right.getLayout());
        clDefault.show(right, "UPI");

        // Cancel action
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

    
        payNowBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
           
                CardLayout layout = (CardLayout) right.getLayout();
                if (isPanelVisible(right, upiPanel)) {
                    String upiId = upiField.getText().trim();
                    if (upiId.isEmpty() || !upiId.contains("@")) {
                        JOptionPane.showMessageDialog(dialog, "Enter a valid UPI ID.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    finalizePayment(billId, "UPI", dialog, billInfoArea);
                    return;
                }

           
                if (isPanelVisible(right, cardPanel)) {
                    String cardNum = cardNumberField.getText().trim();
                    String expiry = cardExpiryField.getText().trim();
                    String cvv = new String(cardCvvField.getPassword()).trim();
                    if (cardNum.length() < 12) {
                        JOptionPane.showMessageDialog(dialog, "Enter valid card number.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (expiry.isEmpty() || !expiry.contains("/")) {
                        JOptionPane.showMessageDialog(dialog, "Enter valid expiry in MM/YY.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (cvv.length() < 3) {
                        JOptionPane.showMessageDialog(dialog, "Enter valid CVV.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    finalizePayment(billId, "Card", dialog, billInfoArea);
                    return;
                }

              
                if (isPanelVisible(right, netPanel)) {
                    String bank = (String) bankBox.getSelectedItem();
                    if (bank == null || "Select Bank".equals(bank)) {
                        JOptionPane.showMessageDialog(dialog, "Select a bank.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    finalizePayment(billId, "NetBanking", dialog, billInfoArea);
                    return;
                }

                // Wallet validation
                if (isPanelVisible(right, walletPanel)) {
                    String wallet = (String) walletBox.getSelectedItem();
                    String mobile = walletMobileField.getText().trim();
                    if (wallet == null || "Select Wallet".equals(wallet)) {
                        JOptionPane.showMessageDialog(dialog, "Select a wallet provider.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (mobile.length() < 10) {
                        JOptionPane.showMessageDialog(dialog, "Enter valid mobile number.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    finalizePayment(billId, "Wallet", dialog, billInfoArea);
                    return;
                }

                // Cash
                if (isPanelVisible(right, cashPanel)) {
                    int confirm = JOptionPane.showConfirmDialog(dialog, "Confirm cash payment received?", "Cash Confirmation", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    finalizePayment(billId, "Cash", dialog, billInfoArea);
                    return;
                }

            }
        });

        dialog.setVisible(true);
    }

    // Utility to check visibility of a card panel in a CardLayout container
    private boolean isPanelVisible(Container parent, Component child) {
        for (Component comp : parent.getComponents()) {
            if (comp == child) {
                return comp.isVisible();
            }
        }
        return false;
    }

    // Finalize payment: update DB and refresh UI
    private void finalizePayment(int billId, String method, JDialog dialog, JTextArea billInfoArea) {
        try (Connection conn = DBConnection.getConnection()) {
            String updateSql = "UPDATE bills SET payment_status = 'Paid', payment_date = ?, payment_method = ? WHERE bill_id = ?";
            PreparedStatement ps = conn.prepareStatement(updateSql);
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(2, method);
            ps.setInt(3, billId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(panel, "Payment successful via " + method + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadPaymentsData();
                clearForm(billInfoArea);
            } else {
                JOptionPane.showMessageDialog(panel, "Payment failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -----------------------------------------------------
    // CHECK BILL DETAILS
    // -----------------------------------------------------
    private void checkBillDetails(JTextArea billInfoArea) {
        String billIdText = billIdField.getText().trim();

        if (billIdText.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Enter Bill ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int billId = Integer.parseInt(billIdText);

            try (Connection conn = DBConnection.getConnection()) {
                String sql =
                        "SELECT b.*, c.name, c.address " +
                                "FROM bills b " +
                                "JOIN customers c ON b.customer_id = c.customer_id " +
                                "WHERE b.bill_id = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, billId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    billInfoArea.setText("Bill not found.");
                    return;
                }

                billInfoArea.setText(String.format(
                        "=== BILL DETAILS ===\n\n" +
                                "Bill ID: %d\n" +
                                "Customer: %s\n" +
                                "Address: %s\n" +
                                "Bill Date: %s\n" +
                                "Due Date: %s\n" +
                                "Units: %.2f\n" +
                                "Rate: ₹%.2f\n" +
                                "Energy Charge: ₹%.2f\n" +
                                "Fixed Charge: %.2f\n" +
                                "Tax: %.2f\n" +
                                "------------------------\n" +
                                "TOTAL DUE: ₹%.2f\n" +
                                "Status: %s",
                        rs.getInt("bill_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getDate("bill_date"),
                        rs.getDate("due_date"),
                        rs.getDouble("units_consumed"),
                        rs.getDouble("rate_per_unit"),
                        rs.getDouble("energy_charge"),
                        rs.getDouble("fixed_charge"),
                        rs.getDouble("tax_amount"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status")
                ));

                amountField.setText(String.format("%.2f", rs.getDouble("total_amount")));
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panel, "Invalid Bill ID!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error loading bill details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void clearForm(JTextArea billInfoArea) {
        billIdField.setText("");
        amountField.setText("");
        billInfoArea.setText("");
    }


    private void loadPaymentsData() {
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT bill_id, customer_id, total_amount, payment_date, payment_status, payment_method " +
                            "FROM bills " +
                            "ORDER BY payment_date DESC";

            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("bill_id"),
                        rs.getInt("customer_id"),
                        String.format("₹%.2f", rs.getDouble("total_amount")),
                        rs.getDate("payment_date"),
                        rs.getString("payment_status"),
                        rs.getString("payment_method")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error loading payments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showRevenueGenerated() {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement psToday = conn.prepareStatement(
                    "SELECT SUM(total_amount) FROM bills WHERE payment_status='Paid' AND payment_date = CURDATE()"
            );

            PreparedStatement psTotal = conn.prepareStatement(
                    "SELECT SUM(total_amount) FROM bills WHERE payment_status='Paid'"
            );

            ResultSet rsToday = psToday.executeQuery();
            ResultSet rsTotal = psTotal.executeQuery();

            double today = rsToday.next() ? rsToday.getDouble(1) : 0.0;
            double total = rsTotal.next() ? rsTotal.getDouble(1) : 0.0;

            JOptionPane.showMessageDialog(
                    panel,
                    String.format(
                            "🔹 Revenue Summary\n\n" +
                                    "Today’s Revenue: ₹%.2f\n" +
                                    "Total Revenue:   ₹%.2f",
                            today, total
                    ),
                    "Revenue Generated",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel,
                    "Error calculating revenue: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
