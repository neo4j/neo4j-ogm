package org.neo4j.ogm.driver.http.transaction;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.driver.http.driver.HttpDriver;
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

    public HttpTransaction(MappingContext mappingContext, TransactionManager transactionManager, HttpDriver driver, String url) {
        super( mappingContext, transactionManager);
        this.driver = driver;
        this.url = url;
    }

    @Override
    public void rollback() {

        if (transactionManager != null && transactionManager.getCurrentTransaction() != null) {
            try {
                HttpDelete request = new HttpDelete(url);
                driver.executeHttpRequest(request);
            } catch (Exception e) {
                throw new TransactionException(e.getLocalizedMessage());
            }
        }

        super.rollback();
    }

    @Override
    public void commit() {

        if (transactionManager != null && transactionManager.getCurrentTransaction() != null) {
            try {
                HttpPost request = new HttpPost(url);
                request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                driver.executeHttpRequest(request);
            } catch (Exception e) {
                throw new TransactionException(e.getLocalizedMessage());
            }
        }

        super.commit();
    }

    public String url() {
        return url;
    }
}
