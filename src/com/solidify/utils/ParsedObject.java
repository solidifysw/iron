package com.solidify.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by jrobins on 2/24/15.
 */
public class ParsedObject {
    private String json;
    private JSONObject jo;
    private HashSet<String> skips;
    private String currentKey;

    public ParsedObject(String json, HashSet<String> skips) {
        this.json = json;
        this.jo = new JSONObject();
        this.skips = skips;
        this.currentKey = "";
        try {
            parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject get() {
        return jo;
    }

    public void parse() throws IOException {
        if (json == null || "".equals(json)) {
            return;
        }
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(json);
        String field = null;

        JsonToken current = null;
        Stack<Object> els = new Stack();
        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            return;
        }

        els.push(jo);
        while((current = jp.nextToken()) != null) {
            switch(current) {
                case FIELD_NAME:
                    String key = jp.getCurrentName();
                    System.out.println("currentKey: "+currentKey+" key: "+key);
                    System.out.println("len: "+currentKey.length());
                    if (!skips.isEmpty() && currentKey.length() > 0) {
                        if (skips.contains(currentKey+"."+key)) {
                            System.out.println("skipping1: " + currentKey + "." + key);
                            //jp.nextToken();
                            jp.skipChildren();
                        } else {
                            els.push(key);
                        }
                    } else if (!skips.isEmpty() && skips.contains(key)) {
                        System.out.println("skipping2: " + key);
                        //jp.nextToken();
                        jp.skipChildren();
                    } else {
                        els.push(key);
                    }
                    break;
                case START_OBJECT:
                    if (els.peek().getClass().equals(String.class)) {
                        currentKey = "".equals(currentKey) ? (String) els.peek() : currentKey + "." + (String) els.peek();
                        System.out.println("1: "+currentKey);
                    }
                    els.push(new JSONObject());
                    break;
                case END_OBJECT:
                    JSONObject tmp = (JSONObject)els.pop();
                    if (!els.isEmpty()) {
                        if (els.peek().getClass().equals(String.class)) {
                            if (currentKey.length() > 0 && currentKey.contains(".")) {
                                currentKey = currentKey.substring(0,currentKey.lastIndexOf("."));
                                System.out.println(currentKey);
                            } else if (currentKey.length() > 0) {
                                currentKey = "";
                            }
                            field = (String) els.pop();
                            ((JSONObject) els.peek()).put(field, tmp);
                        } else if (els.peek().getClass().equals(JSONArray.class)) {
                            ((JSONArray) els.peek()).put(tmp);
                        }
                    }
                    break;
                case START_ARRAY:
                    currentKey = "".equals(currentKey) ? (String) els.peek() : currentKey + "." + (String) els.peek();
                    System.out.println(currentKey);
                    els.push(new JSONArray());
                    break;
                case END_ARRAY:
                    if (!els.isEmpty()) {
                        JSONArray ja = (JSONArray)els.pop();
                        field = (String)els.pop();
                        if (currentKey.length() > 0 && currentKey.contains(".")) {
                            currentKey = currentKey.substring(0,currentKey.lastIndexOf("."));
                            System.out.println(currentKey);
                        }
                        ((JSONObject)els.peek()).put(field,ja);
                    }
                    break;
                case VALUE_STRING:
                    if (els.peek().getClass().equals(String.class)) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, jp.getValueAsString());
                    } else if (els.peek().getClass().equals(JSONArray.class)) {
                        ((JSONArray)els.peek()).put(jp.getValueAsString());
                    }
                    break;
                case VALUE_FALSE:
                    if (els.peek().getClass().equals(String.class)) {
                        field = (String)els.pop();
                        ((JSONObject)els.peek()).put(field,false);
                    } else if (els.peek().getClass().equals(JSONArray.class)) {
                        ((JSONArray)els.peek()).put(false);
                    }
                    break;
                case VALUE_TRUE:
                    if (els.peek().getClass().equals(String.class)) {
                        field = (String)els.pop();
                        ((JSONObject)els.peek()).put(field, true);
                    } else if (els.peek().getClass().equals(JSONArray.class)) {
                        ((JSONArray)els.peek()).put(true);
                    }
                    break;
                case VALUE_NULL:
                    if (els.peek().getClass().equals(String.class)) {
                        field = (String)els.pop();
                        ((JSONObject)els.peek()).put(field, JSONObject.NULL);
                    } else if (els.peek().getClass().equals(JSONArray.class)) {
                        ((JSONArray)els.peek()).put(JSONObject.NULL);
                    }
                    break;
                case VALUE_NUMBER_FLOAT:
                    if (els.peek().getClass().equals(String.class)) {
                        field = (String)els.pop();
                        ((JSONObject)els.peek()).put(field, jp.getFloatValue());
                    } else if (els.peek().getClass().equals(JSONArray.class)) {
                        ((JSONArray)els.peek()).put(jp.getFloatValue());
                    }
                    break;
                case VALUE_NUMBER_INT:
                    if (els.peek().getClass().equals(String.class)) {
                        field = (String)els.pop();
                        ((JSONObject)els.peek()).put(field, jp.getLongValue());
                    } else if (els.peek().getClass().equals(JSONArray.class)) {
                        ((JSONArray)els.peek()).put(jp.getLongValue());
                    }
                    break;
            }
        }
    }
}
