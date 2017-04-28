package VisorFicheros;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.*;

/**
 * @author fmolina
 * @version 1.0 28/04/2017
 */
public class Main extends javax.swing.JFrame {

    /**
     * Creates new form Main
     */
    Pattern pat = null;
    Matcher mat = null;
    private String ruta, usuario, contraseña, contenido, fichero = "fichero";
    private File directorio;
    JTextArea texto = new JTextArea(10, 20);
    static final Logger logger = Logger.getLogger("MyLog");
    SecretKey clave2 = null;

    /**
     * Constructor de clase
     */
    public Main() {
        initComponents();
        setLocationRelativeTo(null);

        FileHandler fh;
        //Detecta si ejecuta desde windows o linux y cambia la ruta del directorio
        String osName = System.getProperty("os.name");
        try {
            if (osName.toUpperCase().contains("WIN")) {
                //Windows
                ruta = ".\\Datos\\";
                fh = new FileHandler(".\\psp_ut06.log", true);

            } else {
                //GNU/Linux, MacOS, etc...            
                ruta = "./Datos/";
                fh = new FileHandler("./psp_ut06.log", true);
            }
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException | SecurityException ex) {
            logger.log(Level.SEVERE, "Error: ", ex);
            System.out.println("Error " + ex);
        }
        Listar();
    }

    /**
     * Método para listar los ficheros creados
     *
     * @param
     * @return
     * @exception
     */
    private void Listar() {
        // Muestra los ficheros en el listado
        directorio = new File(ruta);
        if (directorio.exists()) {
            String listado[] = directorio.list();
            for (String elemento : listado) {
                ListadoFicheros.addItem(elemento);
                logger.log(Level.INFO, "Ficheros listados");
            }
        }
    }

    /**
     * Método para esrcibir la cadena de texto en el fichero
     *
     * @param texto: String con la cadena de texto
     * @return
     * @exception IOException
     */
    private void Escribir(String texto) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(ruta + fichero));
            out.write(texto);
            out.close();
            logger.log(Level.INFO, "Fichero escrito: " + fichero.toString());
        } catch (IOException e) {
            System.out.println(e);
            logger.log(Level.SEVERE, "Error: ", e);
        }
    }

    //método que muestra bytes
    public static void mostrarBytes(byte[] buffer) {
        System.out.write(buffer, 0, buffer.length);
    }

    /**
     * Método para encriptar en fichero el contenido
     *
     * @param fichero: String con el nombre del fichero a visualizar
     * @param usuario: String con el usuario
     * @param contraseña: String con la contraseña
     * @return clave2: SecretKey generada
     * @exception NoSuchAlgorithmException, NoSuchPaddingException,
     * FileNotFoundException, IOException IllegalBlockSizeException,
     * BadPaddingException, InvalidKeyException
     */
    private SecretKey cifrarFichero(String fichero, String usuario, String contraseña) throws NoSuchAlgorithmException,
            NoSuchPaddingException, FileNotFoundException, IOException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {

        FileInputStream fentrada = null; //fichero de entrada
        FileOutputStream fsalida = null; //fichero de salida
        int bytesLeidos;

        KeyGenerator keyGen = KeyGenerator.getInstance("Rijndael");
        SecureRandom secure = new SecureRandom();
        // Crea la cadena formada por el nombre de usuario + la contraseña
        String clave1 = usuario + contraseña;
        // Permite establecer el valor de la semilla en base a la cadena clave1
        secure.setSeed(clave1.getBytes());

        // 128 bits
        keyGen.init(128, secure);
        Cipher cifrador = Cipher.getInstance("Rijndael/ECB/PKCS5Padding");

        SecretKey clave2 = keyGen.generateKey();
        // Se inicializa el cifrador en modo CIFRADO o ENCRIPTACIÓN
        cifrador.init(Cipher.ENCRYPT_MODE, clave2);

        String msj = "Cifrado con Rijndael el fichero: " + fichero
                + ", y creado " + fichero + ".cifrado";
        System.out.println(msj);
        logger.log(Level.INFO, msj);

        // Declaración  de objetos
        byte[] buffer = new byte[1000];
        byte[] bufferCifrado;

        fentrada = new FileInputStream(ruta + fichero);
        fsalida = new FileOutputStream(ruta + fichero + ".cifrado");

        bytesLeidos = fentrada.read(buffer, 0, 1000);

        while (bytesLeidos != -1) {
            bufferCifrado = cifrador.update(buffer, 0, bytesLeidos);

            fsalida.write(bufferCifrado);
            bytesLeidos = fentrada.read(buffer, 0, 1000);
        }
        bufferCifrado = cifrador.doFinal();
        fsalida.write(bufferCifrado);

        fentrada.close();
        fsalida.close();

        return clave2;
    }

    /**
     * Método para desencriptar en fichero.descifrado el contenido
     *
     * @param file1: fichero encriptado
     * @param file2: fichero final desencriptado
     * @param key: SecretKey generada
     * @return
     * @exception NoSuchAlgorithmException, NoSuchPaddingException,
     * FileNotFoundException, IOException IllegalBlockSizeException,
     * BadPaddingException, InvalidKeyException
     */
    private void descifrarFichero(String file1, SecretKey key, String file2) throws NoSuchAlgorithmException,
            NoSuchPaddingException, FileNotFoundException, IOException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        FileInputStream fe = null;
        FileOutputStream fs = null;
        int bytesLeidos;

        // Escoge como algoritmo para descifrar Rijndael
        Cipher cifrador = Cipher.getInstance("Rijndael/ECB/PKCS5Padding");

        // Cifrador en modo DESCIFRADO o DESENCRIPTACIÓN
        cifrador.init(Cipher.DECRYPT_MODE, key);

        String msj = "Descifrado con Rijndael el fichero: " + file1
                + ", y creado " + file2 + ".descifrado";
        System.out.println(msj);
        logger.log(Level.INFO, msj);

        fe = new FileInputStream(file1);
        fs = new FileOutputStream(file2 + ".descifrado");
        byte[] bufferClaro;
        byte[] buffer = new byte[1000];
        bytesLeidos = fe.read(buffer, 0, 1000);

        while (bytesLeidos != -1) {
            bufferClaro = cifrador.update(buffer, 0, bytesLeidos);
            fs.write(bufferClaro);
            bytesLeidos = fe.read(buffer, 0, 1000);
        }

        //Completa el descifrado
        bufferClaro = cifrador.doFinal();
        fs.write(bufferClaro);

        fe.close();
        fs.close();
    }

    /**
     * Método para mostrar el contenido del fichero en pantalla
     *
     * @param fichero: String con el nombre del fichero a visualizar
     * @return
     * @exception FileNotFoundException, IOException
     */
    private void Mostrar(String fichero) {
        texto.setText("");
        texto.setWrapStyleWord(true);
        texto.setLineWrap(true);

        File file = new File(ruta + fichero);
        if (file.exists()) {
            FileReader f = null;
            try {
                String cadena;
                f = new FileReader(file);
                try (BufferedReader b = new BufferedReader(f)) {
                    while ((cadena = b.readLine()) != null) {
                        texto.append(cadena);
                    }
                }
                texto.setCaretPosition(0);
                JOptionPane.showMessageDialog(null, new JScrollPane(texto,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "Contenido del fichero", JOptionPane.OK_OPTION);
                logger.log(Level.INFO, "Muestra el fichero: " + file.toString());
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } finally {
                try {
                    f.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error: ", ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "El fichero " + fichero + " no existe", "Error", JOptionPane.WARNING_MESSAGE);
            logger.log(Level.WARNING, "El fichero " + fichero + " no existe");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        EtiquetaTitulo = new javax.swing.JLabel();
        EtiquetaUsuario = new javax.swing.JLabel();
        TextoUsuario = new javax.swing.JTextField();
        BotonConectar = new javax.swing.JButton();
        BotonMostrar = new javax.swing.JButton();
        ListadoFicheros = new javax.swing.JComboBox<>();
        EtiquetaLista = new javax.swing.JLabel();
        TextoPass = new javax.swing.JTextField();
        EtiquetaUsuario1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TextoContenido = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        EtiquetaTitulo.setFont(new java.awt.Font("Calibri", 1, 20)); // NOI18N
        EtiquetaTitulo.setForeground(new java.awt.Color(51, 51, 51));
        EtiquetaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        EtiquetaTitulo.setText("PROGRAMA DE ENCRIPTADO DE FICHEROS");
        EtiquetaTitulo.setToolTipText("");
        EtiquetaTitulo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        EtiquetaTitulo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        EtiquetaUsuario.setText("Usuario:");

        TextoUsuario.setToolTipText(" el usuario tiene 8 caracteres en minúscula");

        BotonConectar.setText("Guardar");
        BotonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BotonConectarActionPerformed(evt);
            }
        });

        BotonMostrar.setText("Mostrar");
        BotonMostrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BotonMostrarActionPerformed(evt);
            }
        });

        EtiquetaLista.setText("Seleccione el fichero de la lista");

        TextoPass.setToolTipText(" el usuario tiene 8 caracteres en minúscula");

        EtiquetaUsuario1.setText("Contraseña:");

        jLabel1.setText("Contenido:");

        TextoContenido.setColumns(20);
        TextoContenido.setRows(5);
        jScrollPane1.setViewportView(TextoContenido);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(EtiquetaTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(EtiquetaUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(TextoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(EtiquetaUsuario1)
                        .addGap(12, 12, 12)
                        .addComponent(TextoPass, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(BotonConectar, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(EtiquetaLista, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ListadoFicheros, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(BotonMostrar, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(EtiquetaTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(EtiquetaUsuario))
                            .addComponent(TextoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(EtiquetaUsuario1))
                            .addComponent(TextoPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(BotonConectar))
                        .addGap(25, 25, 25)
                        .addComponent(EtiquetaLista)
                        .addGap(9, 9, 9)
                        .addComponent(ListadoFicheros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(BotonMostrar))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Método que recoge la acción de pulsar el botón Conectar
     *
     * @param ActionEvent
     * @return
     * @exception
     */
    private void BotonConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BotonConectarActionPerformed
        if (TextoUsuario.getText().equals("") || TextoPass.getText().equals("") || TextoContenido.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "No puede haber campos vacíos", "Error", JOptionPane.WARNING_MESSAGE);
            logger.log(Level.WARNING, "Contenido incompleto");
        } else {
            usuario = TextoUsuario.getText();
            contraseña = TextoPass.getText();
            contenido = TextoContenido.getText();
            Escribir(contenido);
            try {
                // Declara e incializa objeto tipo clave secreta
                // Llama al método que encripta el fichero que se pasa como parámetro
                clave2 = cifrarFichero(fichero, usuario, contraseña);
                descifrarFichero(ruta + fichero + ".cifrado", clave2, ruta + fichero);
            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } catch (IllegalBlockSizeException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } catch (BadPaddingException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.SEVERE, "Error: ", ex);
            }
            Listar();
        }

    }//GEN-LAST:event_BotonConectarActionPerformed

    /**
     * Método que recoge la acción de pulsar el botón Mostrar
     *
     * @param ActionEvent. Ítem seleccionado del listado de ficheros
     * @return
     * @exception
     */
    private void BotonMostrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BotonMostrarActionPerformed
        String fichero = (String) ListadoFicheros.getSelectedItem();
        Mostrar(fichero);
    }//GEN-LAST:event_BotonMostrarActionPerformed

    /**
     * Método principal
     *
     * @param args
     *
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            logger.log(Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BotonConectar;
    private javax.swing.JButton BotonMostrar;
    private javax.swing.JLabel EtiquetaLista;
    private javax.swing.JLabel EtiquetaTitulo;
    private javax.swing.JLabel EtiquetaUsuario;
    private javax.swing.JLabel EtiquetaUsuario1;
    private javax.swing.JComboBox<String> ListadoFicheros;
    private javax.swing.JTextArea TextoContenido;
    private javax.swing.JTextField TextoPass;
    private javax.swing.JTextField TextoUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
