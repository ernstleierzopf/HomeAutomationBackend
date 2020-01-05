package homeautomation.backend.rest;

import java.net.URI;
import java.sql.Timestamp;
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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import homeautomation.backend.entity.SensorMeasurement;

@Stateless
@Path("secure/sensorMeasurement")
public class SensorMeasurementResource {
	
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
	 * returns all SensorMeasurements <br>
	 * 200: OK, SensorMeasurements are in the body of the Response. <br>
	 * 404: There are no Measurements in the database. <br>
	 * 500: The JSONArray could not be built.
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSensorMeasurements() {
		TypedQuery<SensorMeasurement> query = em.createQuery(
				"SELECT u FROM SensorMeasurement u", SensorMeasurement.class);
		List<SensorMeasurement> sensorMeasurements = query.getResultList();
		if (sensorMeasurements.size() > 0) {
			try {
				JSONArray jsonArray = SensorMeasurement.ConvertToJsonArray(sensorMeasurements);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}
	
	/**
	 * returns a SensorMeasurement <br>
	 * 200: OK, SensorMeasurement is in the body of the Response. <br>
	 * 404: The SensorMeasurement with the id does not exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param id the primary key of SensorMeasurement
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public Response getSensorMeasurementById(@PathParam("id") long id) {
		TypedQuery<SensorMeasurement> query = em.createQuery("SELECT u FROM SensorMeasurement u where u.id = :id",
				SensorMeasurement.class);
		query.setParameter("id", id);
		List<SensorMeasurement> sensorMeasurements = query.getResultList();
		if (sensorMeasurements.size() > 0) {
			try {
				JSONObject jsonObject = SensorMeasurement.ConvertToJsonObject(sensorMeasurements.get(0));
				return Response.ok().entity(jsonObject.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	/**
	 * returns all SensorMeasurements in the timespan <br>
	 * 200: OK, SensorMeasurements are in the body of the Response. <br>
	 * 404: There are no Measurements in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param timeSpanFrom the time in seconds from when the results should be returned
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/timeSpan/{timeSpanFrom}")
	public Response getSensorMeasurementsInTimeSpan(@PathParam("timeSpanFrom") long from) {
		TypedQuery<SensorMeasurement> query = em.createQuery(
				"SELECT u FROM SensorMeasurement u where u.time > :time", SensorMeasurement.class);
		query.setParameter("time", new Timestamp(from));
		List<SensorMeasurement> sensorMeasurements = query.getResultList();
		if (sensorMeasurements.size() > 0) {
			try {
				JSONArray jsonArray = SensorMeasurement.ConvertToJsonArray(sensorMeasurements);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}
	
	/**
	 * returns all SensorMeasurements from one Sensor <br>
	 * 200: OK, SensorMeasurements are in the body of the Response. <br>
	 * 404: There are no Measurements in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param sensorSerialNumber the serial number of the sensor
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/sensorSerialNumber/{sensorSerialNumber}")
	public Response getSensorMeasurementsFromSensor(@PathParam("sensorSerialNumber") long sensorSerialNumber) {
		TypedQuery<SensorMeasurement> query = em.createQuery(
				"SELECT u FROM SensorMeasurement u where u.sensor.serialNumber = :sensor", SensorMeasurement.class);
		query.setParameter("sensor", sensorSerialNumber);
		List<SensorMeasurement> sensorMeasurements = query.getResultList();
		if (sensorMeasurements.size() > 0) {
			try {
				JSONArray jsonArray = SensorMeasurement.ConvertToJsonArray(sensorMeasurements);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}
	
	/**
	 * creates a new SensorMeasurement in the SensorMeasurement Table. Return Codes: <br>
	 * 201: Created, the SensorMeasurement was persisted in the Table.<br>
	 * 409: Conflict, the SensorMeasurement already exists in the Database or the
	 * Sensor is not persistent in the database.
	 * 
	 * @param sm the Entity to be persisted
	 * @return HTTP Response with the path to the entity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSensorMeasurement(SensorMeasurement sm) {
		try {
			em.getTransaction().begin();
			em.persist(sm);
			em.getTransaction().commit();
			return Response.created(URI.create(uri.getAbsolutePath() + "/" + String.valueOf(sm.getId()))).entity(sm)
					.build();
		} catch (RollbackException e) {
			return Response.status(409).entity("The SensorMeasurement could not be created!").build();
		}
	}

	/**
	 * Deletes SensorProperty with the id. Response Code: <br>
	 * 204: noContent, the answer intentionally does not return anything.
	 * 
	 * @param id PK from the SensorMeasurement
	 * @return HTTP Response
	 */
	@DELETE
	@Path("{id}")
	public Response deleteSensorMeasurement(@PathParam("id") long id) {
		SensorMeasurement sm = em.find(SensorMeasurement.class, id);
		if (sm != null) {
			em.getTransaction().begin();
			em.remove(sm);
			em.getTransaction().commit();
		}
		return Response.noContent().build();
	}
}
