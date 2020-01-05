package homeautomation.backend.rest;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import homeautomation.backend.entity.PlugSocket;

public class RpiDeviceState {
	
	private static boolean initialized = false;
	private static EntityManager em = null;
	private static GpioController gpio;
	public static GpioPinDigitalOutput outPin1;
	public static GpioPinDigitalOutput outPin2;
	public static GpioPinDigitalOutput outPin3;
	public static GpioPinDigitalOutput outPin4;
	
	@PostConstruct
	protected static void init() {
		if(initialized)
			return;
		gpio = GpioFactory.getInstance();
		outPin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26);
		outPin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27);
		outPin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28);
		outPin4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29);
		
		if (em == null)
			em = AuthenticationService.EMF.createEntityManager();

		PlugSocket ps1 = em.find(PlugSocket.class, 1);
		if(ps1 != null)
			outPin1.setState(!ps1.getStatus());
		PlugSocket ps2 = em.find(PlugSocket.class, 2);
		if(ps2 != null)
			outPin2.setState(!ps2.getStatus());
		PlugSocket ps3 = em.find(PlugSocket.class, 3);
		if(ps3 != null)
			outPin3.setState(!ps3.getStatus());
		PlugSocket ps4 = em.find(PlugSocket.class, 4);
		if(ps4 != null)
			outPin4.setState(!ps4.getStatus());
		initialized = true;
	}
	
	public static void handleEvent(GpioPinDigitalStateChangeEvent event, int pinNr, GpioPinDigitalOutput outPin) {
		PlugSocket ps = em.find(PlugSocket.class, pinNr);
        if (event.getState() == PinState.HIGH) {
			em.getTransaction().begin();
            if(outPin.isHigh()) {
    			stop(pinNr);
        		if(ps != null) {
        			ps.setStatus(false);
	    			em.merge(ps);
        		}
        		else {
        			ps = new PlugSocket(pinNr, false, "");
        			em.persist(ps);
        		}
            }
            else {
            	run(pinNr);
        		if(ps != null) {
        			ps.setStatus(true);
	    			em.merge(ps);
        		}
        		else {
        			ps = new PlugSocket(pinNr, true, "");
        			em.persist(ps);
        		}
            }
			em.getTransaction().commit();
		}
	}
	 
	public static void run(int portNumber) {
		if(outPin1 == null)
			init();
		switch (portNumber) {
		case 1:
			outPin1.low();;
			break;
		case 2:
			outPin2.low();
			break;
		case 3:
			outPin3.low();
			break;
		case 4:
			outPin4.low();
			break;
		}
	}
	
	public static void stop(int portNumber) {
		if(outPin1 == null)
			init();
		switch (portNumber) {
		case 1:
			outPin1.high();
			break;
		case 2:
			outPin2.high();
			break;
		case 3:
			outPin3.high();
			break;
		case 4:
			outPin4.high();
			break;
		}
	}

}
