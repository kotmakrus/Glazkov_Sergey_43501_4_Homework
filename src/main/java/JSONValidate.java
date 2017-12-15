/**
 * Created by sergeyglazkov on 16.12.17.
 */
import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

public class GetJsonFormat implements ServerClean {

    @NotNull
    private final HttpServer server;
    @NotNull
    private final Gson jsonBuilder;

    private static final String ROOT = "/";
    private static final int PORT = 80;
    private static final int CORRECT = 200;

    PrintWriter pr = new PrintWriter(new OutputStreamWriter(System.out));

    public GetJsonFormat() throws IOException { // check json method

        this.jsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.server.createContext(ROOT, http -> {

            InputStreamReader isr = new InputStreamReader(http.getRequestBody());
            String jsonRequest = new BufferedReader(isr).lines().collect(Collectors.joining());
            pr.println("Request:" + jsonRequest);
            pr.flush();

            String jsonResponse;
            try {

                Object object = builder.fromJson(jsonRequest, Object.class);
                jsonResponse = builder.toJson(object);
            } catch (JsonSyntaxException e) {

                JsonObject jsonError = new JsonObject();
                jsonError.addProperty("message", e.getMessage());
                jsonResponse = builder.toJson(jsonError);
            }

            pr.println("Response:" + jsonResponse);
            pr.flush();

            http.sendResponseHeaders(CORRECT, jsonResponse.length());
            http.getResponseBody().write(jsonResponse.getBytes());
            http.close();
        });
    }

    public static void main(String[] args) throws IOException { // start server
        GetJsonFormat jsonFormat = new GetJsonFormat();
        jsonFormat.start();
        Runtime.getRuntime().addShutdownHook(new Thread(jsonFormat::stop));
    }

    @Override
    public void start() {   // binding to http port, now we are listening
        this.server.start();
    }

    @Override
    public void stop() {   // it's enough to listen
        this.server.stop(0);
    }
}
