package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginFrame() {
        setTitle("Electricity Billing System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(StyleConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        JLabel headerLabel = new JLabel("ELECTRICITY BILLING SYSTEM", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(StyleConstants.PRIMARY_COLOR);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel subHeaderLabel = new JLabel("Smart Management System", SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subHeaderLabel.setForeground(new Color(100, 100, 100));
        subHeaderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleConstants.BACKGROUND_COLOR);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(subHeaderLabel, BorderLayout.SOUTH);
        
        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel loginTitle = new JLabel("Administrator Login", SwingConstants.CENTER);
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginTitle.setForeground(StyleConstants.PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginCard.add(loginTitle, gbc);
        
        gbc.gridy = 1;
        loginCard.add(Box.createVerticalStrut(20), gbc);
        
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        loginCard.add(userLabel, gbc);
        
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridx = 1; gbc.gridy = 2;
        loginCard.add(usernameField, gbc);
        
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 3;
        loginCard.add(passLabel, gbc);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridx = 1; gbc.gridy = 3;
        loginCard.add(passwordField, gbc);
        
        JButton loginButton = new JButton("LOGIN TO DASHBOARD");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(StyleConstants.PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
       
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(StyleConstants.SECONDARY_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(StyleConstants.PRIMARY_COLOR);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        loginCard.add(loginButton, gbc);
        
        JLabel footerLabel = new JLabel("Default credentials: admin / admin123", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footerLabel.setForeground(Color.GRAY);
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 10, 0, 10);
        loginCard.add(footerLabel, gbc);
        
        loginButton.addActionListener(e -> performLogin());
        
        passwordField.addActionListener(e -> performLogin());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(loginCard, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password!\n\nDefault credentials:\nUsername: admin\nPassword: admin123", 
                "Login Failed", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        
        if ("admin".equals(username) && "admin123".equals(password)) {
            SwingUtilities.invokeLater(() -> {
            	new MainFrame();
            	dispose();  // close login window
            });
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password!\n\nPlease use:\nUsername: admin\nPassword: admin123", 
                "Authentication Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}