import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowMenu();
            }
        });
    }

    private static void createAndShowMenu() {
        JFrame frame = new JFrame("Menu Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton pointsSystemButton = new JButton("Sistema de Pontos");
        JButton productManagerButton = new JButton("Gerenciador de Produtos");
        JButton budgetButton = new JButton("Orçamento");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(pointsSystemButton, gbc);

        gbc.gridy++;
        panel.add(productManagerButton, gbc);

        gbc.gridy++;
        panel.add(budgetButton, gbc);

        pointsSystemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha o Menu Principal
                UserRegistrationGUI.main(null); // Abre o Sistema de Pontos
            }
        });

        productManagerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha o Menu Principal
                ProductRegistrationGUI.main(null); // Abre o Gerenciador de Produtos
            }
        });

        budgetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha o Menu Principal
                BudgetGUI.main(null); // Abre o Orçamento
            }
        });

        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null); // Centraliza a janela
        frame.setVisible(true);
    }
}