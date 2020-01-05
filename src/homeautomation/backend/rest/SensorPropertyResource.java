package homeautomation.backend.rest;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import homeautomation.backend.entity.SensorProperty;

@Stateless
@Path("secure/sensorProperty")
public class SensorPropertyResource {

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
	 * returns all SensorProperties of a Sensor <br>
	 * 200: OK, SensorProperties are in the body of the Response. <br>
	 * 404: The Sensor or SensorProperties do not exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param sensorSerialNumber The serial number of the sensor to which the
	 *                           properties belong
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Path("{sensorSerialNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensorPropertiesFromSensor(@PathParam("sensorSerialNumber") long sensorSerialNumber) {
		TypedQuery<SensorProperty> query = em.createQuery(
				"SELECT u FROM SensorProperty u where u.sensor.serialNumber = :serialNumber", SensorProperty.class);
		query.setParameter("serialNumber", sensorSerialNumber);
		List<SensorProperty> sensorProperties = query.getResultList();
		if (sensorProperties.size() > 0) {
			try {
				JSONArray jsonArray = SensorProperty.ConvertToJsonArray(sensorProperties);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	/**
	 * returns all SensorProperties of a Sensor <br>
	 * 200: OK, SensorProperties are in the body of the Response. <br>
	 * 404: The Sensor or SensorProperties do not exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param sensorSerialNumber The serial number of the sensor to which the
	 *                           properties belong
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Path("/id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensorPropertyById(@PathParam("id") long id) {
		TypedQuery<SensorProperty> query = em.createQuery("SELECT u FROM SensorProperty u where u.id = :id",
				SensorProperty.class);
		query.setParameter("id", id);
		List<SensorProperty> sensorProperties = query.getResultList();
		if (sensorProperties.size() > 0) {
			try {
				JSONObject jsonObject = SensorProperty.ConvertToJsonObject(sensorProperties.get(0));
				return Response.ok().entity(jsonObject.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	/**
	 * creates a new SensorProperty in the SensorProperty Table. Return Codes: <br>
	 * 201: Created, the SensorProperty was persisted in the Table.<br>
	 * 409: Conflict, the SensorProperty already exists in the Database or the
	 * Sensor is not persistent in the database.
	 * 
	 * @param sp the Entity to be persisted
	 * @return HTTP Response with the path to the entity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSensorProperty(SensorProperty sp) {
		try {
			em.getTransaction().begin();
			em.persist(sp);
			em.getTransaction().commit();
			return Response.created(URI.create(uri.getAbsolutePath() + "/" + String.valueOf(sp.getId()))).entity(sp)
					.build();
		} catch (RollbackException e) {
			return Response.status(409).entity("The SensorProperty could not be created!").build();
		}
	}

	/**
	 * updates a SensorProperty in the SensorProperty Table. Return Codes: <br>
	 * 201: Created, the SensorProperty did not exist in the table and was
	 * persisted. <br>
	 * 204: noContent, the SensorProperty was updated and the answer intentionally
	 * does not return anything.
	 * 
	 * @param sensorProperty the entity to be updated
	 * @return HTTP Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSensorProperty(SensorProperty sensorProperty) {
		SensorProperty sp = em.find(SensorProperty.class, sensorProperty.getId());
		if(sp != null) {
			em.getTransaction().begin();
			sp.setDescription(sensorProperty.getDescription());
			sp.setSensor(sensorProperty.getSensor());
			sp.setValue(sensorProperty.getValue());
			em.merge(sp);
			em.getTransaction().commit();
			return Response.noContent().build();
		}
		else {
			return createSensorProperty(sensorProperty);
		}
	}

	/**
	 * Deletes SensorProperty with the id. Response Code: <br>
	 * 204: noContent, the answer intentionally does not return anything.
	 * 
	 * @param id PK from the SensorProperty
	 * @return HTTP Response
	 */
	@DELETE
	@Path("{id}")
	public Response deleteSensorProperty(@PathParam("id") long id) {
		SensorProperty sp = em.find(SensorProperty.class, id);
		if (sp != null) {
			em.getTransaction().begin();
			em.remove(sp);
			em.getTransaction().commit();
		}
		return Response.noContent().build();
	}
}
