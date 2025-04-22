import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * 
 * Autor: Jeffrey Mejía
 * 
 */
public class Minterminos {
    private JFrame ventana;
    private JTextArea areaResultado;
    private JComboBox<String> comboVariables;
    private JTextField campoArchivo;
    private JButton btnProcesar;
    private JButton btnMostrarKmap;
    private JButton btnVerInstrucciones;
    private JLabel etiquetaAutor;
    private List<Integer> listaMiniterminos = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Minterminos().iniciarInterfaz());
    }

    public void iniciarInterfaz() {
        ventana = new JFrame("Generador de Función Booleana");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(700, 500);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        campoArchivo = new JTextField("miniterminos.txt", 20);
        comboVariables = new JComboBox<>(new String[]{"3", "4"});
        btnProcesar = new JButton("Procesar");
        btnMostrarKmap = new JButton("Ver K-Map");
        btnVerInstrucciones = new JButton("Ver Instrucciones");

        btnProcesar.addActionListener(e -> procesarArchivo());
        btnMostrarKmap.addActionListener(e -> mostrarKmap());
        btnVerInstrucciones.addActionListener(e -> {
            String instrucciones = leerInstruccionesDesdeXML("instrucciones.xml");
            JTextArea textArea = new JTextArea(instrucciones);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 250));

            JOptionPane.showMessageDialog(ventana, scrollPane, "Instrucciones", JOptionPane.INFORMATION_MESSAGE);
        });

        panelSuperior.add(new JLabel("Archivo:"));
        panelSuperior.add(campoArchivo);
        panelSuperior.add(new JLabel("Variables:"));
        panelSuperior.add(comboVariables);
        panelSuperior.add(btnProcesar);
        panelSuperior.add(btnMostrarKmap);
        panelSuperior.add(btnVerInstrucciones);

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        JScrollPane scrollResultado = new JScrollPane(areaResultado);

        etiquetaAutor = new JLabel("Autores: Jeffrey Mejía y MaJo Montepeque", SwingConstants.CENTER);
        etiquetaAutor.setFont(new Font("Arial", Font.ITALIC, 12));

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(scrollResultado, BorderLayout.CENTER);
        panelPrincipal.add(etiquetaAutor, BorderLayout.SOUTH);

        ventana.add(panelPrincipal);
        ventana.setVisible(true);
    }

    private void procesarArchivo() {
        String rutaArchivo = campoArchivo.getText();
        int cantidadVariables = Integer.parseInt((String) comboVariables.getSelectedItem());
        listaMiniterminos.clear();

        try {
            String contenido = new String(java.nio.file.Files.readAllBytes(new File(rutaArchivo).toPath()));
            String[] partes = contenido.split(",");
            for (String parte : partes) {
                listaMiniterminos.add(Integer.parseInt(parte.trim()));
            }

            areaResultado.setText("");
            areaResultado.append("Minitérminos leídos: " + listaMiniterminos + "\n");
            areaResultado.append("Función Booleana: " + generarFuncion(listaMiniterminos, cantidadVariables) + "\n");

            QuineMcCluskey simplificador = new QuineMcCluskey(cantidadVariables, listaMiniterminos);
            String resultadoSimplificado = simplificador.simplificar();
            areaResultado.append("Función Simplificada: " + resultadoSimplificado + "\n");

        } catch (IOException ex) {
            areaResultado.setText("Error al leer el archivo: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            areaResultado.setText("Error en el formato de los datos del archivo.");
        }
    }

    private String generarFuncion(List<Integer> miniterminos, int variables) {
        StringBuilder funcion = new StringBuilder();

        for (int i = 0; i < miniterminos.size(); i++) {
            int valor = miniterminos.get(i);
            String binario = String.format("%" + variables + "s", Integer.toBinaryString(valor)).replace(' ', '0');
            for (int j = 0; j < variables; j++) {
                char letra = (char) ('A' + j);
                funcion.append(binario.charAt(j) == '0' ? letra + "'" : letra);
            }
            if (i < miniterminos.size() - 1) {
                funcion.append(" + ");
            }
        }
        return funcion.toString();
    }

    private void mostrarKmap() {
        int numVariables = Integer.parseInt((String) comboVariables.getSelectedItem());
        JFrame ventanaMapa = new JFrame("Mapa de Karnaugh");
        ventanaMapa.setSize(400, 300);
        ventanaMapa.setLocationRelativeTo(ventana);

        String[][] datosTabla;
        String[] columnas;
        String[] filas;
        int[][] posiciones;

        if (numVariables == 3) {
            columnas = new String[]{"", "00", "01", "11", "10"};
            filas = new String[]{"0", "1"};
            datosTabla = new String[2][5];
            posiciones = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}};
        } else {
            columnas = new String[]{"", "00", "01", "11", "10"};
            filas = new String[]{"00", "01", "11", "10"};
            datosTabla = new String[4][5];
            posiciones = new int[][]{
                {0, 1, 3, 2},
                {4, 5, 7, 6},
                {12, 13, 15, 14},
                {8, 9, 11, 10}
            };
        }

        for (int i = 0; i < datosTabla.length; i++) {
            datosTabla[i][0] = filas[i];
            for (int j = 0; j < columnas.length - 1; j++) {
                int valor = posiciones[i][j];
                datosTabla[i][j + 1] = listaMiniterminos.contains(valor) ? "1" : "0";
            }
        }

        JTable tabla = new JTable(datosTabla, columnas) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component celda = super.prepareRenderer(renderer, row, col);
                if (col == 0) {
                    celda.setBackground(Color.LIGHT_GRAY);
                    return celda;
                }
                celda.setBackground("1".equals(getValueAt(row, col)) ? new Color(173, 216, 230) : Color.WHITE);
                return celda;
            }
        };

        tabla.setEnabled(false);
        tabla.setRowHeight(30);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 16));

        JPanel panelMapa = new JPanel(new BorderLayout());
        panelMapa.add(new JLabel(" "), BorderLayout.NORTH);
        panelMapa.add(new JScrollPane(tabla), BorderLayout.CENTER);
        ventanaMapa.add(panelMapa);

        ventanaMapa.setVisible(true);
    }

    private String leerInstruccionesDesdeXML(String archivoXML) {
        StringBuilder instrucciones = new StringBuilder();
        try {
            File archivo = new File(archivoXML);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(archivo);
            doc.getDocumentElement().normalize();

            NodeList listaPasos = doc.getElementsByTagName("paso");
            for (int i = 0; i < listaPasos.getLength(); i++) {
                instrucciones.append(listaPasos.item(i).getTextContent()).append("\n");
            }
        } catch (Exception e) {
            instrucciones.append("Error al leer instrucciones XML: ").append(e.getMessage());
        }
        return instrucciones.toString();
    }
}

class QuineMcCluskey {
    private int numVars;
    private List<Integer> minterms;

    public QuineMcCluskey(int numVars, List<Integer> minterms) {
        this.numVars = numVars;
        this.minterms = minterms;
    }

    public String simplificar() {
        Set<String> implicantes = new HashSet<>();
        for (int m : minterms) {
            implicantes.add(String.format("%" + numVars + "s", Integer.toBinaryString(m)).replace(' ', '0'));
        }

        List<String> combinaciones = new ArrayList<>(implicantes);
        List<String> resultado = combinar(combinaciones);

        StringBuilder expresion = new StringBuilder();
        for (int i = 0; i < resultado.size(); i++) {
            expresion.append(convertirABooleana(resultado.get(i)));
            if (i < resultado.size() - 1) expresion.append(" + ");
        }
        return expresion.toString();
    }

    private List<String> combinar(List<String> lista) {
        List<String> resultado = new ArrayList<>();
        Set<String> utilizados = new HashSet<>();

        for (int i = 0; i < lista.size(); i++) {
            for (int j = i + 1; j < lista.size(); j++) {
                String a = lista.get(i);
                String b = lista.get(j);
                int diferencias = 0, posicion = -1;

                for (int k = 0; k < a.length(); k++) {
                    if (a.charAt(k) != b.charAt(k)) {
                        diferencias++;
                        posicion = k;
                    }
                }

                if (diferencias == 1) {
                    String combinado = a.substring(0, posicion) + "-" + a.substring(posicion + 1);
                    resultado.add(combinado);
                    utilizados.add(a);
                    utilizados.add(b);
                }
            }
        }

        for (String s : lista) {
            if (!utilizados.contains(s)) resultado.add(s);
        }

        return new ArrayList<>(new HashSet<>(resultado));
    }

    private String convertirABooleana(String binario) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < binario.length(); i++) {
            if (binario.charAt(i) == '-') continue;
            char letra = (char) ('A' + i);
            resultado.append(binario.charAt(i) == '0' ? letra + "'" : letra);
        }
        return resultado.toString();
    }
}