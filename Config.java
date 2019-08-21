package ca.utoronto.utm.mcs;

import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;

@Singleton
class Config 
{
    @Inject Config() {}

    String ip = "0.0.0.0";
    int port = 8080;
}
