package electricity.billing.system;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;

    public MainFrame() {
        setTitle("Electricity Billing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initUI();
        setVisible(true);
    }

    private void initUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleConstants.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Electricity Billing System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(StyleConstants.PRIMARY_COLOR);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

      
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(0,1));
        sidebar.setBackground(new Color(45, 52, 54));
        sidebar.setPreferredSize(new Dimension(200, 0));

        JButton btnCustomers = createSidebarButton("Customer Management");
        JButton btnMeter = createSidebarButton("Meter Reading");
        JButton btnBilling = createSidebarButton("Billing");
        JButton btnPayments = createSidebarButton("Payments");

        sidebar.add(btnCustomers);
        sidebar.add(btnMeter);
        sidebar.add(btnBilling);
        sidebar.add(btnPayments);

     
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(StyleConstants.BACKGROUND_COLOR);

       
        loadPanel(new CustomerForm().getPanel());

        
        btnCustomers.addActionListener(e -> loadPanel(new CustomerForm().getPanel()));
        btnMeter.addActionListener(e -> loadPanel(new MeterReadingForm().getPanel()));
        btnBilling.addActionListener(e -> loadPanel(new BillingForm().getPanel()));
        btnPayments.addActionListener(e -> loadPanel(new PaymentForm().getPanel()));

       
        add(headerPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(new Color(99, 110, 114));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 10));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(178, 190, 195));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(99, 110, 114));
            }
        });

        return button;
    }

    public void loadPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
