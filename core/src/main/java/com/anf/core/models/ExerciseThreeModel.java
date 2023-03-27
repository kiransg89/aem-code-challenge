package com.anf.core.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.resource.filter.ResourceFilterStream;

import com.adobe.aemds.guide.utils.JcrResourceConstants;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Model(adaptables = SlingHttpServletRequest.class, resourceType = ExerciseThreeModel.RESOURCE_TYPE, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ExerciseThreeModel {

	public static final String RESOURCE_TYPE = "uxdia/components/content/uxdiaCacheableXF";
	public static final String PROP_PATH = "path";
	public static final String PROP_TYPE = "type";
	public static final String PROP_PROPERTY = "property";
	public static final String PROPERTY_VALUE = "property.value";
	public static final String PROPERTY_VALUE_RESOURCETYPE = "anf-code-challenge/components/page";
	public static final String PROPERTY_ORDERBY_VALUE = "@jcr:created";
	public static final String P_GUESS = "p.guessTotal";
	public static final String P_GUESS_VALUE = "10";
	public static final String SEARCH_PATH = "/content/anf-code-challenge/us/en";

	@ScriptVariable
	protected ResourceResolver resolver;

	@OSGiService
	private QueryBuilder queryBuilder;

	//Please check out my article on how to use lombok in AEM https://kiransg.com/2021/11/07/aem-with-lombok/
	@Getter
	LinkedHashSet<Page> pagesByPathQuery = new LinkedHashSet<>();

	@Getter
	LinkedHashSet<Page> pagesByPathRefFilter = new LinkedHashSet<>();

	PageManager pageManager;

	@PostConstruct
	public void init() {
		/***** Begin Code - Kiran SG *****/
		Map<String, String> qPredicate = new HashMap<>();
		qPredicate.put(PROP_PATH, SEARCH_PATH);
		qPredicate.put(PROP_TYPE, JcrResourceConstants.CQ_PAGE_CONTENT);
		qPredicate.put(PROP_PROPERTY, JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY);
		qPredicate.put(PROPERTY_VALUE, PROPERTY_VALUE_RESOURCETYPE);
		qPredicate.put(Predicate.ORDER_BY, PROPERTY_ORDERBY_VALUE);
		qPredicate.put(P_GUESS, P_GUESS_VALUE);
		Session session = resolver.adaptTo(Session.class);
		Query query = queryBuilder.createQuery(PredicateGroup.create(qPredicate), session);
		SearchResult results = query.getResult();
		log.debug("Executed query =" + results.getQueryStatement());
		Iterator<Resource> resources = results.getResources();
		if (null != resources) {
			pageManager = resolver.adaptTo(PageManager.class);
			pagesByPathQuery = StreamSupport.stream(((Iterable<Resource>) () -> resources).spliterator(), false)
					.map(this::getContentPage).collect(Collectors.toCollection(LinkedHashSet::new));
		}
		
		//Please check out my article on how to use query with ResourceFilterStream in AEM https://kiransg.com/2021/11/07/aem-query-builder-using-java-streams/
		Resource resource = resolver.resolve(SEARCH_PATH);
		ResourceFilterStream rfs = resource.adaptTo(ResourceFilterStream.class);
		pagesByPathRefFilter = rfs.setBranchSelector("[jcr:primaryType] == 'cq:Page'")
				.setChildSelector("[jcr:content/sling:resourceType] == 'anf-code-challenge/components/page'").stream()
				.map(this::getContentPage).filter(r -> null != r).limit(10)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		/**** END Code *****/
	}

	private Page getContentPage(Resource resource) {
		if (!StringUtils.equalsIgnoreCase(resource.getPath(), SEARCH_PATH)) {
			return pageManager.getContainingPage(resource);
		}
		return null;
	}
}
