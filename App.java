package ca.utoronto.utm.mcs;

import java.net.URI;
import java.net.URISyntaxException;
import com.sun.net.httpserver.HttpServer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import javax.inject.Inject;
import dagger.ObjectGraph;

public class App implements Runnable
{
    @Inject HttpServer server;
    @Inject Config config;
    @Inject Post post;

    public void run()
    {
        /* TODO: Add Working Context Here */
        server.setExecutor(null);
        server.createContext("/api/v1/post", post);
        server.start();
        System.out.printf("Server started on port %d...\n", config.port);
    }

    public static void main(String[] args) throws URISyntaxException
    {
        ObjectGraph objectGraph = ObjectGraph.create(new DaggerModule(new Config()));
        App app = objectGraph.get(App.class);
        app.run();
    }
}
