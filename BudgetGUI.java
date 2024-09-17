import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.mysql.cj.jdbc.MysqlDataSource;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.mindrot.jbcrypt.BCrypt;
import javax.crypto.Cipher;

import java.util.Base64;

public class BudgetGUI {
    public static void main(String[] args) {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl("jdbc:mysql://10.0.0.109:3306/Fidelity"); // Substitua pelo nome do seu banco de dados
        ds.setUser("dba"); // Substitua pelo seu usuário MySQL
        ds.setPassword("admin123"); // Substitua pela sua senha MySQL

        SwingUtilities.invokeLater(() -> createAndShowGUI(ds));
    }
    
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128; // Tamanho da chave em bits

    // Método para gerar uma chave secreta
    private static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }

    // Método para criptografar um texto usando uma chave
    private static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Método para descriptografar um texto usando uma chave
    private static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData);
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

        JLabel nameLabel = new JLabel("Digite o Telefone do Cliente:");
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
                JOptionPane.showMessageDialog(panel, "Digite o Telefone para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = ds.getConnection()) {
                // Query para buscar o cliente com base apenas no telefone
                String selectQuery = "SELECT username, pontos, telefone, endereço, CPF, `key` FROM Clientes WHERE telefone = ?";
                PreparedStatement statement = conn.prepareStatement(selectQuery);
                statement.setString(1, searchQuery); // Tenta buscar por telefone
                ResultSet resultSet = statement.executeQuery();

                boolean foundUser = false;

                while (resultSet.next()) {
                    String storedCpf = resultSet.getString("CPF");
                    String storedKey = resultSet.getString("key");

                    // Se o input for um telefone, não precisamos criptografar ou comparar CPF
                    // Removido a criptografia do CPF e a verificação do CPF, pois não será necessário
                    if (storedKey != null && storedCpf != null && searchQuery.length() == 11) { // Verifica se o input é um CPF
                        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(storedKey), "AES");

                        // Criptografar o CPF digitado para comparar
                        String encryptedCpf = encrypt(searchQuery, key);

                        // Se o CPF criptografado digitado for diferente do armazenado, continua
                        if (!storedCpf.equals(encryptedCpf)) {
                            continue; // Não há correspondência com o CPF
                        }
                    }

                    // Se o telefone corresponder, atualizamos a interface
                    foundUser = true;
                    String name = resultSet.getString("username");
                    int points = resultSet.getInt("pontos");

                    // Atualiza as informações na interface
                    clientInfoLabel.setText("Cliente: " + name);
                    pointsLabel.setText("Pontos do Cliente: " + points); // Atualiza os pontos do cliente
                }

                if (!foundUser) {
                    clientInfoLabel.setText("Cliente não encontrado.");
                    pointsLabel.setText("Pontos do Cliente: 0");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Erro ao buscar cliente.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Erro na criptografia do CPF.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });



        
        redeemButton.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel, "Nenhum produto adicionado à lista de resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String searchQuery = searchField.getText();
            if (searchQuery.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Digite o Telefone do cliente para realizar o resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = ds.getConnection()) {
                String selectQuery = "SELECT pontos FROM Clientes WHERE CPF = ? OR telefone = ?";
                PreparedStatement stmt = conn.prepareStatement(selectQuery);
                stmt.setString(1, searchQuery);
                stmt.setString(2, searchQuery);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int pontosCliente = rs.getInt("pontos");
                    int totalPontosResgate = calculateTotalPoints(model);

                    int confirm = JOptionPane.showConfirmDialog(panel,
                            "Total de pontos necessários: " + totalPontosResgate + "\nDeseja confirmar o resgate?",
                            "Confirmação de Resgate", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (pontosCliente >= totalPontosResgate) {
                            for (int i = 0; i < model.getRowCount(); i++) {
                                String productName = (String) model.getValueAt(i, 0);
                                int quantity = (int) model.getValueAt(i, 1);

                                String selectProductQuery = "SELECT pontos_resgate, quantidade FROM Produtos WHERE name = ?";
                                PreparedStatement productStmt = conn.prepareStatement(selectProductQuery);
                                productStmt.setString(1, productName);
                                ResultSet productResult = productStmt.executeQuery();

                                if (productResult.next()) {
                                    int availableQuantity = productResult.getInt("quantidade");

                                    if (quantity <= availableQuantity) {
                                        String updateClientQuery = "UPDATE Clientes SET pontos = pontos - ? WHERE CPF = ? OR telefone = ?";
                                        PreparedStatement updateClientStmt = conn.prepareStatement(updateClientQuery);
                                        updateClientStmt.setInt(1, totalPontosResgate);
                                        updateClientStmt.setString(2, searchQuery);
                                        updateClientStmt.setString(3, searchQuery);
                                        updateClientStmt.executeUpdate();

                                        String updateProductQuery = "UPDATE Produtos SET quantidade = quantidade - ? WHERE name = ?";
                                        PreparedStatement updateProductStmt = conn.prepareStatement(updateProductQuery);
                                        updateProductStmt.setInt(1, quantity);
                                        updateProductStmt.setString(2, productName);
                                        updateProductStmt.executeUpdate();
                                        
                                        // Adiciona ao histórico de ações
                                        String historicoQuery = "INSERT INTO Historico (tipo_acao, descricao) VALUES (?, ?)";
                                        PreparedStatement historicoStmt = conn.prepareStatement(historicoQuery);
                                        historicoStmt.setString(1, "Resgate de Produto");
                                        historicoStmt.setString(2, "Produto " + productName + " resgatado pelo cliente " + searchQuery);
                                        historicoStmt.executeUpdate();

                                    } else {
                                        JOptionPane.showMessageDialog(panel, "Estoque insuficiente para o produto: " + productName, "Erro", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                }
                            }

                            pontosCliente -= totalPontosResgate;
                            pointsLabel.setText("Pontos do Cliente: " + pontosCliente);
                            JOptionPane.showMessageDialog(panel, "Resgate realizado com sucesso!");
                            model.setRowCount(0);
                            updateTotalPoints(model, totalPointsLabel);
                        } else {
                            JOptionPane.showMessageDialog(panel, "Pontos insuficientes para o resgate.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
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
