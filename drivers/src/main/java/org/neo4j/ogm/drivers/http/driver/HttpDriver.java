package org.neo4j.ogm.drivers.http.driver;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.http.request.HttpAuthorization;
import org.neo4j.ogm.drivers.http.request.HttpRequest;
import org.neo4j.ogm.drivers.http.transaction.HttpTransaction;
import org.neo4j.ogm.exception.ResultErrorsException;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */

public final class HttpDriver extends AbstractConfigurableDriver {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Logger logger = LoggerFactory.getLogger(HttpDriver.class);

    // required for service loader mechanism
    public HttpDriver() {
    }
    /**
     * Create a new driver and configure via the specified DriverConfiguration
     * @param configuration
     */
    public HttpDriver(DriverConfiguration configuration) {
        configure(configuration);
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.warn("Unexpected Exception when closing http client httpClient: ", e);
        }
    }

    @Override
    public Request request() {
        String url = requestUrl();
        return new HttpRequest(httpClient, url, driverConfig.getCredentials());
    }

    @Override
    public Transaction newTransaction() {

        String url = newTransactionUrl();
        return new HttpTransaction(transactionManager, this, url);
    }

    // TODO: move this !

    public CloseableHttpResponse executeHttpRequest(HttpRequestBase request) {

        try {
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            Credentials credentials = driverConfig.getCredentials();
            HttpAuthorization.authorize(request, credentials);

            CloseableHttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            logger.debug("Status code: {}", statusLine.getStatusCode());
            if (statusLine.getStatusCode() >= 300) {

                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String responseText = EntityUtils.toString(responseEntity);
                logger.debug(responseText);
                EntityUtils.consume(responseEntity);
                if (responseText.contains("\"errors\":[{") || responseText.contains("\"errors\": [{")) {
                    throw new ResultErrorsException(responseText);
                }
            }
            return response;
        }

        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }

        finally {
            request.releaseConnection();
            logger.debug("Connection released");        }
    }

    private String newTransactionUrl() {
        String url = transactionEndpoint(driverConfig.getURI());
        try {
            //URI uri = new URI(transactionEndpoint(driverConfig.getURI()));

            logger.debug("POST {}", url);
            HttpPost request = new HttpPost(url);
            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            org.apache.http.HttpResponse response = executeHttpRequest(request);
            Header location = response.getHeaders("Location")[0];
            return location.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String autoCommitUrl() {
        return transactionEndpoint(driverConfig.getURI()).concat("/commit");
    }

    private String transactionEndpoint(String server) {
        if (server == null) {
            return null;
        }
        String url = server;

        if (!server.endsWith("/")) {
            url += "/";
        }
        return url + "db/data/transaction";
    }

    private String requestUrl() {
        if (transactionManager != null) {
            Transaction tx = transactionManager.getCurrentTransaction();
            if (tx != null) {
                logger.debug("request url {}", ((HttpTransaction) tx).url());
                return ((HttpTransaction) tx).url();
            } else {
                logger.debug("No current transaction, using auto-commit");
            }
        } else {
            logger.debug("No transaction manager available, using auto-commit");
        }
        logger.debug("request url {}", autoCommitUrl());
        return autoCommitUrl();
    }
}
