package homeautomation.backend.rest;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;

import homeautomation.backend.entity.User;

@Path("user")
public class UserResource {
	
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
	 * returns the User activation status. <br>
	 * 200: OK, User activation status is in the response. <br>
	 * 404: The User does not exist in the database. <br>
	 * 
	 * @param email The email address of the user.
	 * @return HTTP Response with the User activation status as Entity
	 */
	@GET
	@Path("{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response isUserActivated(@PathParam("email") String email) {
		TypedQuery<User> query = em.createQuery(
				"SELECT u FROM User u where u.email = :email", User.class);
		query.setParameter("email", email);
		List<User> users = query.getResultList();
		if (users.size() > 0) {
			return Response.ok().entity(users.get(0).isActivated()).build();
		} else {
			return Response.status(404).build();
		}
	}
	
	/**
	 * creates a new User in the User Table. Return Codes: <br>
	 * 201: Created, the User was persisted in the Table.<br>
	 * 400: The user's activation attribute was true. It must be false! <br>
	 * 409: Conflict, the User already exists in the Database.
	 * 
	 * @param sensor the Entity to be persisted
	 * @return HTTP Response with the path to the entity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(User user) {
		try {
			em.getTransaction().begin();
			if (user.isActivated())
				return Response.status(400).entity("The user's activation attribute is true. It must be false!").build();
			em.persist(user);
			em.getTransaction().commit();
			return Response.created(URI.create(uri.getAbsolutePath() + "/" + String.valueOf(user.getEmail())))
					.entity(user).build();
		} catch (RollbackException e) {
			Logger.getLogger(RestAuthenticationFilter.LOGGER)
				.info("Exception at" + this.getClass() + ": " + e.getMessage());
			return Response.status(409).entity("The serial number of the user already exists!").build();
		}
	}

}
