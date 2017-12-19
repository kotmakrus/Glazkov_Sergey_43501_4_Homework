import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

/**
 * Class implements Server interface
 * <p>
 * Task of class is checking valid Json file or not
 */
public class Jsonvalidate implements Server {

    @NotNull
    private final HttpServer server;
    @NotNull
    private final Gson builder;

    private static final int PORT = 80;
    private static final int CORRECT = 200;
    private static final String ROOT = "/";
    
    PrintWriter out = new PrintWriter(System.out);

    /**
     * Checking Json file
     *
     * @throws IOException because method create() of HttpServer can throw IOException
     */
    public Jsonvalidate() throws IOException {
        
        this.builder = new GsonBuilder().setPrettyPrinting().create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.server.createContext(ROOT, http -> {
            
            int request_id = 0;
            InputStreamReader isr = new InputStreamReader(http.getRequestBody());
            final String jsonRequest = new BufferedReader(isr).lines().collect(Collectors.joining());
            
            out.println("request:" + jsonRequest);
            out.flush();
            String jsonResponse;
            
            try {
                Object object = builder.fromJson(jsonRequest, Object.class);
                jsonResponse = builder.toJson(object);
            } catch (JsonSyntaxException e) {
                String[] errorSplittedString = e.getMessage().split(".+: | at ");
                jsonResponse = builder.toJson(
                        new JsonError(
                                e.hashCode(),
                                errorSplittedString[1],
                                "at " + errorSplittedString[2],
                                jsonRequest,
                                request_id
                        ));
            } finally {
                
                request_id++;
            }
            
            out.println("response:" + jsonResponse);
            out.flush();
            
            http.sendResponseHeaders(CORRECT, jsonResponse.length());
            http.getResponseBody().write(jsonResponse.getBytes());
            http.close();
        });
    }

    public static void main(String[] args) throws IOException {
        
        Jsonvalidate jsonvalidate = new Jsonvalidate();
        f.start();
        Runtime.getRuntime().addShutdownHook(new Thread(jsonvalidate::stop));
    }

    @Override
    public void start() {
        
        this.server.start();  // bind server to http port, now we are listening
    }

    @Override
    public void stop() {
        
        this.server.stop(0);  // stop listen
    }
}
