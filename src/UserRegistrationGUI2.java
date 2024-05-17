import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class UserRegistrationGUI extends JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/myDB2";
    private static final String USER = "root";
    private static final String PASSWORD = "Senha135";

    private JTextField usernameField, telefoneField, enderecoField, cpfField, idField, pontosField;

    public UserRegistrationGUI() {
        super("User Registration");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2));

        JLabel nameLabel = new JLabel("Nome:");
        usernameField = new JTextField();
        add(nameLabel);
        add(usernameField);

        JLabel telefoneLabel = new JLabel("Telefone:");
        telefoneField = new JTextField();
        add(telefoneLabel);
        add(telefoneField);

        JLabel enderecoLabel = new JLabel("Endereço:");
        enderecoField = new JTextField();
        add(enderecoLabel);
        add(enderecoField);

        JLabel cpfLabel = new JLabel("CPF:");
        cpfField = new JTextField();
        add(cpfLabel);
        add(cpfField);

        JLabel idLabel = new JLabel("ID:");
        idField = new JTextField();
        add(idLabel);
        add(idField);

        JLabel pontosLabel = new JLabel("Pontos:");
        pontosField = new JTextField();
        pontosField.setEditable(false);
        add(pontosLabel);
        add(pontosField);

        JButton registerButton = new JButton("Registrar");
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        add(registerButton);

        JButton updateButton = new JButton("Atualizar");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateUser();
            }
        });
        add(updateButton);

        JButton deleteButton = new JButton("Excluir");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });
        add(deleteButton);

        JButton assignPointsButton = new JButton("Atribuir Pontos");
        assignPointsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                assignPoints();
            }
        });
        add(assignPointsButton);

        JButton refreshButton = new JButton("Atualizar Pontos");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshPoints();
            }
        });
        add(refreshButton);

        JButton listUsersButton = new JButton("Listar Usuários");
        listUsersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listUsers();
            }
        });
        add(listUsersButton);
    }

    private void registerUser() {
        String username = usernameField.getText();
        String telefone = telefoneField.getText();
        String endereco = enderecoField.getText();
        String cpf = cpfField.getText();

        if (validateInput(username, telefone, endereco, cpf)) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String insertQuery = "INSERT INTO users (username, telefone, endereço, cpf) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, username);
                    statement.setString(2, telefone);
                    statement.setString(3, endereco);
                    statement.setString(4, cpf);
                    int rowsAffected = statement.executeUpdate();
                    System.out.println(rowsAffected + " registro inserido com sucesso.");
                    JOptionPane.showMessageDialog(this, "Usuário registrado com sucesso!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao registrar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateUser() {
        int userId = Integer.parseInt(idField.getText());
        String newUsername = usernameField.getText();
        String newTelefone = telefoneField.getText();
        String newEndereco = enderecoField.getText();
        String newCpf = cpfField.getText();

        if (validateInput(newUsername, newTelefone, newEndereco, newCpf)) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String updateQuery = "UPDATE users SET username = ?, telefone = ?, endereço = ?, cpf = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setString(1, newUsername);
                    statement.setString(2, newTelefone);
                    statement.setString(3, newEndereco);
                    statement.setString(4, newCpf);
                    statement.setInt(5, userId);
                    int rowsAffected = statement.executeUpdate();
                    System.out.println(rowsAffected + " registro atualizado com sucesso.");
                    JOptionPane.showMessageDialog(this, "Usuário atualizado com sucesso!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        int userId = Integer.parseInt(idField.getText());

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String deleteQuery = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setInt(1, userId);
                int rowsAffected = statement.executeUpdate();
                System.out.println(rowsAffected + " registro(s) excluído(s) com sucesso.");
                JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignPoints() {
        int userId = Integer.parseInt(idField.getText());
        int pontos = Integer.parseInt(pontosField.getText());

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String updateQuery = "UPDATE users SET pontos = pontos + ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                statement.setInt(1, pontos);
                statement.setInt(2, userId);
                int rowsAffected = statement.executeUpdate();
                System.out.println(rowsAffected + " pontos atribuídos com sucesso ao usuário com ID " + userId);
                JOptionPane.showMessageDialog(this, "Pontos atribuídos com sucesso ao usuário!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao atribuir pontos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPoints() {
        int userId = Integer.parseInt(idField.getText());

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String selectQuery = "SELECT pontos FROM users WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
                statement.setInt(1, userId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int pontos = resultSet.getInt("pontos");
                    pontosField.setText(String.valueOf(pontos));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao atualizar pontos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listUsers() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String selectQuery = "SELECT id, username, telefone, endereço, cpf, COALESCE(pontos, 0) AS pontos FROM users";
            try (PreparedStatement statement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = statement.executeQuery()) {
                StringBuilder usersList = new StringBuilder();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String username = resultSet.getString("username");
                    String telefone = resultSet.getString("telefone");
                    String endereco = resultSet.getString("endereço");
                    String cpf = resultSet.getString("cpf");
                    int pontos = resultSet.getInt("pontos");
                    usersList.append("ID: ").append(id).append(", Nome: ").append(username).append(", Telefone: ").append(telefone)
                            .append(", Endereço: ").append(endereco).append(", CPF: ").append(cpf).append(", Pontos: ").append(pontos).append("\n");
                }
                JOptionPane.showMessageDialog(this, usersList.toString(), "Lista de Usuários", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao listar usuários: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput(String username, String telefone, String endereco, String cpf) {
        if (username.isEmpty() || telefone.isEmpty() || endereco.isEmpty() || cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Campos Vazios", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!telefone.matches("\\d{11}") || !cpf.matches("\\d{11}")) {
            JOptionPane.showMessageDialog(this, "Número de telefone e CPF devem ter 11 dígitos.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UserRegistrationGUI().setVisible(true);
            }
        });
    }
}




