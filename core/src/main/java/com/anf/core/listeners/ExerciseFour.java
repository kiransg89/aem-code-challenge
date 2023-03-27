package com.anf.core.listeners;

import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.observation.JackrabbitEventFilter;
import org.apache.jackrabbit.api.observation.JackrabbitObservationManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * This is a JCR event listener which listens page added (event = NODE_ADDED)
 */
@Slf4j
@Component(service = EventListener.class, immediate = true)
public class ExerciseFour implements EventListener {

	@Reference
	private SlingRepository repository;

	@Reference
	private JobManager jobManager;

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	private JackrabbitObservationManager observationManager;

	public static final String ROOT_PATH = "/content/anf-code-challenge/us/en";
	public static final String SERVICE_USER = "anf-administrative-service-user";

	private final String[] nodeTypes = new String[] { NameConstants.NT_PAGE, "cq:PageContent",
			JcrConstants.NT_UNSTRUCTURED };

	/**
	 * The event handler delegates the event handling to a sling job.
	 *
	 * @param eventIterator the events iterator
	 */
	@Override
	public void onEvent(EventIterator eventIterator) {
		/***** Begin Code - Kiran SG *****/
		try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(
				Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))) {
			while (eventIterator.hasNext()) {
				Event event = eventIterator.nextEvent();
				doAction(event, resourceResolver);
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		/**** END Code *****/
	}

	private void doAction(Event event, ResourceResolver resourceResolver)
			throws RepositoryException, PersistenceException {
		String eventPath = event.getPath();
		eventPath = StringUtils.substringBefore(eventPath, "/jcr:content");
		log.debug("eventpath : {}", eventPath);
		Resource eventResource = resourceResolver.getResource(eventPath);
		log.debug("jcrContent : {}", eventResource.getName());
		Resource contentResource = eventResource.getChild("jcr:content");
		log.debug("contentResource : {}", contentResource.getName());
		Node valueMapRes = contentResource.adaptTo(Node.class);
		log.debug("valueMapRes : {}", valueMapRes.getName());
		valueMapRes.setProperty("pageCreated", "true");
		resourceResolver.commit();
		log.info("page Created property has been added: {} ", eventPath);
	}

	/**
	 * Login using the service user and register the event listener for the
	 * appropriate content path, node types.
	 */
	@Activate
	protected void activate() {
		try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(
				Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))) {
			log.debug("Node update event listener activated");
			JackrabbitEventFilter jackrabbitEventFilter = new JackrabbitEventFilter().setAbsPath(ROOT_PATH)
					.setNodeTypes(nodeTypes).setEventTypes(Event.NODE_ADDED).setIsDeep(true).setNoExternal(true)
					.setNoLocal(false);
			Session session = resourceResolver.adaptTo(Session.class);
			if (session != null) {
				Workspace workSpace = session.getWorkspace();
				if (null != workSpace) {
					observationManager = (JackrabbitObservationManager) workSpace.getObservationManager();
					observationManager.addEventListener(this, jackrabbitEventFilter);
					log.debug("The Page Event Listener is Registered at {} for the event type {}.", ROOT_PATH,
							jackrabbitEventFilter.getEventTypes());
				}
			}
		} catch (RepositoryException | LoginException e) {
			log.error("An error occurred while getting session", e);
		}
	}

	/**
	 * On deactivate, close/logout the long running session.
	 */
	@Deactivate
	protected void deactivate() {

		log.debug("node update event listener deactivated");
		try {
			if (null != observationManager) {
				observationManager.removeEventListener(this);
				log.info("The Page Event Listener is removed.");
			}
		} catch (RepositoryException e) {
			log.error("An error occurred while removing event listener", e);
		}
	}
}