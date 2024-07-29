import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.sqlite.SQLiteDataSource;

public class ProductRegistrationGUI {
    public static void main(String[] args) {
        // Configuração da fonte de dados SQLite
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:Produtos.db");

        try (Connection connection = ds.getConnection(); Statement statement = connection.createStatement()) {
            // Criação da tabela 'Produtos'
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Produtos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "quantidade INTEGER NOT NULL, "
                    + "preco FLOAT NOT NULL)";
            statement.execute(createTableSQL);
            System.out.println("Tabela 'Produtos' criada com sucesso.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> createAndShowGUI(ds));
    }

    private static void createAndShowGUI(SQLiteDataSource ds) {
        JFrame frame = new JFrame("Gerenciador de Produtos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 550); // Ajuste o tamanho aqui
        frame.setLocationRelativeTo(null); // Centraliza o JFrame na tela

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Adicionar Produto", createCreateProductPanel(frame, ds));
        tabbedPane.addTab("Listar Produtos", createListProductPanel(ds));
        tabbedPane.addTab("Excluir Produto", createDeleteProductPanel(ds));
        tabbedPane.addTab("Atualizar Produto", createUpdateProductPanel(ds));

        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createCreateProductPanel(JFrame frame, SQLiteDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel nameLabel = new JLabel("Nome:");
        JTextField nameField = new JTextField(20);
        JLabel quantidadeLabel = new JLabel("Quantidade:");
        JTextField quantidadeField = new JTextField(20);
        JLabel precoLabel = new JLabel("Preço:");
        JTextField precoField = new JTextField(20);
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

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridy++;
        panel.add(quantidadeField, gbc);

        gbc.gridy++;
        panel.add(precoField, gbc);

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

                if (name.isEmpty() || quantidadeStr.isEmpty() || precoStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantidade;
                float preco;

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

                if (quantidade < 0) {
                    JOptionPane.showMessageDialog(panel, "Quantidade não pode ser negativa.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String insertQuery = "INSERT INTO Produtos (name, quantidade, preco) VALUES (?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(insertQuery);
                    statement.setString(1, name);
                    statement.setInt(2, quantidade);
                    statement.setFloat(3, preco);

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

    private static JPanel createListProductPanel(SQLiteDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel searchLabel = new JLabel("Buscar (Nome):");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Buscar");
        JButton listButton = new JButton("Listar Todos");
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Nome", "Quantidade", "Preço"}, 0);
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

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchQuery = searchField.getText();

                if (searchQuery.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Digite o Nome para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
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
                        model.addRow(new Object[]{id, name, quantidade, preco});
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

        return panel;
    }

    private static void showPasswordDialogAndListProducts(SQLiteDataSource ds, DefaultTableModel model) {
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

    private static void listProducts(SQLiteDataSource ds, DefaultTableModel model) {
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
                model.addRow(new Object[]{id, name, quantidade, preco});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao carregar produtos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static JPanel createDeleteProductPanel(SQLiteDataSource ds) {
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

    private static JPanel createUpdateProductPanel(SQLiteDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel idLabel = new JLabel("ID do Produto:");
        JTextField idField = new JTextField(10);
        JButton searchButton = new JButton("Buscar");
        JLabel nameLabel = new JLabel("Nome:");
        JTextField nameField = new JTextField(20);
        JLabel quantidadeLabel = new JLabel("Quantidade:");
        JTextField quantidadeField = new JTextField(20);
        JLabel precoLabel = new JLabel("Preço:");
        JTextField precoField = new JTextField(20);
        JButton updateButton = new JButton("Atualizar Produto");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(idLabel, gbc);

        gbc.gridx++;
        panel.add(idField, gbc);

        gbc.gridx++;
        panel.add(searchButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
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
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(updateButton, gbc);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int productId;

                try {
                    productId = Integer.parseInt(idField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "ID deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String selectQuery = "SELECT * FROM Produtos WHERE id = ?";
                    PreparedStatement statement = conn.prepareStatement(selectQuery);
                    statement.setInt(1, productId);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        nameField.setText(resultSet.getString("name"));
                        quantidadeField.setText(Integer.toString(resultSet.getInt("quantidade")));
                        precoField.setText(Float.toString(resultSet.getFloat("preco")));
                    } else {
                        JOptionPane.showMessageDialog(panel, "Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao buscar produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int productId;
                String name = nameField.getText();
                String quantidadeStr = quantidadeField.getText();
                String precoStr = precoField.getText();

                if (name.isEmpty() || quantidadeStr.isEmpty() || precoStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantidade;
                float preco;

                try {
                    productId = Integer.parseInt(idField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "ID deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

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

                if (quantidade < 0) {
                    JOptionPane.showMessageDialog(panel, "Quantidade não pode ser negativa.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String updateQuery = "UPDATE Produtos SET name = ?, quantidade = ?, preco = ? WHERE id = ?";
                    PreparedStatement statement = conn.prepareStatement(updateQuery);
                    statement.setString(1, name);
                    statement.setInt(2, quantidade);
                    statement.setFloat(3, preco);
                    statement.setInt(4, productId);
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
}
