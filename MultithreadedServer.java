import java.net.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A template for a multithreaded server. Subclass this and implement
 *  handleConnection. Then instantiate your subclass with a port number,
 *  and call "listen" on the instance.
 *  <p>
 *  From <a href="http://courses.coreservlets.com/Course-Materials/">the
 *  coreservlets.com tutorials on JSF 2, PrimeFaces, Ajax, jQuery, GWT, Android,
 *  Spring, Hibernate, JPA, RESTful Web Services, Hadoop,
 *  servlets, JSP, and Java 7 and Java 8 programming</a>.
 *  <p>
 *  Improved redesign of my earlier version based on suggestion by
 *  Doug Velazquez: Doug.Velazquez@cassidiancommunications.com
 */

public abstract class MultithreadedServer {
    private int port;
    private long timeoutCount;

    /** Build a server on specified port. The server will accept
     *  a connection and then pass the connection to a connection handler,
     *  then put the connection handler in a queue of tasks to
     *  be executed in separate threads. The class will continue to run until
     *  the server is killed (e.g., Control-C in the startup window)
     *  or System.exit() from elsewhere in the Java code)  .
     */

    public MultithreadedServer(int port) {
        this.port = port;
        this.timeoutCount = 0;
    }

    /** Gets port on which server is listening. */

    public int getPort() {
        return(port);
    }

    /** Monitor a port for connections. Each time one is
     *  established, pass resulting Socket to connection handler,
     *  which will execute it in background thread.
     */

    public void listen(Logger logger) {
        long threadId = 0;
        int poolSize = 5;
        ExecutorService tasks = Executors.newFixedThreadPool(poolSize);
        try(ServerSocket listener = new ServerSocket(port)) {
            listener.setSoTimeout(10000);
            Socket socket;
            while(true) {  // Run until killed
                socket = listener.accept();
                timeoutCount = 0;
                tasks.execute(new ConnectionHandler(socket, threadId));
                threadId++;
                if(threadId > (Long.SIZE-1))
                {
                    threadId = 0;
                }
            }
        } catch (SocketTimeoutException e)
        {
            timeoutCount++;
            logger.log(Level.INFO, "Timeouts Occured: " + timeoutCount);
            System.out.println("Timeouts Occured: " + timeoutCount);
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
        tasks.shutdown();
    }

    /** This is the method that provides the behavior to the
     *  server, since it determines what is done with the
     *  resulting socket. <b>Override this method in servers
     *  you write.</b>
     */

    protected abstract void handleConnection(Socket connection, long threadID)
            throws IOException;

    private class ConnectionHandler implements Runnable {
        private Socket connection;
        private long threadId;

        public ConnectionHandler(Socket socket, long id) {
            this.connection = socket;
            this.threadId = id;
        }

        @Override
        public void run() {
            try {
                handleConnection(connection, threadId);
            } catch(IOException ioe) {
                System.err.println("IOException: " + ioe);
            }
        }
    }
}
