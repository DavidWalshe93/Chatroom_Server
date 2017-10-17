//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Server extends MultithreadedServer { // extends MultithreadedServer {
    JTextField enterField;
    JTextArea txtArea;

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    //Create all GUI element reference holders
    //JTextArea txtArea;
//    JButton loadBtn;
//    JButton cancelBtn;
//    JProgressBar progressBar;
//    JLabel timeStamp;
//    JButton zipBtn;
//    JFileChooser fileChooser;
    private int counter = 1;

    private static final String CLASS_NAME = Server.class.getName();

    //Create Swing Workers to process asynchronous blocking requests in the GUI.
    public SwingWorker<Integer, String> worker;
    public SwingWorker<Integer, String> workerZip;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private FileHandler fileHandler;
    private ConsoleHandler cs;

    public Server()  {
        super(8777);
        this.addFileHandler(logger);
        logger.log(Level.INFO, "");
    }

    public static void main(String[] var0) {
        Server s = new Server();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                s.createAndShowGUI(s);
            }
        });
        s.runServer();
    }

    private void addFileHandler(Logger logger) {
        try {
            fileHandler = new FileHandler(CLASS_NAME + ".log");

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Execption: " + ex.getMessage());
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, "Execption: " + ex.getMessage());
        }
        logger.addHandler(fileHandler);
        cs = new ConsoleHandler();
        logger.addHandler(cs);
        logger.setLevel(Level.FINER);
        fileHandler.setLevel(Level.ALL);
        cs.setLevel(Level.ALL);
    }

    //GUI maker method called by main.
    public static void createAndShowGUI(Server s) {
        logger.entering(CLASS_NAME, "createAndShowGUI");

        logger.log(Level.INFO, "Creating GUI");
        JFrame frame = new JFrame("Thread Safe GUI");
        frame.setSize(600, 600);
        frame.setLocation(600, 300);

        Server demo = s;

        frame.setContentPane(demo.createContentPane());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        logger.exiting(CLASS_NAME, "createAndShowGUI");
    }

    //Create the GUI look and feel. Init and setup up all components in the GUI.
    private JPanel createContentPane() {
        logger.entering(CLASS_NAME, "createContentPane");
        JPanel totalGUI = new JPanel();
        totalGUI.setSize(600, 600);
        totalGUI.setLayout(new MigLayout("", "", ""));

        this.enterField = new JTextField();
        this.enterField.setEditable(false);
        this.enterField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent var1) {
                //sendData(var1.getActionCommand());
                enterField.setText("");
            }
        });

        //Instantiate the TextArea to suit the GUI
        txtArea = new JTextArea("", 5, 30);
        txtArea.setEditable(false);
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) txtArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);


        // Create the ScrollPane and instantiate it with the TextArea as an argument
        // along with two constants that define the behaviour of the scrollbars.
        JScrollPane area = new JScrollPane(txtArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);



        //Where the GUI is constructed:
        totalGUI.add(enterField, "span, grow");
        totalGUI.add(area, "span, push, grow");
//        totalGUI.add(timeStamp, "span, grow");
//        totalGUI.add(progressBar, "span, grow");
//        totalGUI.add(loadBtn, "span, grow, split");
//        totalGUI.add(zipBtn, "span, grow, split");
//        totalGUI.add(cancelBtn, "span, grow, split");

        totalGUI.setOpaque(true);
        logger.exiting(CLASS_NAME, "createContentPane");
        return totalGUI;
    }


    //Static method to return a structured time format to the GUI.
    private static String timeFormatter(long milliseconds) {
        logger.entering(CLASS_NAME, "timeFormatter");
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        milliseconds %= 1000;
        String timeFormattedString = "";
        timeFormattedString = String.format("%02d", hours);
        timeFormattedString += ":";
        timeFormattedString += String.format("%02d", minutes);
        timeFormattedString += ":";
        timeFormattedString += String.format("%02d", seconds);
        timeFormattedString += ".";
        timeFormattedString += String.format("%03d", milliseconds);
        logger.exiting(CLASS_NAME, "timeFormatter");
        logger.exiting(CLASS_NAME, "timeFormatter");
        return timeFormattedString;
    }

    public void runServer() {
        try {
            this.server = new ServerSocket(8777, 100);

            while(true) {
                while(true) {
                    try {
                        waitForConnection();
                        getStreams();
                        processConnection();
                    } catch (Exception e) {
                        System.err.println("Server terminated connection");
                    } finally {
                        this.closeConnection();
                        ++this.counter;
                    }
                }
            }
        } catch (IOException var9) {
            var9.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        this.displayMessage("Waiting for connection\n");
        this.connection = this.server.accept();
        this.displayMessage("Connection " + this.counter + " received from: " + this.connection.getInetAddress().getHostName());
    }

    private void getStreams() throws IOException {
        this.output = new ObjectOutputStream(this.connection.getOutputStream());
        this.output.flush();
        this.input = new ObjectInputStream(this.connection.getInputStream());
        this.displayMessage("\nGot I/O streams\n");
    }

    private void processConnection() throws IOException {
        String var1 = "Connection successful";
        this.sendData(var1);
        this.setTextFieldEditable(true);

        do {
            try {
                var1 = (String)this.input.readObject();
                this.displayMessage("\n" + var1);
            } catch (ClassNotFoundException var3) {
                this.displayMessage("\nUnknown object type received");
            }
        } while(!var1.equals("CLIENT>>> TERMINATE"));

    }

    private void closeConnection() {
        this.displayMessage("\nTerminating connection\n");
        this.setTextFieldEditable(false);

        try {
            this.input.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private void sendData(String var1) {
        try {
            this.output.writeObject("SERVER>>> " + var1);
            this.output.flush();
            this.displayMessage("\nSERVER>>> " + var1);
        } catch (IOException var3) {
            this.txtArea.append("\nError writing object");
        }
    }

    private void displayMessage(final String var1) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Server.this.txtArea.append(var1);
                Server.this.txtArea.setCaretPosition(Server.this.txtArea.getText().length());
            }
        });
    }

    private void setTextFieldEditable(final boolean var1) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Server.this.enterField.setEditable(var1);
            }
        });
    }
    @Override
    public void handleConnection(Socket s)
    {

    }
}
