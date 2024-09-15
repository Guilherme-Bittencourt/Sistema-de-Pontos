import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.mindrot.jbcrypt.BCrypt;
import com.mysql.cj.jdbc.MysqlDataSource;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class UserRegistrationGUI {
    public static void main(String[] args) {
    	        // Configuração da fonte de dados MySQL
    	        MysqlDataSource ds = new MysqlDataSource();
    	        ds.setUrl("jdbc:mysql://10.0.0.109:3306/Fidelity"); // Substitua 'seu_banco_de_dados' pelo nome do seu banco de dados
    	        ds.setUser("dba"); // Substitua 'seu_usuario' pelo seu usuário MySQL
    	        ds.setPassword("admin123"); // Substitua 'sua_senha' pela sua senha MySQL

    	        try (Connection connection = ds.getConnection(); Statement statement = connection.createStatement()) {
    	            // Criação da tabela 'Produtos' se não existir
    	        	String createTableSQL = "CREATE TABLE IF NOT EXISTS Clientes ("
    	                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
    	                    + "username VARCHAR(255) NOT NULL, "
    	                    + "CPF VARCHAR(14) NOT NULL UNIQUE, "
    	                    + "telefone VARCHAR(15), "
    	                    + "endereço VARCHAR(255), "
    	                    + "pontos INTEGER DEFAULT 0)";
    	    statement.execute(createTableSQL);
    	    System.out.println("Tabela 'Clientes' criada com sucesso.");
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    }

    // Iniciar a GUI de registro de usuário
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
        JFrame frame = new JFrame("Cadastro de Usuários");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 550);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Criar Usuário", createCreateUserPanel(frame, ds));
        tabbedPane.addTab("Listar Usuários", createListClientesPanel(ds));
        tabbedPane.addTab("Atualizar Usuário", createUpdateUserPanel(ds, "", "", "", ""));
        tabbedPane.addTab("Atribuir Pontos", createAssignPointsPanel(ds));

        frame.getContentPane().add(tabbedPane);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createCreateUserPanel(JFrame frame, MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel nameLabel = new JLabel("Nome:");
        JTextField nameField = new JTextField(20);
        JLabel phoneLabel = new JLabel("Telefone:");
        JTextField phoneField = new JTextField(20);
        JLabel addressLabel = new JLabel("Endereço:");
        JTextField addressField = new JTextField(20);
        JLabel cpfLabel = new JLabel("CPF:");
        JTextField cpfField = new JTextField(20);
        JButton createButton = new JButton("Adicionar Usuário");
        JButton backButton = new JButton("Menu Principal");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(nameLabel, gbc);

        gbc.gridy++;
        panel.add(phoneLabel, gbc);

        gbc.gridy++;
        panel.add(addressLabel, gbc);

        gbc.gridy++;
        panel.add(cpfLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridy++;
        panel.add(phoneField, gbc);

        gbc.gridy++;
        panel.add(addressField, gbc);

        gbc.gridy++;
        panel.add(cpfField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(backButton, gbc);

        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String phone = phoneField.getText();
                String address = addressField.getText();
                String cpf = cpfField.getText();

                if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || cpf.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (phone.length() < 9 || phone.length() > 11) {
                    JOptionPane.showMessageDialog(panel, "O telefone deve ter entre 9 e 11 dígitos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (cpf.length() != 11) {
                    JOptionPane.showMessageDialog(panel, "O CPF deve ter 11 dígitos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    // Verificação de duplicidade de CPF e telefone
                    String checkQuery = "SELECT COUNT(*) FROM Clientes WHERE CPF = ? OR telefone = ?";
                    PreparedStatement checkStatement = conn.prepareStatement(checkQuery);
                    String encryptedCpf = encrypt(cpf, generateKey()); // Criptografa o CPF
                    checkStatement.setString(1, encryptedCpf);
                    checkStatement.setString(2, phone);
                    ResultSet checkResultSet = checkStatement.executeQuery();
                    checkResultSet.next();
                    int count = checkResultSet.getInt(1);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(panel, "CPF ou Telefone já cadastrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Inserir o CPF criptografado
                    String insertQuery = "INSERT INTO Clientes (username, telefone, endereço, CPF, `key`) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(insertQuery);
                    statement.setString(1, name);
                    statement.setString(2, phone);
                    statement.setString(3, address);
                    statement.setString(4, encryptedCpf);
                    statement.setString(5, Base64.getEncoder().encodeToString(generateKey().getEncoded())); // Armazena a chave criptografada
                    int rowsAffected = statement.executeUpdate();
                    JOptionPane.showMessageDialog(panel, rowsAffected + " registro inserido com sucesso.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao adicionar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                MenuGUI.main(new String[0]); // Abre a janela do menu principal
            }
        });

        return panel;
    }

    private static JPanel createListClientesPanel(MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel searchLabel = new JLabel("Buscar (CPF ou Telefone):");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Buscar");
        JButton listButton = new JButton("Listar Todos");
        JButton updateButton = new JButton("Atualizar");
        JButton deleteButton = new JButton("Excluir");
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Telefone", "Endereço", "Pontos"}, 0) { // Removido CPF da tabela
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede a edição das células
            }
        };
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
                    JOptionPane.showMessageDialog(panel, "Digite o CPF ou Telefone para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String selectQuery = "SELECT id, username, telefone, endereço, pontos FROM Clientes WHERE telefone = ? OR CPF = ?";
                    PreparedStatement statement = conn.prepareStatement(selectQuery);
                    statement.setString(1, searchQuery);
                    statement.setString(2, searchQuery);
                    ResultSet resultSet = statement.executeQuery();
                    model.setRowCount(0); // Limpar a tabela

                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("username");
                        String phone = resultSet.getString("telefone");
                        String address = resultSet.getString("endereço");
                        int pontos = resultSet.getInt("pontos");
                        model.addRow(new Object[]{id, name, phone, address, pontos});
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(panel, "Nenhum usuário encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao carregar usuários.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        listButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel passwordPanel = new JPanel();
                JPasswordField passwordField = new JPasswordField(20);
                passwordPanel.add(new JLabel("Digite a senha para listar todos os registros:"));
                passwordPanel.add(passwordField);

                int option = JOptionPane.showConfirmDialog(panel, passwordPanel, "Autenticação Necessária", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    String password = new String(passwordField.getPassword());

                    if ("admin123".equals(password)) {
                        try (Connection conn = ds.getConnection()) {
                            String selectQuery = "SELECT id, username, telefone, endereço, pontos FROM Clientes";
                            PreparedStatement statement = conn.prepareStatement(selectQuery);
                            ResultSet resultSet = statement.executeQuery();
                            model.setRowCount(0); // Limpar a tabela

                            while (resultSet.next()) {
                                int id = resultSet.getInt("id");
                                String name = resultSet.getString("username");
                                String phone = resultSet.getString("telefone");
                                String address = resultSet.getString("endereço");
                                int points = resultSet.getInt("pontos");
                                model.addRow(new Object[]{id, name, phone, address, points});
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(panel, "Erro ao carregar usuários.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel, "Senha incorreta.", "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    String id = model.getValueAt(selectedRow, 0).toString();
                    String name = model.getValueAt(selectedRow, 1).toString();
                    String phone = model.getValueAt(selectedRow, 2).toString();
                    String address = model.getValueAt(selectedRow, 3).toString();

                    JFrame updateFrame = new JFrame("Atualizar Usuário");
                    updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    JPanel updatePanel = createUpdateUserPanel(ds, name, phone, address, id);
                    updateFrame.getContentPane().add(updatePanel);
                    updateFrame.pack();
                    updateFrame.setLocationRelativeTo(null);
                    updateFrame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(panel, "Selecione um usuário para atualizar.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Solicita confirmação antes de excluir
                int confirm = JOptionPane.showConfirmDialog(panel, "Você realmente deseja excluir este usuário?", "Confirmação de Exclusão", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Obtém o ID do usuário selecionado na tabela
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int userId = (int) model.getValueAt(selectedRow, 0);

                        try (Connection conn = ds.getConnection()) {
                            String deleteQuery = "DELETE FROM Clientes WHERE id = ?";
                            PreparedStatement statement = conn.prepareStatement(deleteQuery);
                            statement.setInt(1, userId);
                            int rowsAffected = statement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(panel, "Usuário excluído com sucesso.");
                                model.removeRow(selectedRow);
                            } else {
                                JOptionPane.showMessageDialog(panel, "Erro ao excluir usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(panel, "Erro ao excluir usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nenhum usuário selecionado para exclusão.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        return panel;
    }


    private static JPanel createUpdateUserPanel(MysqlDataSource ds, String name, String phone, String address, String cpf) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel searchLabel = new JLabel("Buscar (CPF ou Telefone):");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Buscar");
        JLabel nameLabel = new JLabel("Nome:");
        JTextField nameField = new JTextField(20);
        JLabel phoneLabel = new JLabel("Telefone:");
        JTextField phoneField = new JTextField(20);
        JLabel addressLabel = new JLabel("Endereço:");
        JTextField addressField = new JTextField(20);
        JTextField cpfField = new JTextField(20);
        JButton updateButton = new JButton("Atualizar Usuário");

        // Inicializa os campos com os dados fornecidos
        searchField.setText(phone);
        nameField.setText(name);
        phoneField.setText(phone);
        addressField.setText(address);
        cpfField.setText(cpf);

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

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(nameLabel, gbc);

        gbc.gridx++;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(phoneLabel, gbc);

        gbc.gridx++;
        panel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(addressLabel, gbc);

        gbc.gridx++;
        panel.add(addressField, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(updateButton, gbc);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchQuery = searchField.getText();

                if (searchQuery.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Digite o CPF ou Telefone para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String selectQuery = "SELECT * FROM Clientes WHERE CPF = ? OR telefone = ?";
                    PreparedStatement statement = conn.prepareStatement(selectQuery);
                    statement.setString(1, searchQuery);
                    statement.setString(2, searchQuery);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        nameField.setText(resultSet.getString("username"));
                        phoneField.setText(resultSet.getString("telefone"));
                        addressField.setText(resultSet.getString("endereço"));
                        cpfField.setText(resultSet.getString("CPF"));
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nenhum usuário encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao buscar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String phone = phoneField.getText();
                String address = addressField.getText();
                String cpf = cpfField.getText();

                if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || cpf.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Todos os campos devem ser preenchidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String updateQuery = "UPDATE Clientes SET username = ?, telefone = ?, endereço = ? WHERE CPF = ?";
                    PreparedStatement statement = conn.prepareStatement(updateQuery);
                    statement.setString(1, name);
                    statement.setString(2, phone);
                    statement.setString(3, address);
                    statement.setString(4, cpf);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(panel, "Usuário atualizado com sucesso.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Erro ao atualizar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao atualizar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }





    
    private static JPanel createAssignPointsPanel(MysqlDataSource ds) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel idLabel = new JLabel("CPF ou Telefone do Usuário:");
        JButton searchButton = new JButton("Buscar");

        JLabel purchaseLabel = new JLabel("Valor da Compra:");
        JTextField purchaseField = new JTextField(10);
        JButton calculateButton = new JButton("Calcular Pontos");
        JLabel deductPoints = new JLabel("Redução de Pontos:");
        JTextField negativePoints = new JTextField(10);
        JButton lowerButton = new JButton("Reduzir Pontos");
        JTextField searchField = new JTextField(20);

        JLabel pointsLabel = new JLabel("Pontos do Usuário:");
        JTextField pointsField = new JTextField(10);
        pointsField.setEditable(true);

        JButton assignButton = new JButton("Atribuir Pontos");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(idLabel, gbc);

        gbc.gridx++;
        panel.add(searchField, gbc);

        gbc.gridx++;
        panel.add(searchButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(purchaseLabel, gbc);

        gbc.gridx++;
        panel.add(purchaseField, gbc);

        gbc.gridx++;
        panel.add(calculateButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(pointsLabel, gbc);

        gbc.gridx++;
        panel.add(pointsField, gbc);

        gbc.gridx++;
        panel.add(assignButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(deductPoints, gbc);

        gbc.gridx++;
        panel.add(negativePoints, gbc);

        gbc.gridx++;
        panel.add(lowerButton, gbc);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchQuery = searchField.getText();

                if (searchQuery.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Digite o CPF ou Telefone para buscar.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String selectQuery = "SELECT * FROM Clientes WHERE CPF = ? OR telefone = ?";
                    PreparedStatement statement = conn.prepareStatement(selectQuery);
                    statement.setString(1, searchQuery);
                    statement.setString(2, searchQuery);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        int points = resultSet.getInt("pontos");
                        pointsField.setText(Integer.toString(points));
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nenhum usuário encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                        pointsField.setText("Nenhum usuário encontrado.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao carregar usuários.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double purchaseAmount = Double.parseDouble(purchaseField.getText());
                    int points = calculatePoints(purchaseAmount);
                    pointsField.setText(Integer.toString(points));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Digite um valor válido para a compra.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        assignButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchQuery = searchField.getText();
                int pointsToAdd = Integer.parseInt(pointsField.getText());

                if (pointsToAdd <= 0) {
                    JOptionPane.showMessageDialog(panel, "Erro! Insira valores positivos para atribuir.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String updateQuery = "UPDATE Clientes SET pontos = pontos + ? WHERE CPF = ? OR telefone = ?";
                    PreparedStatement statement = conn.prepareStatement(updateQuery);
                    statement.setInt(1, pointsToAdd);
                    statement.setString(2, searchQuery);
                    statement.setString(3, searchQuery);
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(panel, pointsToAdd + " pontos atribuídos com sucesso ao usuário com CPF ou Telefone: " + searchQuery);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nenhum usuário encontrado para atribuir pontos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao atribuir pontos ao usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        lowerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchQuery = searchField.getText();
                int pointsToReduce = Integer.parseInt(negativePoints.getText());

                if (pointsToReduce <= 0) {
                    JOptionPane.showMessageDialog(panel, "Erro! Insira valores positivos para reduzir.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = ds.getConnection()) {
                    String updateQuery = "UPDATE Clientes SET pontos = pontos - ? WHERE CPF = ? OR telefone = ?";
                    PreparedStatement statement = conn.prepareStatement(updateQuery);
                    statement.setInt(1, pointsToReduce);
                    statement.setString(2, searchQuery);
                    statement.setString(3, searchQuery);
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(panel, pointsToReduce + " pontos reduzidos com sucesso do usuário com CPF ou Telefone: " + searchQuery);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nenhum usuário encontrado para reduzir pontos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao reduzir pontos do usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        return panel;
        
    }

    
    private static int calculatePoints(double purchaseAmount) {
        double points = purchaseAmount * 0.03;

        int roundedPoints = (int) Math.round(points);
        return roundedPoints;
    }
    
}