package org.neo4j.ogm.drivers.http.transaction;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.TransactionException;

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

        try {
            if (transactionManager.canRollback()) {
                HttpDelete request = new HttpDelete(url);
                driver.executeHttpRequest(request);
            }
        }
        catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage());
        }
        finally {
            super.rollback();
        }
    }

    @Override
    public void commit() {

        try {
            if (transactionManager.canCommit()) {
                HttpPost request = new HttpPost(url + "/commit");
                request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                driver.executeHttpRequest(request);
            } else {
                if (transactionManager.isExtended(transactionManager.getCurrentTransaction())) {
                    throw new TransactionException("Transaction is marked for rollback");
                }
            }
        }
        catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage());
        }
        finally {
            super.commit();
        }
    }

    public String url() {
        return url;
    }
}
