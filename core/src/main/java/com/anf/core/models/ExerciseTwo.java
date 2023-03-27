package com.anf.core.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;

import com.anf.core.beans.NewsData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Model(adaptables = SlingHttpServletRequest.class, resourceType = ExerciseTwo.RESOURCE_TYPE, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ExerciseTwo {

	public static final String RESOURCE_TYPE = "uxdia/components/content/uxdiaCacheableXF";
	public static final String NESW_PATH = "/var/commerce/products/anf-code-challenge/newsData";
	public static final String SERVICE_USER = "anf-administrative-service-user";

	@ScriptVariable
	protected ResourceResolver resolver;

	@OSGiService
	ResourceResolverFactory resolverFactory;

	public Set<NewsData> getNewsContent() {
		/***** Begin Code - Kiran SG *****/
		try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(
				Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))) {
			Resource resource = resourceResolver.resolve(NESW_PATH);
			if (!ResourceUtil.isNonExistingResource(resource) && resource.hasChildren()) {
				return StreamSupport.stream(resource.getChildren().spliterator(), false).map(this::prepareData)
						.collect(Collectors.toSet());
			}
		} catch (LoginException e) {
			log.error("Cannot login as a service user", e);
		}
		return new HashSet<>();
		/**** END Code *****/
	}

	private NewsData prepareData(Resource resource) {
		NewsData newsData = new NewsData();
		newsData.setTitle(resource.getValueMap().get("title", StringUtils.EMPTY));
		newsData.setDescription(resource.getValueMap().get("description", StringUtils.EMPTY));
		newsData.setContent(resource.getValueMap().get("content", StringUtils.EMPTY));
		newsData.setAuthor(resource.getValueMap().get("author", StringUtils.EMPTY));
		newsData.setUrlImage(resource.getValueMap().get("urlImage", StringUtils.EMPTY));
		newsData.setUrl(resource.getValueMap().get("url", StringUtils.EMPTY));
		return newsData;
	}
}