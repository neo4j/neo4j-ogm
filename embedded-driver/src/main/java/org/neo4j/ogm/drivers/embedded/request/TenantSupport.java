package org.neo4j.ogm.drivers.embedded.request;

import static scala.collection.JavaConverters.*;

import scala.Function1;
import scala.Option;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.cypher.internal.frontend.v3_4.ast.Statement;
import org.neo4j.cypher.internal.frontend.v3_4.parser.CypherParser;
import org.neo4j.cypher.internal.frontend.v3_4.prettifier.ExpressionStringifier;
import org.neo4j.cypher.internal.frontend.v3_4.prettifier.Prettifier;
import org.neo4j.cypher.internal.util.v3_4.bottomUp;
import org.neo4j.cypher.internal.v3_4.expressions.LabelName;
import org.neo4j.cypher.internal.v3_4.expressions.NodePattern;

public class TenantSupport {

    private static final String ILLEGAL_TENANT_MESSAGE = "Only tenants with alpha-numeric characters are allowed, starting with an alphabetic charater. This tenant does not match the rule: ";
    private final String tenant;

    public TenantSupport(String tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant must not be null");
        }
        this.tenant = tenant;
    }

    public static TenantSupport supportFor(String tenant) {
        if (hasIllegalForm(tenant)) {
            throw new IllegalArgumentException(ILLEGAL_TENANT_MESSAGE + tenant);
        }

        return new TenantSupport(tenant);
    }

    private static boolean hasIllegalForm(String tenant) {
        return !(
            // check the first character first to be alphabetic
            StringUtils.isAlphanumeric(tenant)
                // finds null, empty, only blanks, blanks within the string and special characters
                || StringUtils.isAlpha(tenant.substring(0, 1))
        );
    }

    public String withTenant(String cypher) {
        CypherParser cypherParser = new CypherParser();
        Statement statement = cypherParser.parse(cypher, Option.empty());

        Function1<Object, Object> rewriter = bottomUp.apply(new AddLabelRewriter(tenant), SimpleStopper.SIMPLE_STOPPER);

        Prettifier prettifier = new Prettifier(new ExpressionStringifier(null));

        return prettifier.asString((Statement) rewriter.apply(statement));
    }

    private static class SimpleStopper extends scala.runtime.AbstractFunction1<Object, Object> {
        final static SimpleStopper SIMPLE_STOPPER = new SimpleStopper();

        private SimpleStopper() {
        }

        @Override public Object apply(Object object) {
            return false;
        }
    }

    private class AddLabelRewriter extends scala.runtime.AbstractFunction1<Object, Object> {

        private final String tenant;

        private AddLabelRewriter(String tenant) {
            this.tenant = tenant;
        }

        @Override public Object apply(Object patternElement) {

            if (patternElement instanceof NodePattern) {

                NodePattern nodePattern = (NodePattern) patternElement;
                Seq<LabelName> newLabels = addTenantLabel(nodePattern);

                return nodePattern
                    .copy(nodePattern.variable(), newLabels, nodePattern.properties(), nodePattern.position());
            } else {
                return patternElement;
            }
        }

        private Seq<LabelName> addTenantLabel(NodePattern nodePattern) {
            Collection<LabelName> existingLabels = new ArrayList<LabelName>(
                asJavaCollectionConverter(nodePattern.labels()).asJavaCollection());
            existingLabels.add(new LabelName(tenant, nodePattern.position()));

            return collectionAsScalaIterableConverter(existingLabels).asScala().toSeq();
        }
    }
}
