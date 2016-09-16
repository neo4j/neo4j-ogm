package org.neo4j.ogm.index;

/**
 * Created by markangrish on 16/09/2016.
 */
public enum AutoIndexMode {
	NONE("none"),
	ASSERT("assert"),
	CREATE_DROP("create-drop"),
	VALIDATE("validate"),
	DUMP("dump");

	public static String stringValues() {

		StringBuilder sb = new StringBuilder("[");
		AutoIndexMode[] values = values();
		if (values.length >= 1) {
			sb.append(values[0]);
		}

		for (int i = 1; i < values.length; i++){
			sb.append(", ");
			sb.append(values[i]);
		}
		return sb.append("]").toString();
	}

	public static AutoIndexMode fromString(String name) {
		if (name != null) {
			for (AutoIndexMode mode : AutoIndexMode.values()) {
				if (name.equalsIgnoreCase(mode.name)) {
					return mode;
				}
			}
		}
		return null;
	}

	private final String name;

	AutoIndexMode(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}
}
