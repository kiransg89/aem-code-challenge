package com.anf.core.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component(service = { Servlet.class }, immediate = true)
@SlingServletResourceTypes(resourceTypes = ExerciseOneDatasource.RESOURCE_TYPE, methods = HttpConstants.METHOD_GET)
public class ExerciseOneDatasource extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 1L;
	static final String RESOURCE_TYPE = "form/options/country/datasource";
	static final String DATASOURCE_PATH = "/content/dam/anf-code-challenge/exercise-1/countries.json";

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
			throws IOException {
		/***** Begin Code - Kiran SG *****/
		try {
			ResourceResolver resolver = request.getResourceResolver();
			Resource resource = resolver.resolve(DATASOURCE_PATH);
			Asset asset = resource.adaptTo(Asset.class);
			Rendition original = asset.getOriginal();
			try (InputStream in = original.adaptTo(InputStream.class);
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
				JSONObject jsonObject = new JSONObject(reader.lines().collect(Collectors.joining()));
				List<Resource> optionsResourceList = new ArrayList<>();
				if (null != jsonObject.keys()) {
					@SuppressWarnings("rawtypes")
					Iterator keys = jsonObject.keys();
					StreamSupport.stream(((Iterable<String>) () -> keys).spliterator(), false).sorted().forEach(key -> {
						try {
							ValueMap map = new ValueMapDecorator(new TreeMap<>());
							map.put("text", key);
							map.put("value", jsonObject.get(key));
							optionsResourceList.add(
									new ValueMapResource(resolver, new ResourceMetadata(), "nt:unstructured", map));
						} catch (JSONException e) {
							log.error("Unable to persist the resource {}", e.getMessage());
						}
					});
				}
				DataSource ds = new SimpleDataSource(optionsResourceList.iterator());
				request.setAttribute(DataSource.class.getName(), ds);
			}
		} catch (JSONException e) {
			log.error("Unable to persist the resource {}", e.getMessage());
		}
		/**** END Code *****/
	}
}