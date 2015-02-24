package com.solidify.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by jrobins on 2/24/15.
 */
public class ParsedObject {
    private String json;
    private ParentObject p;

    public ParsedObject(String json) {
       this.json = json;
        this.p = new ParentObject();
        try {
            parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parse() throws IOException {
        if (json == null || "".equals(json)) {
            return;
        }
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(json);
        JsonToken current = null;
        Stack<ParentObject> objects = new Stack<ParentObject>();
        JSONObject curObj = null;
        String field = null;

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            return;
        }

        while((current = jp.nextToken()) != null) {
            if (current == JsonToken.FIELD_NAME) {
                field = jp.getCurrentName();
            } else if (current == JsonToken.VALUE_STRING) {
                p.put(field,jp.getValueAsString());
            } else if (current == JsonToken.START_OBJECT) {
                objects.push(p);
                p = new ParentObject(field);
            } else if (current == JsonToken.END_OBJECT) {
                if (!objects.isEmpty()) {
                    ParentObject tmp = objects.pop();
                    tmp.put(p.getField(),p);
                    p = tmp;
                }
            }
        }
        //System.out.println(p.toString());
    }

    public JSONObject get() {
        return p;
    }
}
