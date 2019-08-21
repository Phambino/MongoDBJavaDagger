package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import javax.inject.Inject;
import dagger.Module;
import dagger.Provides;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;


@Module (injects = {App.class,Post.class}, library = true) //TODO: Add in any new classes here
class DaggerModule {
    Config config;

    DaggerModule(Config cfg) {
        config = cfg;
    }

    @Provides MongoClient provideMongoClient() {
        MongoClient mongoClient = MongoClients.create();
        return mongoClient;
    }

    @Provides HttpServer provideHttpServer() {
        /* TODO: Fill in this function */
        HttpServer server = null;
        try {
            /* TODO: Fill in this function */
            server = HttpServer.create(new InetSocketAddress(config.ip, config.port), 0);
        }catch(IOException e){
            System.exit(-1);
        }
        return server;
    }
}
