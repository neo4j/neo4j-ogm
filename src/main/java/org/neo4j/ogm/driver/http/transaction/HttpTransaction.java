package org.neo4j.ogm.driver.http.transaction;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.api.transaction.TransactionManager;
import org.neo4j.ogm.driver.http.driver.HttpDriver;
import org.neo4j.ogm.driver.impl.transaction.AbstractTransaction;
import org.neo4j.ogm.driver.impl.transaction.TransactionException;

/**
 * @author vince
 */
public class HttpTransaction extends AbstractTransaction {

    private final HttpDriver driver;
    private final String url;

    public HttpTransaction(TransactionManager transactionManager, HttpDriver driver, String url) {
        super(transactionManager);
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
                HttpPost request = new HttpPost(url + "/commit");
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
