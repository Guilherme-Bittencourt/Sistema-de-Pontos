import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.mysql.cj.jdbc.MysqlDataSource;

public class BudgetGUI {
    public static void main(String[] args) {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl("jdbc:mysql://10.0.0.109:3306/Fidelity"); // Substitua pelo nome do seu banco de dados
        ds.setUser("dba"); // Substitua pelo seu usuário MySQL
        ds.setPassword("admin123"); // Substitua pela sua senha MySQL

        SwingUtilities.invokeLater(() -> createAndShowGUI(ds));
    }

    private static void createAndShowGUI(MysqlDataSource ds) {
        JFrame frame = new JFrame("Lista de Resgate");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 750);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Lista de Resgate", createBudgetPanel(frame, ds));

        frame.getContentPane().add(tabbedPane);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createBudgetPanel(JFrame frame, MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel nameLabel = new JLabel("Digite o CPF ou Telefone do Cliente:");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Buscar Cliente");
        JLabel clientInfoLabel = new JLabel("Cliente: ");
        JLabel pointsLabel = new JLabel("Pontos do Cliente: 0");
        JButton addButton = new JButton("Adicionar Produto à Lista");
        JButton redeemButton = new JButton("Resgatar Produto");
        JButton removeButton = new JButton("Remover Produto Selecionado");
        JLabel totalPointsLabel = new JLabel("Total de Pontos: 0");
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Produto", "Quantidade", "Preço Unitário", "Subtotal", "Pontos"}, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        JButton backButton = new JButton("Menu Principal");

        // Layout Configuration
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        panel.add(searchField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(searchButton, gbc);

        gbc.gridx++;
        panel.add(backButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(clientInfoLabel, gbc);

        gbc.gridy++;
        panel.add(pointsLabel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        panel.add(totalPointsLabel, gbc);
        
        gbc.gridy++;
        panel.add(addButton, gbc);

        gbc.gridy++;
        panel.add(removeButton, gbc);

        gbc.gridy++;
        panel.add(redeemButton, gbc);

        
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha a janela do gerenciador de produtos
                MenuGUI.main(new String[0]); // Abre a janela do menu principal
            }
        });
        
        searchButton.addActionListener(e -> {
            String searchQuery = searchField.getText(); // Obtém o valor digitado no campo de busca

            if (searchQuery.isEmpty()) {
                // Se o campo estiver vazio, exibe um erro
                JOptionPane.showMessageDialog(panel, "Digite o CPF ou Telefone para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tentativa de conexão e busca no banco de dados
            try (Connection conn = ds.getConnection()) {
                // Query para buscar o cliente com base no CPF ou telefone
                String selectQuery = "SELECT username, pontos FROM Clientes WHERE CPF = ? OR telefone = ?";
                PreparedStatement statement = conn.prepareStatement(selectQuery);
                statement.setString(1, searchQuery);
                statement.setString(2, searchQuery);
                ResultSet resultSet = statement.executeQuery();

                // Verifica se algum resultado foi encontrado
                if (resultSet.next()) {
                    String name = resultSet.getString("username");
                    int points = resultSet.getInt("pontos");

                    // Atualiza as informações na interface
                    clientInfoLabel.setText("Cliente: " + name);
                    pointsLabel.setText("Pontos do Cliente: " + points); // Atualiza os pontos do cliente
                } else {
                    // Se o cliente não for encontrado, exibe mensagem de erro
                    clientInfoLabel.setText("Cliente não encontrado.");
                    pointsLabel.setText("Pontos do Cliente: 0");
                }
            } catch (SQLException ex) {
                // Tratamento de erro ao acessar o banco de dados
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Erro ao buscar cliente.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        redeemButton.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                // Caso não haja produtos na lista de resgate, exibe uma mensagem de erro
                JOptionPane.showMessageDialog(panel, "Nenhum produto adicionado à lista de resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String searchQuery = searchField.getText(); // Obtém o valor digitado no campo de busca
            if (searchQuery.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Digite o CPF ou Telefone do cliente para realizar o resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = ds.getConnection()) {
                // Busca os pontos do cliente no banco de dados
                String selectQuery = "SELECT pontos FROM Clientes WHERE CPF = ? OR telefone = ?";
                PreparedStatement stmt = conn.prepareStatement(selectQuery);
                stmt.setString(1, searchQuery);
                stmt.setString(2, searchQuery);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int pontosCliente = rs.getInt("pontos"); // Pontos atuais do cliente
                    int totalPontosResgate = calculateTotalPoints(model); // Total de pontos necessários para o resgate

                    // Exibe uma janela de confirmação para o usuário
                    int confirm = JOptionPane.showConfirmDialog(panel,
                            "Total de pontos necessários: " + totalPontosResgate + "\nDeseja confirmar o resgate?",
                            "Confirmação de Resgate",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Verifica se o cliente tem pontos suficientes
                        if (pontosCliente >= totalPontosResgate) {
                            // Loop por cada item na tabela de resgate para atualizar o estoque e os pontos do cliente
                            for (int i = 0; i < model.getRowCount(); i++) {
                                String productName = (String) model.getValueAt(i, 0); // Nome do produto
                                int quantity = (int) model.getValueAt(i, 1); // Quantidade do produto

                                // Consulta para buscar os pontos de resgate e a quantidade disponível do produto
                                String selectProductQuery = "SELECT pontos_resgate, quantidade FROM Produtos WHERE name = ?";
                                PreparedStatement productStmt = conn.prepareStatement(selectProductQuery);
                                productStmt.setString(1, productName);
                                ResultSet productResult = productStmt.executeQuery();

                                if (productResult.next()) {
                                    int availableQuantity = productResult.getInt("quantidade");

                                    // Verifica se há quantidade suficiente no estoque
                                    if (quantity <= availableQuantity) {
                                        // Atualiza os pontos do cliente
                                        String updateClientQuery = "UPDATE Clientes SET pontos = pontos - ? WHERE CPF = ? OR telefone = ?";
                                        PreparedStatement updateClientStmt = conn.prepareStatement(updateClientQuery);
                                        updateClientStmt.setInt(1, totalPontosResgate); // Deduz os pontos do total de resgate
                                        updateClientStmt.setString(2, searchQuery);
                                        updateClientStmt.setString(3, searchQuery);
                                        updateClientStmt.executeUpdate();

                                        // Atualiza a quantidade de produtos no estoque
                                        String updateProductQuery = "UPDATE Produtos SET quantidade = quantidade - ? WHERE name = ?";
                                        PreparedStatement updateProductStmt = conn.prepareStatement(updateProductQuery);
                                        updateProductStmt.setInt(1, quantity); // Deduz a quantidade resgatada do estoque
                                        updateProductStmt.setString(2, productName);
                                        updateProductStmt.executeUpdate();
                                    } else {
                                        // Caso não tenha quantidade suficiente em estoque
                                        JOptionPane.showMessageDialog(panel, "Estoque insuficiente para o produto: " + productName, "Erro", JOptionPane.ERROR_MESSAGE);
                                        return; // Encerra o processo de resgate
                                    }
                                }
                            }

                            // Atualiza os pontos do cliente na interface após o resgate
                            pontosCliente -= totalPontosResgate;
                            pointsLabel.setText("Pontos do Cliente: " + pontosCliente);

                            // Exibe mensagem de sucesso
                            JOptionPane.showMessageDialog(panel, "Resgate realizado com sucesso!");

                            // Limpa a lista de resgate após o sucesso
                            model.setRowCount(0);
                            updateTotalPoints(model, totalPointsLabel);
                        } else {
                            // Caso o cliente não tenha pontos suficientes
                            JOptionPane.showMessageDialog(panel, "Pontos insuficientes para o resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    // Caso o cliente não seja encontrado no banco de dados
                    JOptionPane.showMessageDialog(panel, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Erro ao realizar resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Event Listener for Add Button
        addButton.addActionListener(e -> {
            JPanel addProductPanel = new JPanel(new GridBagLayout());
            JLabel productLabel = new JLabel("Produto:");
            JComboBox<String> productComboBox = new JComboBox<>();
            JLabel availableLabel = new JLabel("Disponível: 0");

            // Preenche o JComboBox com os nomes dos produtos
            try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT name FROM Produtos");
                while (rs.next()) {
                    productComboBox.addItem(rs.getString("name"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Atualiza a quantidade disponível quando o produto é selecionado
            productComboBox.addActionListener(e1 -> {
                String productName = (String) productComboBox.getSelectedItem();
                try (Connection conn = ds.getConnection()) {
                    String checkQuery = "SELECT quantidade FROM Produtos WHERE name = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setString(1, productName);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int availableQuantity = rs.getInt("quantidade");
                        availableLabel.setText("Disponível: " + availableQuantity);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            GridBagConstraints gbcAdd = new GridBagConstraints();
            gbcAdd.gridx = 0;
            gbcAdd.gridy = 0;
            gbcAdd.insets = new Insets(5, 5, 5, 5);
            addProductPanel.add(productLabel, gbcAdd);

            gbcAdd.gridx = 1;
            addProductPanel.add(productComboBox, gbcAdd);

            gbcAdd.gridx = 0;
            gbcAdd.gridy++;
            addProductPanel.add(availableLabel, gbcAdd);

            int option = JOptionPane.showConfirmDialog(panel, addProductPanel, "Adicionar Produto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                String productName = (String) productComboBox.getSelectedItem();

                // Verifica se o produto já foi adicionado
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (model.getValueAt(i, 0).equals(productName)) {
                        JOptionPane.showMessageDialog(panel, "Este produto já foi adicionado.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Adiciona o produto à lista
                try (Connection conn = ds.getConnection()) {
                    String checkQuery = "SELECT quantidade, preco, pontos_resgate FROM Produtos WHERE name = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setString(1, productName);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        int availableQuantity = rs.getInt("quantidade");
                        float price = rs.getFloat("preco");
                        int pontosResgate = rs.getInt("pontos_resgate");

                        if (availableQuantity == 0) {
                            JOptionPane.showMessageDialog(panel, "Produto indisponível em estoque.", "Erro", JOptionPane.ERROR_MESSAGE);
                        } else {
                            float subtotal = price * 1;
                            model.addRow(new Object[]{productName, 1, price, subtotal, pontosResgate});
                            updateTotalPoints(model, totalPointsLabel);
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel, "Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao verificar estoque do produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Event Listener for Remove Button
        removeButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
                updateTotalPoints(model, totalPointsLabel);
            } else {
                JOptionPane.showMessageDialog(panel, "Selecione um produto para remover.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // Calcula o total de pontos necessários para o resgate
    private static int calculateTotalPoints(DefaultTableModel model) {
        int totalPoints = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int pontos = (int) model.getValueAt(i, 4); // Coluna 4 é onde os pontos estão
            totalPoints += pontos;
        }
        return totalPoints;
    }

    // Atualiza o total de pontos exibidos
    private static void updateTotalPoints(DefaultTableModel model, JLabel totalPointsLabel) {
        int totalPoints = calculateTotalPoints(model);
        totalPointsLabel.setText("Total de Pontos: " + totalPoints);
    }
}
