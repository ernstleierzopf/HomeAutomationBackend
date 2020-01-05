package homeautomation.backend.rest;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import homeautomation.backend.entity.PlugSocket;

@Stateless
@Path("secure/plugSocket")
public class PlugSocketResource {
	
	@Context
	UriInfo uri;

	private EntityManager em = null;

	/**
	 * Initiates the EntityManagerFactory emf and the EntityManager em
	 */
	@PostConstruct
	public void init() {
		if (AuthenticationService.EMF == null)
			AuthenticationService.EMF = Persistence.createEntityManagerFactory("HomeAutomationPU");
		if (em == null)
			em = AuthenticationService.EMF.createEntityManager();
	}
	
	/**
	 * returns all PlugSockets <br>
	 * 200: OK, PlugSockets are in the body of the Response. <br>
	 * 404: There are no PlugSockets in the database. <br>
	 * 500: The JSONArray could not be built.
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPlugSockets() {
		TypedQuery<PlugSocket> query = em.createQuery(
				"SELECT u FROM PlugSocket u", PlugSocket.class);
		List<PlugSocket> plugSockets = query.getResultList();
		if (plugSockets.size() > 0) {
			try {
				JSONArray jsonArray = PlugSocket.ConvertToJsonArray(plugSockets);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}
	
	/**
	 * returns a PlugSocket <br>
	 * 200: OK, PlugSocket is in the body of the Response. <br>
	 * 404: The PlugSockets with the plugNumber does not exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param plugNumber the primary key of PlugSocket
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/plugNumber/{plugNumber}")
	public Response getPlugSocketByNumber(@PathParam("plugNumber") int plugNumber) {
		TypedQuery<PlugSocket> query = em.createQuery("SELECT u FROM PlugSocket u where u.plugNumber = :plugNumber",
				PlugSocket.class);
		query.setParameter("plugNumber", plugNumber);
		List<PlugSocket> plugSockets = query.getResultList();
		if (plugSockets.size() > 0) {
			try {
				JSONObject jsonObject = PlugSocket.ConvertToJsonObject(plugSockets.get(0));
				return Response.ok().entity(jsonObject.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}
	
	/**
	 * creates a new PlugSocket in the PlugSocket Table. Return Codes: <br>
	 * 201: Created, the PlugSocket was persisted in the Table.<br>
	 * 409: Conflict, the PlugSocket already exists in the Database
	 * 
	 * @param ps the Entity to be persisted
	 * @return HTTP Response with the path to the entity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createPlugSocket(PlugSocket ps) {
		try {
			if (!ps.getStatus())
				RpiDeviceState.run(ps.getPlugNumber());
			else
				RpiDeviceState.stop(ps.getPlugNumber());
			em.getTransaction().begin();
			em.persist(ps);
			em.getTransaction().commit();
			return Response.created(URI.create(uri.getAbsolutePath() + "/" + String.valueOf(ps.getPlugNumber()))).entity(ps)
					.build();
		} catch (RollbackException e) {
			return Response.status(409).entity("The PlugSocket could not be created!").build();
		}
	}
	
	/**
	 * updates a PlugSocket in the PlugSocket Table. Return Codes: <br>
	 * 201: Created, the PlugSocket did not exist in the table and was
	 * persisted. <br>
	 * 204: noContent, the PlugSocket was updated and the answer intentionally
	 * does not return anything.
	 * 
	 * @param plugSocket the entity to be updated
	 * @return HTTP Response
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePlugSocket(PlugSocket plugSocket) {
		PlugSocket ps = em.find(PlugSocket.class, plugSocket.getPlugNumber());
		if(ps != null) {
			if (!ps.getStatus())
				RpiDeviceState.run(ps.getPlugNumber());
			else
				RpiDeviceState.stop(ps.getPlugNumber());
			em.getTransaction().begin();
			ps.setDevice(plugSocket.getDevice());
			ps.setStatus(plugSocket.getStatus());
			em.merge(ps);
			em.getTransaction().commit();
			return Response.noContent().build();
		}
		else {
			return createPlugSocket(plugSocket);
		}
	}
	
	/**
	 * Deletes PlugSocket with the plugNumber. Response Code: <br>
	 * 204: noContent, the answer intentionally does not return anything.
	 * 
	 * @param plugNumber PK from the PlugSocket
	 * @return HTTP Response
	 */
	@DELETE
	@Path("{plugNumber}")
	public Response deletePlugSocket(@PathParam("plugNumber") int plugNumber) {
		PlugSocket ps = em.find(PlugSocket.class, plugNumber);
		if (ps != null) {
			em.getTransaction().begin();
			em.remove(ps);
			em.getTransaction().commit();
		}
		return Response.noContent().build();
	}
}
