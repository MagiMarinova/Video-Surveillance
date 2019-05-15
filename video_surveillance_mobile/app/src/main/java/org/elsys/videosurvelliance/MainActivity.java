package org.elsys.videosurvelliance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Main activity for video survelliance project
 *
 *
 * @author  Magrgarita Marinova
 * @version 1.0
 * @since   2019-03-25
 */

    public class MainActivity extends AppCompatActivity {


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Get references of UI elements
            final Button loginButton =(Button) findViewById(R.id.loginButton);
            final Switch saveCredentialsButton = (Switch)findViewById(R.id.saveCredentials);
            final EditText usernameField = (EditText)findViewById(R.id.usernameField);
            final EditText passwordField = (EditText)findViewById(R.id.passwordField);
            final EditText addressField= (EditText)findViewById(R.id.addressField);
            //


            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Get element value
                    final String username = usernameField.getText().toString();
                    final String password = passwordField.getText().toString();
                    final String address = addressField.getText().toString();
                    //

                    if(saveCredentialsButton.isChecked()){
                        saveCredentials(address , username , password);
                    }

                    loginRequest("http://" + address + ":3000/androidLogin", username, password, new Runnable() {
                        @Override
                        public void run() {

                            //Pass the address value to the next activity
                            Intent intent = new Intent(MainActivity.this , VideoStream.class);
                            intent.putExtra("ADDRESS" , address);
                            startActivity(intent);
                            //
                        }
                    });
                }
            });



            //Check if credentials are saved
            if(isCredentialsSaved()){
                final JsonObject credentials = readCredentials();

                //auto-fill the credentials
                usernameField.setText(credentials.get("username").toString().replace("\"" , ""));
                passwordField.setText(credentials.get("password").toString().replace("\"" , ""));
                addressField.setText(credentials.get("address").toString().replace("\"" , ""));
                //
            }
            //
        }


    /**
     * Method for making a request to the given server
     * The server responds with a boolean according to the given credentials
     *
     *
     * @param url
     * @param username
     * @param password
     * @param onSuccess - callback for when the server responds with success
     */

    private void loginRequest(String url , String username , String password , final Runnable onSuccess){
        //Buffer JSON object to be send
        JsonObject json = new JsonObject();
        json.addProperty("username" , username);
        json.addProperty("password" , password);
        //

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        Ion.with(this).load(url).setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if(e !=null){
                            System.out.println(e);
                            new AlertDialog.Builder(MainActivity.this).setTitle("Network error!")
                                    .setMessage("Server is unreachable!").setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                            return;
                        }
                        if(result.get("isSuccessful").getAsBoolean()){
                            onSuccess.run();
                        }else{
                            new AlertDialog.Builder(MainActivity.this).setTitle("Wrong credentials!")
                                    .setMessage("Please check you username and password!").setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }
                    }
                });
    }

        /**
         * Method for writing credentials to a file
         *
         *
         * @param url
         * @param username
         * @param password
         */
        private void saveCredentials(String url , String username , String password){
            JsonObject json = new JsonObject();
            json.addProperty("address" , url);
            json.addProperty("username" , username);
            json.addProperty("password" , password);

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("credentials.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(json.toString());
                outputStreamWriter.close();
            }
            catch (IOException e) {
                System.err.println("File write failed: " + e.toString());
            }
        }

        /**
         * Method for reading the saved credentials from a file
         *
         *
         * @return {JsonObject}
         */

        private JsonObject readCredentials(){
            JsonParser parser= new JsonParser();
            try {
                FileInputStream fis = openFileInput("credentials.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                //Return a JSON object
                return parser.parse(sb.toString()).getAsJsonObject();
                //
            } catch (FileNotFoundException fileNotFound) {
                return null;
            } catch (IOException ioException) {
                return null;
            }
        }

        /**
         * Method for checking if credentials are already saved
         *
         *
         * @return {boolean}
         */

        private boolean isCredentialsSaved() {
            String path = getFilesDir().getAbsolutePath() + "/credentials.txt";
            File file = new File(path);
            return file.exists();
        }

    }
