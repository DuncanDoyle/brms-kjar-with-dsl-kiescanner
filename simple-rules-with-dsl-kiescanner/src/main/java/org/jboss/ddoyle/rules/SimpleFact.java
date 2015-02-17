package org.jboss.ddoyle.rules;

import java.util.UUID;

public class SimpleFact {

	private final String id;
	
	public SimpleFact() {
		this(UUID.randomUUID().toString());
	}
	
	public SimpleFact(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
}
