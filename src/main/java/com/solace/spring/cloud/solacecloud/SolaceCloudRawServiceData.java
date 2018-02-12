package com.solace.spring.cloud.solacecloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SolaceCloudRawServiceData} object represents the data read from the {@literal SCLOUD_SERVICES}
 * environment variable and transformed from JSON text to a collection of objects.
 *
 * The root of the data structure is a {@link List}. Each element of the list represents one service from
 * the JSON.
 */
public class SolaceCloudRawServiceData extends HashMap<String, List<Map<String,Object>>> {

	private static final long serialVersionUID = 4752489968962323379L;

	public SolaceCloudRawServiceData() {
		super();
	}

	public SolaceCloudRawServiceData(Map<? extends String, ? extends List<Map<String, Object>>> map) {
		super(map);
	}
}
