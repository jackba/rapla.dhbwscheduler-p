package org.rapla.plugin.dhbwscheduler.server.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.ServletTestBase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.rest.client.HTTPConnector;
import org.rapla.server.ServerServiceContainer;
import org.rapla.servletpages.RaplaPageGenerator;

public class SimplePageGeneratorTest extends ServletTestBase {
    
    public SimplePageGeneratorTest(String name) {
        super(name);
    }

    final static String paramName = "param";

    protected void setUp() throws Exception {
        WAR_SRC_FOLDER_NAME = "../rapla/war";
        super.setUp();
    }

    public void testGenerator() throws Exception
    {
        ServerServiceContainer raplaContainer = getContainer().getContext().lookup(ServerServiceContainer.class);
        raplaContainer.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
        String pagename = "testpage";
        raplaContainer.addWebpage(pagename, TestPage.class);
        String body = "";
        String authenticationToken = null;
        String paramValue = "world";
        URL url = new URL( "http://localhost:"+port+ "/rapla/" + pagename + "?"+ paramName +"="+paramValue);
        String result = new HTTPConnector().sendCallWithString("GET", url , body, authenticationToken);
        assertEquals("Hello world",result);
    }
    
    public static class TestPage implements RaplaPageGenerator
    {
        @Override
        public void generatePage(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String parameter = request.getParameter(paramName);
            PrintWriter writer = response.getWriter();
            try
            {
                writer.print("Hello " + parameter);
            }
            finally
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
        }
        
    }

}
