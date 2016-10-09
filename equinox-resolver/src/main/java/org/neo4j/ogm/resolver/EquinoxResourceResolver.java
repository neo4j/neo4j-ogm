package org.neo4j.ogm.resolver;

import org.eclipse.core.runtime.FileLocator;
import org.neo4j.ogm.classloader.ResourceResolver;

import java.io.File;
import java.net.URL;

/**
 * {@link org.neo4j.ogm.classloader.ResourceResolver} for Equinox OSGi container that
 * converts "bundleresource" URLs to files.
 *
 * @author dkrizic
 */
public class EquinoxResourceResolver implements ResourceResolver {

    /**
     * Handles bundleresource URLs by using Equinox specific {@link FileLocator}.
     * @param resource the URL to be converted
     * @return The file or null if it is another protocol than bundleresource.
     * @throws Exception
     */
    @Override
    public File resolve(URL resource) throws Exception {
        if( resource.getProtocol().equals("bundleresource") ) {
            final URL url = FileLocator.toFileURL(resource);
            return new File(url.getPath());
        }
        return null;
    }

}


