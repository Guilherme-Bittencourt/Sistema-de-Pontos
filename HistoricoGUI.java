import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import com.mysql.cj.jdbc.MysqlDataSource;
import javax.swing.table.TableColumnModel;

public class HistoricoGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MysqlDataSource ds = new MysqlDataSource();
                ds.setUrl("jdbc:mysql://10.0.0.109:3306/Fidelity"); // Substitua pelo nome do seu banco de dados
                ds.setUser("dba"); // Substitua pelo seu usuário MySQL
                ds.setPassword("admin123"); // Substitua pela sua senha MySQL
                createAndShowHistoricoGUI(ds);
            }
        });
    }

    private static void createAndShowHistoricoGUI(MysqlDataSource ds) {
        JFrame frame = new JFrame("Histórico de Ações");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Ajuste o tamanho da janela para ser maior
        frame.setLocationRelativeTo(null); // Centraliza a janela na tela

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel filterLabel = new JLabel("Filtrar por Tipo de Ação ou Data (AAAA-MM-DD):");
        JTextField filterField = new JTextField(30); // Aumenta o tamanho do campo de busca
        JButton filterButton = new JButton("Filtrar");
        JButton listAllButton = new JButton("Listar Todos");
        JButton backButton = new JButton("Menu Principal");

        // Configurações para ajustar o tamanho do JTable
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Tipo de Ação", "Descrição", "Data/Hora"}, 0) {
            // Sobrescrever o método para impedir que as células sejam editáveis
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede a edição de qualquer célula
            }
        };
        
        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(750, 300)); // Tamanho preferencial da tabela
        table.setFillsViewportHeight(true);

        // Ajusta o tamanho das colunas
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // ID - coluna mais estreita
        columnModel.getColumn(1).setPreferredWidth(100); // Tipo de Ação - coluna intermediária
        columnModel.getColumn(2).setPreferredWidth(400); // Descrição - maior coluna
        columnModel.getColumn(3).setPreferredWidth(150); // Data/Hora - coluna intermediária

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(750, 400)); // Tamanho preferencial do painel de rolagem

        // Layout dos componentes
        gbc.gridwidth = 1;
        panel.add(backButton, gbc);

        // Linha para o campo de filtro
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // O campo de busca ocupa toda a largura
        panel.add(filterLabel, gbc);
        

        gbc.gridy = 1;
        panel.add(filterField, gbc); // Campo de busca

        // Linha para os botões
        gbc.gridwidth = 1; // Restabelece o gridwidth para 1
        gbc.gridx = 1;
        panel.add(filterButton, gbc); // Botão de filtro

        gbc.gridx = 0;
        panel.add(listAllButton, gbc); // Botão de listar todos
        

        // Linha para a tabela
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4; // A tabela ocupa toda a largura
        gbc.fill = GridBagConstraints.BOTH; // Ocupar espaço horizontal e vertical
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollPane, gbc); // Adiciona o painel de rolagem com a tabela

        
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha a janela do gerenciador de produtos
                MenuGUI.main(new String[0]); // Abre a janela do menu principal
            }
        });
        
        
        // Filtrar histórico com base no tipo de ação ou data
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String filterQuery = filterField.getText();
                model.setRowCount(0); // Limpar a tabela

                try (Connection conn = ds.getConnection()) {
                    String query = "SELECT * FROM Historico WHERE tipo_acao LIKE ? OR data_hora LIKE ?";
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setString(1, "%" + filterQuery + "%");
                    statement.setString(2, "%" + filterQuery + "%");
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String tipoAcao = rs.getString("tipo_acao");
                        String descricao = rs.getString("descricao");
                        Timestamp dataHora = rs.getTimestamp("data_hora");
                        model.addRow(new Object[]{id, tipoAcao, descricao, dataHora});
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(panel, "Nenhum registro encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao carregar histórico.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Listar todos os registros do histórico
        listAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.setRowCount(0); // Limpar a tabela

                try (Connection conn = ds.getConnection()) {
                    String query = "SELECT * FROM Historico";
                    PreparedStatement statement = conn.prepareStatement(query);
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String tipoAcao = rs.getString("tipo_acao");
                        String descricao = rs.getString("descricao");
                        Timestamp dataHora = rs.getTimestamp("data_hora");
                        model.addRow(new Object[]{id, tipoAcao, descricao, dataHora});
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Erro ao carregar histórico.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}