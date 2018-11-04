package com.example.kayna.irun;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

//https://developer.android.com/reference/android/os/AsyncTask.html
public class GetCoordinatesTask extends AsyncTask<String, Void, Void>{
    public static String DOWNLOAD_COORDINATES_ACTION_NAME = "com.example.kayna.irun.coordinates_downloaded";
    private AppCompatActivity view;
    private Exception exception;
    private ProgressBar progressBar;

    GetCoordinatesTask(AppCompatActivity activity)
    {
        view = activity;
    }
    @Override
    protected Void doInBackground(String... inputParameters) {
        String username = inputParameters[0];
        String password = inputParameters[1];
        System.out.println("do in background");
        JSONObject body = new JSONObject();
        try {
            body.put("email",username); //
            body.put("password", password);//
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
             String response =  doPostJSON("https://api.thingsee.com/v2/accounts/login",body);
            System.out.print("post_response: " + response);
            JSONObject jsonObject = new JSONObject(response);
            String brToken = jsonObject.getString("accountAuthToken");
            System.out.println("brToken: " + brToken);

            String devicesResponse = doGetJSON("https://api.thingsee.com/v2/devices", brToken);
            System.out.println("devicesResponse: " + devicesResponse);
            JSONObject jsonObject1 = new JSONObject(devicesResponse);
            JSONArray devices = jsonObject1.getJSONArray("devices");
            JSONObject firstDevice = (JSONObject) devices.get(0);
            String firstDeviceID = firstDevice.getString("uuid");
            System.out.println("deviceID: " + firstDeviceID);

            String coordinatesJson = doGetJSON("https://api.thingsee.com/v2/events/"+firstDeviceID, brToken);
            System.out.println("CoordinatesJson: " + coordinatesJson);
            JSONObject jsonObject2 = new JSONObject(coordinatesJson);

            List<Location> coordinatesLoc = extractCoordinates(jsonObject2);
            System.out.println("coordinates="+coordinatesLoc);

            Intent intent = new Intent();
            intent.setAction(DOWNLOAD_COORDINATES_ACTION_NAME);
            intent.putExtra("coordinates",new ArrayList(coordinatesLoc) );
// send/broadcast intent
            view.sendBroadcast(intent);



        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent();
            intent.setAction(DOWNLOAD_COORDINATES_ACTION_NAME);
            intent.putExtra("error",e);
// send/broadcast intent
            view.sendBroadcast(intent);
        }
        return null;
    }

    private static String doPostJSON(String url, JSONObject body) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            //send body request
            connection.setDoOutput(true);//https://stackoverflow.com/questions/8587913/what-exactly-does-urlconnection-setdooutput-affect
            connection.getOutputStream().write(body.toString().getBytes("UTF-8"));

            //processing the response
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            InputStream responseStream = connection.getInputStream();
            if (responseStream != null) {
                try {
                    return readStreamIntoString(responseStream);
                } finally {
                    responseStream.close();
                }
            }
            else return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStreamIntoString(InputStream stream)
            throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        int maxReadSize = 100000;
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }


    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    //https://developer.android.com/training/basics/network-ops/connecting.html#download
    private static String doGetJSON(String url, String bearerToken) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true); //true indicates that the application intends to read data from the URL connection (there will be response body)
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer "+bearerToken);

            connection.connect();//// Open communications link (network traffic occurs here).
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                result = readStreamIntoString(stream);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }



    public List<Location> extractCoordinates(JSONObject coordinatesInJSON) throws Exception {
        final int GROUP_LOCATION     = 0x01 << 16;
        final int PROPERTY1          = 0x01 << 8;
        final int PROPERTY2          = 0x02 << 8;
        List<Location>   coordinates = new ArrayList<Location>();
        int    k;
        JSONArray events = coordinatesInJSON.getJSONArray("events");
        try {

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                Location loc   = new Location("ThingseeONE");
                loc.setTime(event.getLong("timestamp"));
                k = 0;
                if(!event.has("cause"))
                {
                    continue;
                }
                JSONObject cause = event.getJSONObject("cause");
                if(!cause.has("senses"))
                {
                    continue;
                }
                JSONArray senses = cause.getJSONArray("senses");
                for (int j = 0; j < senses.length(); j++) {
                    JSONObject sense   = senses.getJSONObject(j);
                    int        senseID = Integer.decode(sense.getString("sId"));
                    double     value   = sense.getDouble("val");


                    switch (senseID) {
                        case GROUP_LOCATION | PROPERTY1:
                            loc.setLatitude(value);
                            k++;
                            break;

                        case GROUP_LOCATION | PROPERTY2:
                            loc.setLongitude(value);
                            k++;
                            break;
                    }

                    if (k == 2) {
                        coordinates.add(loc);
                        k = 0;
                    }
                }
            }
        } catch (Exception e) {
            throw new LocationsProcessingError("Processing coordinates failed", e);
        }

        return coordinates;
    }
}

class LocationsProcessingError extends RuntimeException {
    public LocationsProcessingError( String message , Exception e) {
        super(message,e);
    }





}


