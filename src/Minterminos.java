import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Minterminos {
    private JFrame ventana;
    private JTextArea areaResultado;
    private JComboBox<String> comboVariables;
    private JTextField campoArchivo, campoMiniterminoManual;
    private DefaultListModel<Integer> modeloLista;
    private JList<Integer> listaVisual;
    private final List<Integer> miniterminos = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Minterminos().crearInterfaz());
    }

    private void crearInterfaz() {
        ventana = new JFrame("Generador de Función Booleana");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(800, 600);
        ventana.setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(240, 245, 255));

        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelMiniterminos(), BorderLayout.WEST);
        panelPrincipal.add(crearPanelResultado(), BorderLayout.CENTER);
        panelPrincipal.add(crearEtiquetaAutor(), BorderLayout.SOUTH);

        ventana.add(panelPrincipal);
        ventana.setVisible(true);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(200, 220, 255));

        campoArchivo = new JTextField("miniterminos.txt", 15);
        comboVariables = new JComboBox<>(new String[]{"3", "4"});
        JButton btnProcesar = new JButton("Procesar");
        JButton btnKMap = new JButton("Ver Mapa");
        JButton btnInstrucciones = new JButton("Ver Instrucciones");

        btnProcesar.addActionListener(e -> procesarMiniterminos());
        btnKMap.addActionListener(e -> mostrarKmap());
        btnInstrucciones.addActionListener(e -> mostrarInstrucciones());

        panel.add(new JLabel("Variables:"));
        panel.add(comboVariables);
        panel.add(btnProcesar);
        panel.add(btnKMap);
        panel.add(btnInstrucciones);

        return panel;
    }

    private JPanel crearPanelMiniterminos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(220, 255, 240));

        campoMiniterminoManual = new JTextField(5);
        JButton btnAgregar = new JButton("Agregar");
        JButton btnLimpiar = new JButton("Limpiar");

        modeloLista = new DefaultListModel<>();
        listaVisual = new JList<>(modeloLista);
        listaVisual.setVisibleRowCount(15);

        btnAgregar.addActionListener(e -> agregarMiniterminoManual());
        btnLimpiar.addActionListener(e -> limpiarLista());

        panel.add(new JLabel("Minitérmino:"));
        panel.add(campoMiniterminoManual);
        panel.add(btnAgregar);
        panel.add(btnLimpiar);
        panel.add(new JScrollPane(listaVisual));

        return panel;
    }

    private JScrollPane crearPanelResultado() {
        areaResultado = new JTextArea();
        areaResultado.setEditable(true);
        areaResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaResultado.setBackground(new Color(245, 255, 250));
        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setPreferredSize(new Dimension(150, 100)); 
               return new JScrollPane(areaResultado);
    }

    private JLabel crearEtiquetaAutor() {
        JLabel etiqueta = new JLabel("Autores: Jeffrey Mejía y MaJo Montepeque", SwingConstants.CENTER);
        etiqueta.setFont(new Font("Arial", Font.ITALIC, 7));
        etiqueta.setForeground(new Color(100, 100, 150));
        return etiqueta;
    }

    private void agregarMiniterminoManual() {
        try {
            int valor = Integer.parseInt(campoMiniterminoManual.getText().trim());
            if (!miniterminos.contains(valor)) {
                miniterminos.add(valor);
                modeloLista.addElement(valor);
            }
            campoMiniterminoManual.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(ventana, "Ingrese un número válido.");
        }
    }

    private void limpiarLista() {
        miniterminos.clear();
        modeloLista.clear();
    }

    private void procesarMiniterminos() {
        String archivo = campoArchivo.getText().trim();
        int vars = Integer.parseInt((String) comboVariables.getSelectedItem());

        if (!archivo.isEmpty()) {
            try {
                String contenido = new String(java.nio.file.Files.readAllBytes(new File(archivo).toPath()));
                for (String parte : contenido.split(",")) {
                    int valor = Integer.parseInt(parte.trim());
                    if (!miniterminos.contains(valor)) {
                        miniterminos.add(valor);
                        modeloLista.addElement(valor);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                areaResultado.setText("Error al leer el archivo: " + e.getMessage());
                return;
            }
        }

        Collections.sort(miniterminos);
        areaResultado.setText("");
        areaResultado.append("Minitérminos: " + miniterminos + "\n");
        areaResultado.append("Función Booleana: " + generarFuncion(miniterminos, vars) + "\n");

        QuineMcCluskey qm = new QuineMcCluskey(vars, miniterminos);
        String resultado = qm.simplificar();
        areaResultado.append("Función Simplificada: " + resultado + "\n");
    }

    private String generarFuncion(List<Integer> minterms, int variables) {
        StringBuilder funcion = new StringBuilder();

        for (int i = 0; i < minterms.size(); i++) {
            int val = minterms.get(i);
            String bin = String.format("%" + variables + "s", Integer.toBinaryString(val)).replace(' ', '0');
            for (int j = 0; j < variables; j++) {
                char letra = (char) ('A' + j);
                funcion.append(bin.charAt(j) == '0' ? letra + "'" : letra);
            }
            if (i < minterms.size() - 1) funcion.append(" + ");
        }
        return funcion.toString();
    }

    private void mostrarKmap() {
        int vars = Integer.parseInt((String) comboVariables.getSelectedItem());
        JFrame frame = new JFrame("Mapa de Karnaugh");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(ventana);

        String[][] datos;
        String[] col, fila;
        int[][] posiciones;

        if (vars == 3) {
            col = new String[]{"", "00", "01", "11", "10"};
            fila = new String[]{"0", "1"};
            datos = new String[2][5];
            posiciones = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}};
        } else {
            col = new String[]{"", "00", "01", "11", "10"};
            fila = new String[]{"00", "01", "11", "10"};
            datos = new String[4][5];
            posiciones = new int[][]{
                {0, 1, 3, 2}, {4, 5, 7, 6}, {12, 13, 15, 14}, {8, 9, 11, 10}
            };
        }

        for (int i = 0; i < datos.length; i++) {
            datos[i][0] = fila[i];
            for (int j = 0; j < col.length - 1; j++) {
                int val = posiciones[i][j];
                datos[i][j + 1] = miniterminos.contains(val) ? "1" : "0";
            }
        }

        JTable tabla;
        tabla = new JTable(datos, col) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setFont(new Font("Monospaced", Font.BOLD, 14));
                c.setBackground(col == 0 ? new Color(200, 200, 255) :
                        "1".equals(getValueAt(row, col)) ? new Color(173, 255, 181) : Color.WHITE);
                return c;
            }
        };

        tabla.setEnabled(false);
        tabla.setRowHeight(30);
        frame.add(new JScrollPane(tabla));
        frame.setVisible(true);
    }

    private void mostrarInstrucciones() {
        String texto = leerInstruccionesDesdeXML("instrucciones.xml");
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, 250));
        JOptionPane.showMessageDialog(ventana, scroll, "Instrucciones", JOptionPane.INFORMATION_MESSAGE);
    }

    private String leerInstruccionesDesdeXML(String archivoXML) {
        StringBuilder resultado = new StringBuilder();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(archivoXML));
            doc.getDocumentElement().normalize();

            NodeList pasos = doc.getElementsByTagName("paso");
            for (int i = 0; i < pasos.getLength(); i++) {
                resultado.append(pasos.item(i).getTextContent()).append("\n");
            }
        } catch (Exception e) {
            resultado.append("Error al leer instrucciones XML: ").append(e.getMessage());
        }
        return resultado.toString();
    }
    
    static class QuineMcCluskey {
        private final int numVariables;
        private final List<Integer> minterms;

        public QuineMcCluskey(int numVariables, List<Integer> minterms) {
            this.numVariables = numVariables;
            this.minterms = new ArrayList<>(minterms);
        }

        public String simplificar() {
            Set<String> expresiones = new HashSet<>();
            for (int minterm : minterms) {
                String bin = String.format("%" + numVariables + "s", Integer.toBinaryString(minterm)).replace(' ', '0');
                StringBuilder expresion = new StringBuilder();
                for (int i = 0; i < numVariables; i++) {
                    char var = (char) ('A' + i);
                    expresion.append(bin.charAt(i) == '1' ? var : var + "'");
                }
                expresiones.add(expresion.toString());
            }
            return String.join(" + ", expresiones);
        }
    }
}