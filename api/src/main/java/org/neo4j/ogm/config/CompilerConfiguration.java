package org.neo4j.ogm.config;

/**
 * @author vince
 */
public class CompilerConfiguration {

    public static final String COMPILER = "compiler";
    private static final String DEFAULT_COMPILER_CLASS_NAME = "org.neo4j.ogm.compiler.MultiStatementCypherCompiler";

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
        if (configuration.get(COMPILER) == null) {
            return DEFAULT_COMPILER_CLASS_NAME;
        }
        return (String) configuration.get(COMPILER);
    }
}
