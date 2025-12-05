import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame ventana = new JFrame("BLOC DE NOTAS BÁSICO");
        ventana.setLayout(new BorderLayout());
        ventana.setSize(720, 480);
        ventana.setResizable(true);
        JTextArea textArea = new JTextArea(20, 80);
        ventana.add(textArea, BorderLayout.CENTER);
        textArea.setBackground(new Color(205, 199, 199));

        // region JScrollPane
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        ventana.add(scroll, BorderLayout.CENTER);
        // endregion

        // region JMenuBar
        JMenuBar mb = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenu ayuda = new JMenu("Ayuda");

        mb.add(archivo);
        mb.add(ayuda);

        ventana.setJMenuBar(mb);
        // endregion

        crearMenuCargarArchivo(ventana, textArea, archivo);

        crearMenuGuardarArchivo(ventana, textArea, archivo);
        configurarAtajoGuardar(ventana, textArea);

        crearMenuTextoFormateado(ventana, ayuda);
        crearMenuAtajos(ventana, ayuda);
        crearMenuAbrirWeb(ventana, ayuda, "GitHub", "https://github.com/Gacortor451");

        ventana.add(crearBarraPlantillas(textArea), BorderLayout.EAST);

        // region JPopupMenu
        JPopupMenu popup = new JPopupMenu();

        JMenuItem copiar = new JMenuItem("Copiar");
        copiar.addActionListener(e -> textArea.copy());
        popup.add(copiar);

        JMenuItem pegar = new JMenuItem("Pegar");
        pegar.addActionListener(e -> textArea.paste());
        popup.add(pegar);

        JMenuItem cortar = new JMenuItem("Cortar");
        cortar.addActionListener(e -> textArea.cut());
        popup.add(cortar);

        textArea.setComponentPopupMenu(popup);
        // endregion

        configurarConfirmacionSalida(ventana);
        ventana.setVisible(true);
    }

    // region Guardar

    /**
     * Crea y configura un elemento de menú "Guardar archivo".
     *
     * @param ventana  ventana principal de la aplicación (JFrame)
     * @param textArea textArea cuyo contenido se guardará en el archivo seleccionado
     * @param archivo  el menú "Archivo" al que se añadirá este nuevo ítem
     */
    private static void crearMenuGuardarArchivo(JFrame ventana, JTextArea textArea, JMenu archivo) {
        JMenuItem guardarArchivo = new JMenuItem("Guardar archivo");
        guardarArchivo.addActionListener(e -> guardarArchivo(ventana, textArea));
        archivo.add(guardarArchivo);
    }

    /**
     * Lógica para guardar el contenido del JTextArea en un archivo elegido por el usuario.
     *
     * @param ventana  ventana principal de la aplicación (JFrame)
     * @param textArea textArea cuyo contenido se guardará en el archivo seleccionado
     */
    private static void guardarArchivo(JFrame ventana, JTextArea textArea) {
        JFileChooser chooser = new JFileChooser();
        // Mensaje de aviso
        JOptionPane.showMessageDialog(ventana,
                "Tenga cuidado de no sobrescribir un archivo, es decir,\n" +
                        "si llamas a tu archivo igual que uno ya existente, el anterior perderá su información.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
        );
        chooser.setFileFilter(new FileNameExtensionFilter("Filtrando por '.txt'", "txt"));

        if (chooser.showSaveDialog(ventana) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();

            // Añadir la extensión ".txt" si no se agrega ninguna
            if (!archivo.getName().contains(".")) {
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            }

            try (BufferedWriter fw = new BufferedWriter(new FileWriter(archivo))) {
                fw.write(textArea.getText()); // Sobrescribe o crea el archivo
                JOptionPane.showMessageDialog(ventana,
                        "Archivo guardado en:\n" + archivo.getAbsolutePath(),
                        "Guardado exitoso",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ventana,
                        "Error al guardar el archivo:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    /**
     * Configura el atajo de teclado Ctrl+S para guardar
     *
     * @param ventana  ventana principal de la aplicación (JFrame)
     * @param textArea textArea cuyo contenido se guardará en el archivo
     */
    private static void configurarAtajoGuardar(JFrame ventana, JTextArea textArea) {
        KeyStroke ctrlS = KeyStroke.getKeyStroke("control S");
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlS, "guardar");
        textArea.getActionMap().put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarArchivo(ventana, textArea);
            }
        });
    }

    // endregion


    /**
     * Crea y configura un elemento de menú "Cargar archivo".
     *
     * @param ventana  ventana principal de la aplicación (JFrame)
     * @param textArea textArea donde se cargará el contenido del archivo seleccionado
     * @param archivo  el menú "Archivo" al que se añadirá este nuevo ítem
     */
    private static void crearMenuCargarArchivo(JFrame ventana, JTextArea textArea, JMenu archivo) {
        JMenuItem cargarArchivo = new JMenuItem("Abrir archivo");
        cargarArchivo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Filtrando por '.txt'", "txt"));
            if (chooser.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()));
                    textArea.read(br, null); // Carga el contenido del archivo en un textArea
                    // Sustituto de = while ((line = br.readLine()) != null) { ... }
                    br.close();
                    JOptionPane.showMessageDialog(ventana,
                            "Archivo obtenido desde:\n" + chooser.getSelectedFile().getAbsolutePath(),
                            "Carga exitosa",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ventana,
                            "Error al cargar el archivo:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        archivo.add(cargarArchivo);
    }

    /**
     * Muestra un mensaje de respuesta
     *
     * @param ventana ventana principal de la aplicación (JFrame)
     * @param ayuda   el menú "Ayuda" al que se añadirá este nuevo ítem
     */
    public static void crearMenuTextoFormateado(JFrame ventana, JMenu ayuda) {
        JMenuItem textoFormateado = new JMenuItem("Texto formateado");
        textoFormateado.addActionListener(e ->
                JOptionPane.showMessageDialog(ventana,
                        "Contenido no disponible: MARKDOWN",
                        "Contenido no disponible",
                        JOptionPane.INFORMATION_MESSAGE
                ));
        ayuda.add(textoFormateado);
    }

    /**
     * Crea y configura un elemento de menú que abre una página web en el navegador.
     *
     * @param ventana ventana principal de la aplicación (JFrame)
     * @param ayuda   el menú "Ayuda" al que se añadirá este nuevo ítem
     * @param nombre  nombre del ítem de menú
     * @param url     dirección web que se abrirá al hacer clic
     */
    public static void crearMenuAbrirWeb(JFrame ventana, JMenu ayuda, String nombre, String url) {
        JMenuItem abrirWeb = new JMenuItem(nombre);
        abrirWeb.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(url)); // Abre la URL en el navegador predeterminado
            } catch (IOException | URISyntaxException ex) {
                JOptionPane.showMessageDialog(ventana,
                        "No se pudo acceder a la página web:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        ayuda.add(abrirWeb);
    }

    /**
     * Muestra un mensaje de respuesta
     *
     * @param ventana ventana principal de la aplicación (JFrame)
     * @param ayuda   el menú "Ayuda" al que se añadirá este nuevo ítem
     */
    public static void crearMenuAtajos(JFrame ventana, JMenu ayuda) {
        JMenuItem textoFormateado = new JMenuItem("Atajos disponibles");
        textoFormateado.addActionListener(e ->
                JOptionPane.showMessageDialog(ventana,
                        "Ctrl + s: Guardar archivo",
                        "Atajos disponibles",
                        JOptionPane.INFORMATION_MESSAGE
                ));
        ayuda.add(textoFormateado);
    }

    /**
     * Crea una barra de herramientas situada a la derecha con dos botones de plantillas.
     *
     * @param textArea textArea donde se insertarán las plantillas
     * @return una JToolBar configurada
     */
    private static JToolBar crearBarraPlantillas(JTextArea textArea) {
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false); // Esto inhabilita la posibilidad de arrastrar la barra de herramientas

        // Botón 2: plantilla con signos "-"
        JButton btnGuiones = new JButton("Lista: - ");
        btnGuiones.addActionListener(e -> {
            String plantillaGuiones =
                    """       
                            \n\t-\s
                            \t-\s
                            \t-\s""";
            textArea.append(plantillaGuiones); // Añade la plantilla al final del contenido
        });
        toolBar.add(btnGuiones);

        // Botón 2: plantilla con signos "+"
        JButton btnMas = new JButton("Lista: +");
        btnMas.addActionListener(e -> {
            String plantillaMas =
                    """
                            \n\t+\s
                            \t+\s
                            \t+\s""";
            textArea.append(plantillaMas); // Añade la plantilla al final del contenido
        });

        toolBar.add(btnMas);
        return toolBar;
    }

    /**
     * Configura un mensaje de confirmación al intentar cerrar la ventana principal.
     *
     * @param ventana ventana principal de la aplicación (JFrame)
     */
    private static void configurarConfirmacionSalida(JFrame ventana) {
        ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int opcion = JOptionPane.showConfirmDialog(
                        ventana,
                        "¿Está seguro que desea salir?",
                        "¿Cerrar?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (opcion == JOptionPane.YES_OPTION) {
                    ventana.dispose(); // Cierra la ventana
                }
            }
        });
    }

}

