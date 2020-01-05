package homeautomation.backend.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.pi4j.util.ExecUtil;

@Path("information")
public class InformationResource {
	static String LIN_PROGRAM_PATH = "/opt/vc/bin/vcgencmd measure_temp";
	static String LIN_PUBLIC_KEY_PATH = "/home/pi/.ssh/pub.pkcs8";

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/hardware")
	public String getHardwareInfo() {
		try {
			StringBuilder sb = new StringBuilder();
			String result[] = ExecUtil.execute(LIN_PROGRAM_PATH);
			if (result != null && result.length > 0) {
				for (String line : result) {
					sb.append(line);
				}
				return sb.toString();
			}
		} catch (IOException | InterruptedException e) {
			return Response.status(500).build().toString();
		}
		return "";
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/PublicKey")
	public String getPublicKey() {
		try {
			StringBuilder sb = new StringBuilder();
			List<String> lines = Files.readAllLines(Paths.get(LIN_PUBLIC_KEY_PATH));
			for (String line : lines) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		} catch (IOException e) {
			//return Response.status(500).build().toString();
			return "An Error occured: " + e.getMessage();
		}
	}
}
