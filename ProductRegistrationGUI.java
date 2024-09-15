import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.mysql.cj.jdbc.MysqlDataSource;

public class ProductRegistrationGUI {
    public static void main(String[] args) {
        // Configuração da fonte de dados MySQL
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl("jdbc:mysql://10.0.0.109:3306/Fidelity"); // Substitua 'seu_banco_de_dados' pelo nome do seu banco de dados
        ds.setUser("dba"); // Substitua 'seu_usuario' pelo seu usuário MySQL
        ds.setPassword("admin123"); // Substitua 'sua_senha' pela sua senha MySQL

        try (Connection connection = ds.getConnection(); Statement statement = connection.createStatement()) {
            // Criação da tabela 'Produtos' com pontos_resgate
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Produtos ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "quantidade INT NOT NULL, "
                    + "preco DECIMAL(10, 2) NOT NULL, "
                    + "pontos_resgate INT NOT NULL)";
            statement.execute(createTableSQL);
            System.out.println("Tabela 'Produtos' criada com sucesso.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Iniciar a GUI de registro de produto
        SwingUtilities.invokeLater(() -> createAndShowGUI(ds));
    }

    private static void createAndShowGUI(MysqlDataSource ds) {
        JFrame frame = new JFrame("Lista de Produtos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 550); // Ajuste o tamanho aqui
        frame.setLocationRelativeTo(null); // Centraliza o JFrame na tela

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Adicionar Produto", createCreateProductPanel(frame, ds));
        tabbedPane.addTab("Listar Produtos", createListProductPanel(ds));

        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createCreateProductPanel(JFrame frame, MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel nameLabel = new JLabel("Nome:");
        JTextField nameField = new JTextField(20);
        JLabel quantidadeLabel = new JLabel("Quantidade:");
        JTextField quantidadeField = new JTextField(20);
        JLabel precoLabel = new JLabel("Preço:");
        JTextField precoField = new JTextField(20);
        JLabel pontosLabel = new JLabel("Pontos para Resgate:");
        JTextField pontosField = new JTextField(20);
        JButton createButton = new JButton("Adicionar Produto");
        JButton backButton = new JButton("Menu Principal");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(nameLabel, gbc);

        gbc.gridy++;
        panel.add(quantidadeLabel, gbc);

        gbc.gridy++;
        panel.add(precoLabel, gbc);

        gbc.gridy++;
        panel.add(pontosLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridy++;
        panel.add(quantidadeField, gbc);

        gbc.gridy++;
        panel.add(precoField, gbc);

        gbc.gridy++;
        panel.add(pontosField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createButton, gbc);

        gbc.gridy++;
        panel.add(backButton, gbc);

        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String quantidadeStr = quantidadeField.getText();
                String precoStr = precoField.getText();
                String pontosStr = pontosField.getText();

                if (name.isEmpty() || quantidadeStr.isEmpty() || precoStr.isEmpty() || pontosStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantidade;
                float preco;
                int pontosResgate;

                try {
                    quantidade = Integer.parseInt(quantidadeStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Quantidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    preco = Float.parseFloat(precoStr.replace(",", "."));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Preço deve ser um número decimal.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    pontosResgate = Integer.parseInt(pontosStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Pontos para resgate devem ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (quantidade < 0) {
                    JOptionPane.showMessageDialog(panel, "Quantidade não pode ser negativa.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String insertQuery = "INSERT INTO Produtos (name, quantidade, preco, pontos_resgate) VALUES (?, ?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(insertQuery);
                    statement.setString(1, name);
                    statement.setInt(2, quantidade);
                    statement.setFloat(3, preco);
                    statement.setInt(4, pontosResgate);

                    int rowsAffected = statement.executeUpdate();
                    JOptionPane.showMessageDialog(panel, rowsAffected + " registro(s) inserido(s) com sucesso.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao adicionar produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha a janela do gerenciador de produtos
                MenuGUI.main(new String[0]); // Abre a janela do menu principal
            }
        });

        return panel;
    }

    private static JPanel createListProductPanel(MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel searchLabel = new JLabel("Buscar (Nome):");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Buscar");
        JButton listButton = new JButton("Listar Todos");
        JButton updateButton = new JButton("Atualizar");
        JButton deleteButton = new JButton("Excluir");
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Nome", "Quantidade", "Preço", "Pontos"}, 0);
        table.setModel(model);
        JScrollPane scrollPane = new JScrollPane(table);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(searchLabel, gbc);

        gbc.gridx++;
        panel.add(searchField, gbc);

        gbc.gridx++;
        panel.add(searchButton, gbc);

        gbc.gridx++;
        panel.add(listButton, gbc);

        gbc.gridx++;
        panel.add(updateButton, gbc);

        gbc.gridx++;
        panel.add(deleteButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchQuery = searchField.getText();

                if (searchQuery.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Digite o nome para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String selectQuery = "SELECT * FROM Produtos WHERE name LIKE ?";
                    PreparedStatement statement = conn.prepareStatement(selectQuery);
                    statement.setString(1, "%" + searchQuery + "%");
                    ResultSet resultSet = statement.executeQuery();
                    model.setRowCount(0); // Limpar a tabela

                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        int quantidade = resultSet.getInt("quantidade");
                        float preco = resultSet.getFloat("preco");
                        int pontos = resultSet.getInt("pontos_resgate");
                        model.addRow(new Object[]{id, name, quantidade, preco, pontos});
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(panel, "Nenhum produto encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao carregar produtos.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        listButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPasswordDialogAndListProducts(ds, model);
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    String id = model.getValueAt(selectedRow, 0).toString();
                    String name = model.getValueAt(selectedRow, 1).toString();
                    String quantidade = model.getValueAt(selectedRow, 2).toString();
                    String preco = model.getValueAt(selectedRow, 3).toString();
                    String pontos = model.getValueAt(selectedRow, 4).toString();

                    JFrame updateFrame = new JFrame("Atualizar Produto");
                    updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    JPanel updatePanel = createUpdateProductPanel(ds, id, name, quantidade, preco, pontos);
                    updateFrame.getContentPane().add(updatePanel);
                    updateFrame.pack();
                    updateFrame.setLocationRelativeTo(null);
                    updateFrame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(panel, "Selecione um produto para atualizar.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Solicita confirmação antes de excluir o produto
                int confirm = JOptionPane.showConfirmDialog(panel, "Você realmente deseja excluir este produto?", "Confirmação de Exclusão", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Obtém o ID do produto selecionado na tabela
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int productId = (int) model.getValueAt(selectedRow, 0);

                        try (Connection conn = ds.getConnection()) {
                            String deleteQuery = "DELETE FROM Produtos WHERE id = ?";
                            PreparedStatement statement = conn.prepareStatement(deleteQuery);
                            statement.setInt(1, productId);
                            int rowsAffected = statement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(panel, "Produto excluído com sucesso.");
                                model.removeRow(selectedRow); // Remove a linha da tabela após a exclusão
                            } else {
                                JOptionPane.showMessageDialog(panel, "Erro ao excluir produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(panel, "Erro ao excluir produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nenhum produto selecionado para exclusão.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });


        return panel;
    }

    private static void showPasswordDialogAndListProducts(MysqlDataSource ds, DefaultTableModel model) {
        JPasswordField passwordField = new JPasswordField(20);
        JPanel passwordPanel = new JPanel();
        passwordPanel.add(new JLabel("Digite a senha para listar todos os produtos:"));
        passwordPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(null, passwordPanel, "Autenticação Necessária", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());

            if ("admin123".equals(password)) {
                listProducts(ds, model);
            } else {
                JOptionPane.showMessageDialog(null, "Senha incorreta.", "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void listProducts(MysqlDataSource ds, DefaultTableModel model) {
        // Limpa a tabela
        model.setRowCount(0);

        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            String query = "SELECT * FROM Produtos";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantidade = rs.getInt("quantidade");
                float preco = rs.getFloat("preco");
                int pontos = rs.getInt("pontos_resgate");
                model.addRow(new Object[]{id, name, quantidade, preco, pontos});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao carregar produtos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel createUpdateProductPanel(MysqlDataSource ds, String id, String name, String quantidade, String preco, String pontos) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel nameLabel = new JLabel("Nome:");
        JTextField nameField = new JTextField(20);
        JLabel quantidadeLabel = new JLabel("Quantidade:");
        JTextField quantidadeField = new JTextField(20);
        JLabel precoLabel = new JLabel("Preço:");
        JTextField precoField = new JTextField(20);
        JLabel pontosLabel = new JLabel("Pontos para Resgate:");
        JTextField pontosField = new JTextField(20);
        JButton updateButton = new JButton("Atualizar Produto");

        // Preenche os campos com os valores atuais do produto
        nameField.setText(name);
        quantidadeField.setText(quantidade);
        precoField.setText(preco);
        pontosField.setText(pontos);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(nameLabel, gbc);

        gbc.gridx++;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(quantidadeLabel, gbc);

        gbc.gridx++;
        panel.add(quantidadeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(precoLabel, gbc);

        gbc.gridx++;
        panel.add(precoField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(pontosLabel, gbc);

        gbc.gridx++;
        panel.add(pontosField, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(updateButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String quantidadeStr = quantidadeField.getText();
                String precoStr = precoField.getText();
                String pontosStr = pontosField.getText();

                if (name.isEmpty() || quantidadeStr.isEmpty() || precoStr.isEmpty() || pontosStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantidade;
                float preco;
                int pontosResgate;

                try {
                    quantidade = Integer.parseInt(quantidadeStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Quantidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    preco = Float.parseFloat(precoStr.replace(",", "."));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Preço deve ser um número decimal.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    pontosResgate = Integer.parseInt(pontosStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Pontos para resgate devem ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String updateQuery = "UPDATE Produtos SET name = ?, quantidade = ?, preco = ?, pontos_resgate = ? WHERE id = ?";
                    PreparedStatement statement = conn.prepareStatement(updateQuery);
                    statement.setString(1, name);
                    statement.setInt(2, quantidade);
                    statement.setFloat(3, preco);
                    statement.setInt(4, pontosResgate);
                    statement.setString(5, id);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(panel, rowsAffected + " produto(s) atualizado(s) com sucesso.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao atualizar produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    
    private static JPanel createDeleteProductPanel(MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel idLabel = new JLabel("ID do Produto:");
        JTextField idField = new JTextField(10);
        JButton deleteButton = new JButton("Excluir Produto");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(idLabel, gbc);

        gbc.gridx++;
        panel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(deleteButton, gbc);

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int productId;

                try {
                    productId = Integer.parseInt(idField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "ID deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String deleteQuery = "DELETE FROM Produtos WHERE id = ?";
                    PreparedStatement statement = conn.prepareStatement(deleteQuery);
                    statement.setInt(1, productId);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(panel, rowsAffected + " produto(s) excluído(s) com sucesso.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao excluir produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

}
