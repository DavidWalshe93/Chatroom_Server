/**
 * Author: David Walshe
 * Date: 20/10/2017
 * Decription:
 *     This code sets up a MultiThreaded server to allow various clients to connect in and communicate
 *     with a chatroom service.
 */

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Server extends MultithreadedServer {

    //Thread safe Objects and Variables
    private DataContainer dc;
    AtomicBoolean updateClients;

    //Server Objects
    private ServerSocket server;
    private Socket connection;

    //Logger Objects
    private static final String CLASS_NAME = Server.class.getName();
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private FileHandler fileHandler;
    private ConsoleHandler cs;

    //Constructor
    public Server() {
        super(8112);
        this.addFileHandler(logger);
        logger.log(Level.INFO, "");
        updateClients = new AtomicBoolean(false);       //Used to alert all Output threads of newly available content for their respective client sockets.
        dc = new DataContainer(20);                           //Set up a data container to hold the conversation history, limited to 20 lines
    }

    /**
     * Used to parse the filehandler for the log output from XML to human readable layout
     */
    public void parseLog() {
        Process proc;
        try{
            String srcPath = System.getProperty("user.dir");
            //Get the source path of the raw log file
            while(srcPath.contains("\\")) {
                srcPath = srcPath.replace("\\", "/");
            }
            srcPath += "/Server.log";
            //Get the destination path of the parsed log file
            String desPath = System.getProperty("user.dir");
            while(desPath.contains("\\")) {
                desPath = desPath.replace("\\", "/");
            }
            desPath += "/Server_Parsed.log";
            logger.log(Level.INFO, "Executing Perl Formatting Script on GUI Exit");
            //Call Perl Process to do the text conversion
            proc = Runtime.getRuntime().exec("Perl C:\\Users\\David\\Desktop\\Client-Server\\Server\\src\\FormatLoggerFileHandler.pl " + srcPath + " " + desPath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Execption: " + e.getMessage());
        }
    }

    /**
     * This method is used to set up a filehandler for the logger object created within this program.
     *
     */
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

    /**
     * This is a static utility method to be used to give formatted time information out.
     *
     */
    private static String timeFormatter(long milliseconds, String format) {

        //logger.entering(CLASS_NAME, "timeFormatter");
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);  //For some unknown reason 1 hour difference being exhibited from actual time.
        milliseconds %= 1000;
        StringBuilder timeFormattedString = new StringBuilder();
        format = format.toUpperCase();
        switch (format) {
            case "H,M,S,U":
                timeFormattedString.append(String.format("%02d", hours));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", minutes));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", seconds));
                timeFormattedString.append(".");
                timeFormattedString.append(String.format("%03d", milliseconds));
                break;
            case "H,M,S":
                timeFormattedString.append(String.format("%02d", hours));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", minutes));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", seconds));
                break;
            case "H,M":
                timeFormattedString.append(String.format("%02d", hours));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", minutes));
                break;
            case "H":
                timeFormattedString.append(String.format("%02d", hours));
                break;
            case "M":
                timeFormattedString.append(String.format("%02d", minutes));
                break;
            case "S":
                timeFormattedString.append(String.format("%02d", seconds));
                break;
            case "U":
                timeFormattedString.append(String.format("%03d", milliseconds));
                break;
            default:
                timeFormattedString.append(String.format("%02d", hours));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", minutes));
                timeFormattedString.append(":");
                timeFormattedString.append(String.format("%02d", seconds));
                break;
        }
        //logger.exiting(CLASS_NAME, "timeFormatter");
        //logger.exiting(CLASS_NAME, "timeFormatter");
        return timeFormattedString.toString();
    }

    /**
     * This method is used to set up the server and begin listening for client socket connections.
     */
    public void runServer() {
        try {
            while (true) {
                try {
                    listen();
                } catch (Exception e) {
                    System.err.println("Server terminated connection");
                } finally {
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    /**
     * This method is used to handle all incoming connections that are accepted by the listen method in
     * the listen method within the MultithreadedServer Class. It sets up two threads, one for input from
     * a client socket and the other is used to set up an output connection back to the client.
     */
    @Override
    public void handleConnection(Socket s, long id) {
        logger.entering(CLASS_NAME, "handleConnection - Thread: " + id);
        Socket connection = s;
        long threadId = id;
        AtomicBoolean[] alive = new AtomicBoolean[1];
        alive[0] = new AtomicBoolean();
        alive[0].set(true);

        long threadStartTime = System.currentTimeMillis();

        try(        BufferedReader in = SocketUtils.getReader(connection);
                    PrintWriter out = SocketUtils.getWriter(connection)
        ) {
            logger.log(Level.INFO, "Got I/O Streams from Client");
            processInputConnection(threadId, in, alive);
            processOutputConnection(threadId, out, alive);
            while (alive[0].get()) {
                Thread.sleep(10000);
                long threadCurrentTime = System.currentTimeMillis();
                long threadRunTime = threadCurrentTime - threadStartTime;
                System.out.println(id + " -- Connection state: " + alive[0].get() + "\n\r" + "Connection Time: " + timeFormatter(threadRunTime, "H,M,S"));
            }
            logger.log(Level.INFO, "Client Thread " + id + " Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.exiting(CLASS_NAME, "handleConnection - Thread: " + id);
    }

    /**
     * This method is used to kick of an input connection from a client socket. It reads in all text data
     * on the socket connection and adds it to a DataContainer to be stored and distributed to other clients
     */
    private void processInputConnection(long id, BufferedReader in, AtomicBoolean[] alive) {
        logger.entering(CLASS_NAME, "processInputConnection - Thread: " + id);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dc.add("CLIENT_" + id + "@[" + timeFormatter(System.currentTimeMillis(), "H,M") + "] Joined the Chat !\r\n");
                    updateClients.set(true);
                    try {
                        String input = "";
                        while ((input = in.readLine()) != null) {
                            if (input.contains("exit_chat")) {
                                break;
                            } else {
                                dc.add("CLIENT_" + id + "@[" + timeFormatter(System.currentTimeMillis(), "H,M") + "] >>> " + input + "\r\n");
                                dc.boundSize();
                                updateClients.set(true);
                            }
                        }
                        alive[0].set(false);
                    } catch (Exception e) {
                        alive[0].set(false);
                        e.printStackTrace();
                    }
                    dc.add("\r\nCLIENT_" + id + "@[" + timeFormatter(System.currentTimeMillis(), "H,M") + "] Left the Chat !\r\n");
                    updateClients.set(true);
                    System.out.println("Thread " + id + " Reader Closed");
                }
            }).start();
        } catch (Exception e) {
            alive[0].set(false);
            e.printStackTrace();
        }
        logger.exiting(CLASS_NAME, "processInputConnection - Thread: " + id);
    }

    /**
     * This method is used to kick of an output connection to a client socket. It prints out all text data
     * within a data container object to allow for
     */
    private void processOutputConnection(long id, PrintWriter pr, AtomicBoolean[] alive) {
        logger.entering(CLASS_NAME, "processOutputConnection - Thread: " + id);
        PrintWriter out = pr;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.entering(CLASS_NAME, "processingOutputThread->Runnable - Thread: " + id);
                    while (alive[0].get()) {
                        if (updateClients.get()) {
                            out.println(dc.toString() + "\r\n<EOT>\r\n");
                            Thread.sleep(20);
                            updateClients.set(false);
                        }
                        Thread.sleep(10);
                        if (out.checkError()) {
                            System.out.println("Error: OutputStream Killed");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.log(Level.INFO, "Thread " + id + " Writer Closed");
                System.out.println("Thread " + id + " Writer Closed");
                logger.exiting(CLASS_NAME, "processingOutputThread->Runnable - Thread: " + id);
            }
        }).start();
        logger.exiting(CLASS_NAME, "processOutputConnection - Thread: " + id);
    }
}
