package com.solidify.api;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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

            Enumeration e = req.getParameterNames();
            while (e.hasMoreElements()) {
                String param = (String) e.nextElement();
                System.out.println(param + " : " + req.getParameter(param));
            }


            person.put("firstName", "John");
            person.put("lastName", "Doe");
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.println(person.toString());
    }

}
