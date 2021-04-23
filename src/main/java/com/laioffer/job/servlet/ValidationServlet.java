package com.laioffer.job.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.entity.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "ValidationServlet", urlPatterns = {"/validation"})
public class ValidationServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }
        ValidationRequestBody body = mapper.readValue(request.getReader(), ValidationRequestBody.class);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        //Create a custom response handler
        ResponseHandler<Boolean> responseHandler = res -> res.getStatusLine().getStatusCode() == 200;

        try {
            boolean isValid = httpclient.execute(new HttpGet(body.url), responseHandler);
            response.setContentType("application/json");
            ValidationResponseBody resultResponse = new ValidationResponseBody(isValid);
            mapper.writeValue(response.getWriter(), resultResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}
