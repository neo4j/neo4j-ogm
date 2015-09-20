package org.neo4j.ogm.driver.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.transaction.AbstractTransaction;
import org.neo4j.ogm.session.transaction.TransactionException;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public class HttpTransaction extends AbstractTransaction {

    private final HttpDriver driver;
    private final String url;

    public HttpTransaction(MappingContext mappingContext, TransactionManager transactionManager, boolean autoCommit, HttpDriver driver, String url) {
        super( mappingContext, transactionManager, autoCommit );
        this.driver = driver;
        this.url = url;
    }

    @Override
    public void rollback() {

        if (!autoCommit()) {
            try {
                String url = url();
                HttpDelete request = new HttpDelete(url);
                driver.executeRequest(request);
            } catch (Exception e) {
                throw new TransactionException(e.getLocalizedMessage());
            }
        }

        super.rollback();
    }

    @Override
    public void commit() {

        if (!autoCommit()) {
            try {
                HttpPost request = new HttpPost(url());
                request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                driver.executeRequest(request);
            } catch (Exception e) {
                throw new TransactionException(e.getLocalizedMessage());
            }
        }

        super.commit();
    }

    @Override
    public String url() {
        return url;
    }
}
