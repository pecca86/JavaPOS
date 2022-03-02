/**
 * Utility class for handling different http requests
 */
package sharedResources.utils;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Class for handling this JavaFX project's Http calls to different backend API's
 */

public class HttpController {

    private HttpURLConnection conn;
    private String method;
    private String clientUrl;
    private String response;
    private int status;
    // when sending data in body
    private String contentType;
    private String urlParameters;
    private boolean hasHeaders = false;
    private String errorMsg;


    /**
     * Constructor
     *
     * @param method    POST / GET etc.
     * @param clientUrl API URI
     */
    public HttpController (String method, String clientUrl) {
        this.method = method;
        this.clientUrl = clientUrl;
    }

    /**
     * Constructor for HTTP request with data in the body
     *
     * @param method        HTTP Method
     * @param clientUrl     Target URL
     * @param contentType   Data type
     * @param urlParameters Data parameters
     */
    public HttpController (String method, String clientUrl, String contentType, String urlParameters) {
        this.method = method;
        this.clientUrl = clientUrl;
        this.contentType = contentType;
        this.urlParameters = urlParameters;
        this.hasHeaders = true;
    }


    /**
     * Set which method to call the API with.
     *
     * @param m method: POST / GET etc.
     */
    public void setMethod (String m) {
        this.method = m;
    }


    /**
     * Set API url
     *
     * @param u URL we want to call
     */
    public void setUrl (String u) {
        this.clientUrl = u;
    }

    /**
     * @param type The content type to specify, ex. "application/json"
     * @return Controller instance
     */
    public HttpController setContentType (String type) {
        this.contentType = type;
        this.hasHeaders = true;
        return this;
    }

    /**
     * Send a request to the API using classes URL and HttpURLConnection.
     * The response is saved in the class attribute response.
     */
    public void sendRequest () {
        try {
            // Config
            // Connection attributes
            URL url = new URL(this.clientUrl);
            this.conn = (HttpURLConnection) url.openConnection();
            this.conn.setRequestMethod(this.method);

            // If sending with body data
            if (this.hasHeaders) this.setHeaders();

            //RESPONSE
            this.status = this.conn.getResponseCode();
            InputStream stream;
            if (this.status >= 400) stream = this.conn.getErrorStream();
            else stream = this.conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);

            if (this.status >= 400) this.errorMsg = content.toString();
            else this.response = content.toString();
            in.close();

        } catch (ConnectException e) {
            this.response = "Connection to " + this.clientUrl + " failed.";
            System.out.println("Connection failed!");
            this.errorMsg = "Connection error!";
        } catch (IOException e) {
            e.printStackTrace();
            this.errorMsg = "Connection to " + this.clientUrl + " failed. IOException.";
        }
    }


    /**
     * Set the headers for the HTTP request
     */
    private void setHeaders () {
        byte[] postData = this.urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        String request = "<Url here>";
        this.conn.setDoOutput(true);
        this.conn.setInstanceFollowRedirects(false);
        this.conn.setRequestProperty("Content-Type", this.contentType);
        this.conn.setRequestProperty("charset", "utf-8");
        this.conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        this.conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(this.conn.getOutputStream())) {
            wr.write(postData);
        } catch (IOException e) {
            System.out.println("Invalid headers!");
            this.response = "Invalid headers...";
        }
    }


    /**
     * Get the API response.
     *
     * @return the response the client got back from the API.
     */
    public String getResponse () {
        if (this.errorMsg == null) return this.response;
        return this.errorMsg;
    }

    public int getStatus () {
        return this.status;
    }
}
