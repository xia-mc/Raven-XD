package keystrokesmod.script.classes;

import com.google.gson.JsonParser;
import keystrokesmod.utility.URLUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

public class Response {
    private final HttpURLConnection connection;

    protected Response(HttpURLConnection connection) {
        this.connection = connection;
    }

    public int code() {
        try {
            return this.connection.getResponseCode();
        }
        catch (IOException e) {
            return 0;
        }
    }

    public String string() {
        return URLUtils.getTextFromConnection(this.connection);
    }

    public Json json() {
        return new Json((new JsonParser()).parse(URLUtils.getTextFromConnection(this.connection)).getAsJsonObject(), 0);
    }
}
