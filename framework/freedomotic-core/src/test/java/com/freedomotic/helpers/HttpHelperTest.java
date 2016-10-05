package com.freedomotic.helpers;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;

import java.nio.charset.Charset;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HttpHelperTest {

    private static final String A_USERNAME = "user";
    private static final String A_PASSWORD = "password";
    private static final String A_PATH = "/everything";
    private static final String OK_RESPONSE = "OK";
    public static final int RANDOM_PORT = 0;

    private HttpHelper httpHelper;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(RANDOM_PORT);

    private MockServerClient mockServerClient;
    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        Integer port = mockServerRule.getPort();
        mockServerClient = new MockServerClient("localhost", mockServerRule.getPort());

        httpHelper = new HttpHelper();
        baseUrl = "http://localhost:" + port + A_PATH;
    }

    @Test
    public void usernameAndPasswordAreSendWithRequest() throws Exception {
        respondWhenBasicAuthIsGiven(SC_OK, OK_RESPONSE);
        respond401WhenNoBasicAuthenticationIsGiven();

        String content = httpHelper.retrieveContent(baseUrl, A_USERNAME, A_PASSWORD);

        assertEquals(OK_RESPONSE, content);
    }

    @Test
    public void xmlResponseCanBeParsed() throws Exception {
        String xmlResponse = "<xml>" +
                "<test>" +
                OK_RESPONSE +
                "</test>" +
                "</xml>";
        respond(SC_OK, xmlResponse);

        String xPath = "//test/child::text()";
        List<String> xmls = httpHelper.queryXml(baseUrl, A_USERNAME, A_PASSWORD, xPath);

        assertEquals(1, xmls.size());
        assertEquals(OK_RESPONSE, xmls.get(0));
    }

    @Test
    public void xmlShouldBePosted() throws Exception {
        // Given
        final String requestBody = "<xml><test>data</test></xml>";
        final String responseBody = String.format("<xml><test>%s</test></xml>", OK_RESPONSE);
        final byte[] request = requestBody.getBytes("UTF-8");
        mockServerClient.when(request().withBody(requestBody))
                .respond(response().withBody(responseBody).withStatusCode(200));

        // When
        byte[] response = httpHelper.post(baseUrl, request, A_USERNAME, A_PASSWORD);

        // Then
        assertThat(responseBody, equalTo(new String(response, "UTF-8")));

    }

    private void respondWhenBasicAuthIsGiven(int responseStatusCode, String response) {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withHeader(new Header("Authorization", "Basic " + asBasicAuthString()))
                        .withPath(A_PATH)
        )
                .respond(
                        response()
                                .withStatusCode(responseStatusCode)
                                .withBody(response)
                );
    }

    private void respond401WhenNoBasicAuthenticationIsGiven() {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath(A_PATH)
        )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.SC_UNAUTHORIZED)
                                .withHeader(new Header("WWW-Authenticate", "Basic realm=\"Realm\""))
                );
    }

    private void respond(int responseStatusCode, String response) {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath(A_PATH)
        )
                .respond(
                        response()
                                .withStatusCode(responseStatusCode)
                                .withBody(response)
                );
    }

    private static String asBasicAuthString() {
        return encodeBase64String((A_USERNAME + ":" + A_PASSWORD).getBytes(Charset.forName("ASCII")));
    }

}