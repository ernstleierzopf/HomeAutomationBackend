package homeautomation.backend.rest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

@SuppressWarnings("serial")
public class StartupServlet extends HttpServlet
{
 
    public void init() throws ServletException
    {
    	if(!System.getProperty("os.name").toLowerCase().contains("win")) {
            RpiDeviceState.init();
            Thread dhtThread = new Thread(new SensorThread());
            dhtThread.start();
    	}
    }
}