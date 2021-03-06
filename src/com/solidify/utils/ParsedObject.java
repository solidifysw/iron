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
    private HashSet<String> keys;
    private String currentKey;
    private Stack<Object> els;
    private SkipLogic should;

    /**
     * Stream parses the json input string into a JSONObject accessible with the get method.  Accepts a HashSet of Strings
     * representing the elements in the json string to exclude from the JSONObject result.  Use dot notation.  For example:
     * json = {"a":"abc", "b":"def", "c":{"d":"ghi","e":["x","y"]}}
     * by including b and c.e in the keys HashSet the resultant JSONObject would contain:
     * {"a":"abc", "c":{"d":"ghi"}}
     * @param json String of json to stream parse
     * @param keys list of dot notattion strings of elements to skip when parsing the input json
     * @param should skip or include what is in the keys HashSet
     */
    public ParsedObject(String json, HashSet<String> keys, SkipLogic should) {
        this.json = json;
        this.jo = new JSONObject();
        this.keys = keys;
        this.should = should;
        this.currentKey = "";
        this.els = new Stack();
        try {
            parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject get() {
        return jo;
    }

    public boolean shouldSkip(String key) {
       return should.skip(key,currentKey,keys);
    }

    private boolean stackIsString() {
        if (els.isEmpty()) return false;
        return els.peek().getClass().equals(String.class);
    }

    private boolean stackIsJSONObject() {
        if (els.isEmpty()) return false;
        return els.peek().getClass().equals(JSONObject.class);
    }

    private boolean stackIsJSONArray() {
        if (els.isEmpty()) return false;
        return els.peek().getClass().equals(JSONArray.class);
    }

    private void trimCurrentKey() {
        if (currentKey.length() > 0 && currentKey.contains(".")) {
            currentKey = currentKey.substring(0,currentKey.lastIndexOf("."));
        } else {
            currentKey = "";
        }
    }

    public void parse() throws IOException {
        if (json == null || "".equals(json)) {
            return;
        }
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(json);
        String field = null;

        JsonToken current = null;

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            return;
        }

        els.push(jo);
        while((current = jp.nextToken()) != null) {
            switch(current) {
                case FIELD_NAME: {
                    String key = jp.getCurrentName();
                    if (shouldSkip(key)) {
                        jp.nextToken();
                        jp.skipChildren();
                    } else {
                        els.push(key);
                    }
                    break;
                }
                case START_OBJECT: {
                    if (stackIsString()) {
                        currentKey = "".equals(currentKey) ? (String) els.peek() : currentKey + "." + (String) els.peek();
                    }
                    els.push(new JSONObject());
                    break;
                }
                case END_OBJECT: {
                    JSONObject tmp = (JSONObject) els.pop();
                    if (!els.isEmpty()) {
                        if (stackIsString()) {
                            trimCurrentKey();
                            field = (String) els.pop();
                            ((JSONObject) els.peek()).put(field, tmp);
                        } else if (stackIsJSONArray()) {
                            ((JSONArray) els.peek()).put(tmp);
                        }
                    }
                    break;
                }
                case START_ARRAY: {
                    currentKey = "".equals(currentKey) ? (String) els.peek() : currentKey + "." + (String) els.peek();
                    els.push(new JSONArray());
                    break;
                }
                case END_ARRAY: {
                    if (!els.isEmpty()) {
                        JSONArray ja = (JSONArray) els.pop();
                        field = (String) els.pop();
                        trimCurrentKey();
                        ((JSONObject) els.peek()).put(field, ja);
                    }
                    break;
                }
                case VALUE_STRING: {
                    if (stackIsString()) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, jp.getValueAsString());
                    } else if (stackIsJSONArray()) {
                        ((JSONArray) els.peek()).put(jp.getValueAsString());
                    }
                    break;
                }
                case VALUE_FALSE: {
                    if (stackIsString()) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, false);
                    } else if (stackIsJSONArray()) {
                        ((JSONArray) els.peek()).put(false);
                    }
                    break;
                }
                case VALUE_TRUE: {
                    if (stackIsString()) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, true);
                    } else if (stackIsJSONArray()) {
                        ((JSONArray) els.peek()).put(true);
                    }
                    break;
                }
                case VALUE_NULL: {
                    if (stackIsString()) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, JSONObject.NULL);
                    } else if (stackIsJSONArray()) {
                        ((JSONArray) els.peek()).put(JSONObject.NULL);
                    }
                    break;
                }
                case VALUE_NUMBER_FLOAT: {
                    if (stackIsString()) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, jp.getValueAsString());
                        //((JSONObject) els.peek()).put(field, jp.getFloatValue());
                    } else if (stackIsJSONArray()) {
                        ((JSONArray) els.peek()).put(jp.getFloatValue());
                    }
                    break;
                }
                case VALUE_NUMBER_INT: {
                    if (stackIsString()) {
                        field = (String) els.pop();
                        ((JSONObject) els.peek()).put(field, jp.getValueAsString());
                        //((JSONObject) els.peek()).put(field, jp.getLongValue());
                    } else if (stackIsJSONArray()) {
                        ((JSONArray) els.peek()).put(jp.getLongValue());
                    }
                    break;
                }
            }
        }
    }
}
