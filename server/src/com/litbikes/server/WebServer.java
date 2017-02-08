package com.litbikes.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;



public class WebServer {
	private static Logger LOG = Log.getLogger(WebServer.class);
	
	public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);

        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        context.setResourceBase("./web");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(new ResourceHandler());
        
        server.setHandler(context);
        server.start();
        LOG.info("Webserver started");
        
        server.join();
    }
	
}
