package com.litbikes.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class WebServer {
	
	private static Logger LOG = Log.getLogger(WebServer.class);
    private Server server;
    
    public WebServer(int port)
    {
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        context.setResourceBase("./web");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(new ResourceHandler());

        server = new Server(port);
        server.setHandler(context);
    }
	
	public void start() {
        try {
			server.start();
	        LOG.info("Webserver started");
	        server.join();
		} catch (Exception e) {
			LOG.warn("Webserver failed to start!");
			e.printStackTrace();
		}
	}
	
}
