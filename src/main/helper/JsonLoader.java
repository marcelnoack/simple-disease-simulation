package main.helper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonLoader {
    public static JSONObject getData() throws IOException {
        File file = new File("input/params.json");
        BufferedReader br = new BufferedReader(new FileReader(file));

        String temp;
        String jsonString = "";
        while ((temp = br.readLine()) != null) {
            jsonString += temp;
        }

        return new JSONObject(jsonString);
    }
}
