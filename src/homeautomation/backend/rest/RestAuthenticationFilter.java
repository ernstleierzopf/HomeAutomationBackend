package homeautomation.backend.rest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.persistence.NoResultException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;

import homeautomation.backend.entity.User;

public class RestAuthenticationFilter implements javax.servlet.Filter {
	public static final String AUTHENTICATION_HEADER = "Authorization";
	public static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
	public static final String QUOP_HEADER = "quop";
	public static final String NONCE_HEADER = "Nonce";
	public static final String TIMESTAMP_HEADER = "Time";
	public static final String USER_EMAIL_HEADER = "Email";
	public static final String LOGGER = "HomeAutomationLogger";
	private String email;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filter) throws IOException, ServletException {
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			Logger.getLogger(LOGGER).info(httpServletRequest.getRemoteAddr() + "@" + httpServletRequest.getRequestURI() + " accessed.");
			
			String authCredentials = httpServletRequest
					.getHeader(AUTHENTICATION_HEADER);
			email = httpServletRequest.getHeader(USER_EMAIL_HEADER);

			// better injected
			AuthenticationService authenticationService = new AuthenticationService();
			
			if(AuthenticationService.EM == null) {
				AuthenticationService.EM = AuthenticationService.EMF.createEntityManager();
			}
			
			try {
				boolean authenticationStatus = authenticationService.authenticateDigest(authCredentials, email);
			
				if (authenticationStatus) {
					filter.doFilter(request, response);
				} else {
					if (response instanceof HttpServletResponse) {
						HttpServletResponse httpServletResponse = (HttpServletResponse) response;
						byte[] nonce = authenticationService.createNonce(System.currentTimeMillis());
						Timestamp time = new Timestamp(System.currentTimeMillis());
						if(email != null) {
							AuthenticationService.EM.getTransaction().begin();
							User u = AuthenticationService.EM.find(User.class, email);
							if(u != null) {
								u.setNonce(nonce);
								u.setNonceCreationDate(time);
							}
							AuthenticationService.EM.getTransaction().commit();
						}
						httpServletResponse.addHeader(WWW_AUTHENTICATE_HEADER, "Digest " + Base64.encodeBase64String(nonce));
						httpServletResponse.addHeader(QUOP_HEADER, "auth");
						httpServletResponse.addHeader(NONCE_HEADER, Base64.encodeBase64String(nonce));
						httpServletResponse.addHeader(TIMESTAMP_HEADER, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time));
						httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					}
				}
			} catch(NoResultException | NoSuchAlgorithmException e) {
				if (response instanceof HttpServletResponse) {
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					String nonce = Base64.encodeBase64String(authenticationService.createNonce(System.currentTimeMillis()));
					Timestamp time = new Timestamp(System.currentTimeMillis());
					httpServletResponse.addHeader(WWW_AUTHENTICATE_HEADER, "Digest " + nonce);
					httpServletResponse.addHeader(NONCE_HEADER, nonce);
					httpServletResponse.addHeader(QUOP_HEADER, "auth");
					httpServletResponse.addHeader(TIMESTAMP_HEADER, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time));
					httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
				
			}

		}
	}
}