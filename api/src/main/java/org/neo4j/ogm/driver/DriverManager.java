package org.neo4j.ogm.driver;

/**
 * Controls the loading and unloading of Drivers in the OGM.
 * NOTE: Most of this functionality has been moved from the old Components class. It can probably be cleaned up.
 *
 * @author Mark Angrish
 */
public class DriverManager {

	private static Driver driver;

	public static void register(String driverClassName) {
		if (driver != null && !driver.getClass().getCanonicalName().equals(driverClassName)) {
			driver.close();
			driver = null;
		}
		try {
			final Class<?> driverClass = Class.forName(driverClassName);
			driver = (Driver) driverClass.newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Driver getDriver() {
		return driver;
	}

	public static void degregister(Driver driver) {
		if (driver != null) {
			driver.close();
			DriverManager.driver = null;
		}
	}

	public static void register(Driver driver) {
		DriverManager.driver = driver;
	}
}
