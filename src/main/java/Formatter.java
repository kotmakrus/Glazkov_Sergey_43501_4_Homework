import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;


public class Formatter implements Server {

    @NotNull
    private final HttpServer server;
    @NotNull
    private final Gson builder;

    private static final int PORT = 80;
    private static final int CORRECT = 200;
    private static final String ROOT = "/";
    
    PrintWriter out = new PrintWriter(System.out);

    public Formatter() throws IOException {  // check json files
        
        this.builder = new GsonBuilder().setPrettyPrinting().create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.server.createContext(ROOT, http -> {
            
            int idForRequest = 0;
            InputStreamReader isr = new InputStreamReader(http.getRequestBody());
            final String jsonRequest = new BufferedReader(isr).lines().collect(Collectors.joining());
            
            System.out.println("request:" + jsonRequest);
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
                                idForRequest
                        ));
            } finally {
                //noinspection UnusedAssignment
                idForRequest ++;
            }
           
            
            out.println("response:" + jsonResponse);
            out.flush();
            
            http.sendResponseHeaders(CORRECT, jsonResponse.length());
            http.getResponseBody().write(jsonResponse.getBytes());
            http.close();
        });
    }

    public static void main(String[] args) throws IOException {  // start server and waiting for json files
        
        Formatter formatter = new Formatter();
        formatter.start();
        Runtime.getRuntime().addShutdownHook(new Thread(formatter::stop));
    }

    @Override
    public void start() {
        this.server.start(); // bind server to http port, start listening
    }
    
    @Override
    public void stop() {
        this.server.stop(0); // stop listening
    }
}
