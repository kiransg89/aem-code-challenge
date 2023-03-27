package com.anf.core.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.tagging.TagConstants;
import com.day.crx.JcrConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component(service = WorkflowProcess.class, property = {
		Constants.SERVICE_DESCRIPTION + "=Experience Fragment Replication Notification",
		Constants.SERVICE_VENDOR + "=ANF", "process.label=" + "Anf Form Submission Workflow" })
public class ExerciseOneWorkflow implements WorkflowProcess {

	public static final String USERDATA_PATH = "/var/anf-code-challenge";
	public static final String SERVICE_USER = "anf-administrative-service-user";

	static Map<String, Object> defaultProperties = new HashMap<>();
	static {
		defaultProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
	}

	@Reference
	ResourceResolverFactory resolverFactory;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		/***** Begin Code - Kiran SG *****/
		WorkflowData workData = workItem.getWorkflowData();
		String payload = (String) workData.getPayload();
		try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(
				Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))) {
			Resource resource = resourceResolver.resolve(payload);
			Map<String, Object> userDataProperties = defaultProperties;
			String firstName = resource.getValueMap().get("firstName", StringUtils.EMPTY);
			String lastName = resource.getValueMap().get("lastName", StringUtils.EMPTY);
			userDataProperties.put("firstName", firstName);
			userDataProperties.put("lastName", lastName);
			userDataProperties.put("age", resource.getValueMap().get("age", StringUtils.EMPTY));
			userDataProperties.put("country", resource.getValueMap().get("country", StringUtils.EMPTY));
			ResourceUtil.getOrCreateResource(resourceResolver,
					USERDATA_PATH + TagConstants.SEPARATOR + firstName + lastName, userDataProperties,
					JcrConstants.NT_UNSTRUCTURED, true);
			resourceResolver.delete(resource);
			resourceResolver.commit();
		} catch (LoginException | PersistenceException e) {
			log.error("Cannot login as a service user", e);
		}
		/**** END Code *****/
	}
}