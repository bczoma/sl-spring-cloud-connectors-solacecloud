package com.solace.spring.cloud.solacecloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.springframework.cloud.AbstractCloudConnector;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.FallbackServiceInfoCreator;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.service.BaseServiceInfo;
import org.springframework.cloud.util.EnvironmentAccessor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SolaceCloudConnector extends AbstractCloudConnector<Map<String,Object>> {

	private ObjectMapper objectMapper = new ObjectMapper();
	private EnvironmentAccessor environment = new EnvironmentAccessor();
	
	private ApplicationInstanceInfoCreator applicationInstanceInfoCreator = new ApplicationInstanceInfoCreator();

	private Iterable<ServiceDataPostProcessor> serviceDataPostProcessors;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SolaceCloudConnector() {
		super((Class) SolaceCloudServiceInfoCreator.class);
		scanServiceDataPostProcessors();
	}

	@Override
	public boolean isInMatchingCloud() {
		return environment.getEnvValue("SCLOUD_APPLICATION") != null;
	}
	
	@Override
	public ApplicationInstanceInfo getApplicationInstanceInfo() {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> rawApplicationInstanceInfo 
				= objectMapper.readValue(environment.getEnvValue("SCLOUD_APPLICATION"), Map.class);
			return applicationInstanceInfoCreator.createApplicationInstanceInfo(rawApplicationInstanceInfo);
		} catch (Exception e) {
			throw new CloudException(e);
		} 
	}
	
	/* package for testing purpose */
	void setCloudEnvironment(EnvironmentAccessor environment) {
		this.environment = environment;
	}

	
	/**
	 * Return object representation of the SCLOUD_SERVICES environment variable
	 * <p>
	 * Returns a list whose element is a map with service attributes. 
	 * </p>
	 * @return parsed service data
	 */
	protected List<Map<String,Object>> getServicesData() {
		String servicesString = environment.getEnvValue("SCLOUD_SERVICES");
		SolaceCloudRawServiceData rawServices = new SolaceCloudRawServiceData();
		
		if (servicesString != null && servicesString.length() > 0) {
			try {
				rawServices = objectMapper.readValue(servicesString, SolaceCloudRawServiceData.class);
			} catch (Exception e) {
				throw new CloudException(e);
			}
		}

		for (ServiceDataPostProcessor postProcessor : serviceDataPostProcessors) {
			rawServices = postProcessor.process(rawServices);
		}

		List<Map<String, Object>> flatServices = new ArrayList<Map<String, Object>>();
		for (Map.Entry<String, List<Map<String,Object>>> entry : rawServices.entrySet()) {
			flatServices.addAll(entry.getValue());
		}

		return flatServices;
	}
	

	@Override
	protected FallbackServiceInfoCreator<BaseServiceInfo,Map<String,Object>> getFallbackServiceInfoCreator() {
		return new SolaceCloudFallbackServiceInfoCreator();
	}

	private void scanServiceDataPostProcessors() {
		serviceDataPostProcessors = ServiceLoader.load(ServiceDataPostProcessor.class);
	}
}

class SolaceCloudFallbackServiceInfoCreator extends FallbackServiceInfoCreator<BaseServiceInfo,Map<String,Object>> {
	@Override
	public BaseServiceInfo createServiceInfo(Map<String,Object> serviceData) {
		String id = (String) serviceData.get("name");
		return new BaseServiceInfo(id);
	}
}
