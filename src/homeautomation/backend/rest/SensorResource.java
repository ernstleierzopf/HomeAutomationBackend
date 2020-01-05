package homeautomation.backend.rest;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
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

import homeautomation.backend.entity.Sensor;
import homeautomation.backend.entity.SensorProperty;
import homeautomation.backend.entity.SensorType;

@Stateless
@Path("secure/sensor")
public class SensorResource {

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
	 * returns all Sensors <br>
	 * 200: OK, Sensors are in the body of the Response. <br>
	 * 404: No Sensors exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSensors() {
		TypedQuery<Sensor> query = em.createQuery("SELECT u FROM Sensor u", Sensor.class);
		List<Sensor> sensors = query.getResultList();
		if (sensors.size() > 0) {
			try {
				JSONArray jsonArray = Sensor.ConvertToJsonArray(sensors);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	/**
	 * returns the Sensor with a specific serialNumber <br>
	 * 200: OK, Sensor is in the body of the Response. <br>
	 * 404: The Sensor does not exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param serialNumber PK of the Sensor Table
	 * @return HTTP Response with the Sensor as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/serialNumber/{serialNumber}")
	public Response getSensorBySerialNumber(@PathParam("serialNumber") String serialNumber) {
		TypedQuery<Sensor> query = em.createQuery("SELECT u FROM Sensor u where u.serialNumber = :serialNumber",
				Sensor.class);
		query.setParameter("serialNumber", serialNumber);
		List<Sensor> sensors = query.getResultList();
		if (sensors.size() > 0) {
			try {
				JSONObject jsonObject = Sensor.ConvertToJsonObject(sensors.get(0));
				return Response.ok().entity(jsonObject.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	/**
	 * returns all Sensors with a specific type <br>
	 * 200: OK, Sensors are in the body of the Response. <br>
	 * 404: No Sensor with the type does not exist in the database. <br>
	 * 500: The JSONArray could not be built.
	 * 
	 * @param type The ordinal Number of the SensorType
	 * @return HTTP Response with the Sensors as Entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/type/{type}")
	public Response getSensorsByType(@PathParam("type") int type) {
		if (type < 0 || type > SensorType.values().length - 1)
			return Response.status(400)
					.entity("The sensor type must be between 0 and " + (SensorType.values().length - 1)).build();

		TypedQuery<Sensor> query = em.createQuery("SELECT u FROM Sensor u where u.type = :type", Sensor.class);
		query.setParameter("type", SensorType.values()[type]);
		List<Sensor> sensors = query.getResultList();
		if (sensors.size() > 0) {
			try {
				JSONArray jsonArray = Sensor.ConvertToJsonArray(sensors);
				return Response.ok().entity(jsonArray.toString()).build();
			} catch (JSONException e) {
				return Response.status(500).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	/**
	 * creates a new Sensor in the Sensor Table. Return Codes: <br>
	 * 201: Created, the Sensor was persisted in the Table.<br>
	 * 409: Conflict, the Sensor already exists in the Database.
	 * 
	 * @param sensor the Entity to be persisted
	 * @return HTTP Response with the path to the entity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSensor(Sensor sensor) {
		try {
			em.getTransaction().begin();
			em.persist(sensor);
			em.getTransaction().commit();
			return Response.created(URI.create(uri.getAbsolutePath() + "/" + String.valueOf(sensor.getSerialNumber())))
					.entity(sensor).build();
		} catch (RollbackException e) {
			return Response.status(409).entity("The serial number of the sensor already exists!").build();
		}
	}

	/**
	 * updates a Sensor in the Sensor Table. Return Codes: <br>
	 * 201: Created, the Sensor did not exist in the table and was persisted. <br>
	 * 204: noContent, the Sensor was updated and the answer intentionally does not
	 * return anything.
	 * 
	 * @param sensor the entity to be updated
	 * @return HTTP Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSensor(Sensor sensor) {
		Sensor s = em.find(Sensor.class, sensor.getSerialNumber());
		if (s != null) {
			s.setType(sensor.getType());
			s.setUnitOfMeasurement(sensor.getUnitOfMeasurement());
			em.merge(s);
			return Response.noContent().build();
		} else {
			return createSensor(sensor);
		}
	}

	/**
	 * Deletes Sensor with the serialNumber. Response Code: <br>
	 * 204: noContent, the answer intentionally does not return anything.
	 * 
	 * @param serialNumber PK from the Sensor
	 * @return HTTP Response
	 */
	@DELETE
	@Path("{serialNumber}")
	public Response deleteSensor(@PathParam("serialNumber") String serialNumber) {
		Sensor s = em.find(Sensor.class, serialNumber);
		if (s != null) {
			//check if measurements exist
			Query queryMeasurements = em
					.createQuery("SELECT COUNT(u.sensor.serialNumber) FROM SensorMeasurement u WHERE u.sensor.serialNumber = :serialNumber");
			queryMeasurements.setParameter("serialNumber", s.getSerialNumber());
			long count = (long) queryMeasurements.getSingleResult();
			if(count > 0)
				return Response.status(409).build();
			
			em.getTransaction().begin();
			TypedQuery<SensorProperty> queryProperties = em
					.createQuery("SELECT u FROM SensorProperty u WHERE u.sensor = :serialNumber", SensorProperty.class);
			queryProperties.setParameter("serialNumber", s);
			List<SensorProperty> sensorProperties = queryProperties.getResultList();
			for(int i = 0; i < sensorProperties.size(); i++) {
				em.remove(sensorProperties.get(i));
			}
			em.remove(s);
			em.getTransaction().commit();
		}
		return Response.noContent().build();
	}

	/**
	 * closes the EntityManager em and the EntityManagerFactory emf.
	 */
	@PreDestroy
	public void destroy() {
		em.close();
	}
}
