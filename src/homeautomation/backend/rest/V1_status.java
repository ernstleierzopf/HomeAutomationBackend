package homeautomation.backend.rest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v1/status")
public class V1_status {

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String returnTitle(){
		LocalDateTime now = LocalDateTime.now();
		Timestamp t = Timestamp.valueOf(now);
		
		return "time (" + ZoneId.systemDefault() + ") = " + t + " " + new Timestamp(System.currentTimeMillis()); 
	}
}
