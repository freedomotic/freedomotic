package com.freedomotic.helpers;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;

import java.nio.charset.Charset;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
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

    private static String asBasicAuthString() {
        return encodeBase64String((A_USERNAME + ":" + A_PASSWORD).getBytes(Charset.forName("ASCII")));
    }

}