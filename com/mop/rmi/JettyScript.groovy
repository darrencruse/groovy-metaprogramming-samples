package com.mop.rmi
import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.*
import groovy.servlet.*

@Grab(group = 'org.mortbay.jetty', module = 'jetty-embedded', version = '6.1.0')
def runServer(duration) {
    def server = new Server(8080)
    def context = new Context(server, "/", Context.SESSIONS);
    context.resourceBase = "."
    context.addServlet(TemplateServlet, "*.gsp")
    server.start()
    sleep duration
    server.stop()
}

runServer(10000)
