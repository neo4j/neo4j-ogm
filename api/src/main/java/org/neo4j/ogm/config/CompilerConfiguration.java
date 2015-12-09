package org.neo4j.ogm.config;

/**
 * @author vince
 */
public class CompilerConfiguration {

    public static final String COMPILER = "compiler";

    private final Configuration configuration;

    public CompilerConfiguration() {
        this.configuration = new Configuration();
    }

    public CompilerConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setCompilerClassName(String compilerClassName) {
        configuration.set(COMPILER, compilerClassName);
    }

    public String getCompilerClassName() {
        return (String) configuration.get(COMPILER);
    }
}
