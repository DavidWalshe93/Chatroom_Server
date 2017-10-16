import java.net.*;
import java.io.*;

/** A shorthand way to create BufferedReaders and
 *  PrintWriters associated with a Socket.
 *  <p>
 *  From <a href="http://courses.coreservlets.com/Course-Materials/">the
 *  coreservlets.com tutorials on JSF 2, PrimeFaces, Ajax, JavaScript, jQuery, GWT, Android,
 *  Spring, Hibernate, JPA, RESTful Web Services, Hadoop, Spring MVC,
 *  servlets, JSP, Java 8 lambdas and streams (for those that know Java already),
 *  and Java 8 programming (for those new to Java)</a>.
 */

public class SocketUtils {
    /** Make a BufferedReader to get incoming data. */

    public static BufferedReader getReader(Socket s)
            throws IOException {
        return(new BufferedReader(
                new InputStreamReader(s.getInputStream())));
    }

    /** Make a PrintWriter to send outgoing data.
     *  This PrintWriter will automatically flush stream
     *  when println is called.
     */

    public static PrintWriter getWriter(Socket s)
            throws IOException {
        // Second argument of true means autoflush.
        return(new PrintWriter(s.getOutputStream(), true));
    }

    private SocketUtils() {} // Uninstantiatable class: static methods only
}