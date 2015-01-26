package com.solidify.api;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Created by jrobins on 1/15/15.
 */
public class Person extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        System.out.println("in Person.doGet");
        PrintWriter out = res.getWriter();
        JSONObject person = new JSONObject();
        System.out.println(req.getRequestURI());
        try {
            person.put("firstName", "John");
            person.put("lastName", "Doe");
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.println(person.toString());
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("in Person.doPut");
        System.out.println(request.getRequestURI());
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        String data = buffer.toString();
        JSONObject jo = new JSONObject(data);
        System.out.println(jo.getString("firstName"));

    }

}
