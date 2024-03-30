package com.example.hackathon_test_2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    EditText userInputEditText;
    RecyclerView chatRecyclerView;
    ChatAdapter chatAdapter;
    List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInputEditText = findViewById(R.id.user_input_edit_text);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);

        // Initialize RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Call the API to fetch the initial response
        new FetchInitialResponseTask().execute();
    }

    public void sendRequest(View view) {
        String userInput = userInputEditText.getText().toString();
        userInputEditText.setText(""); // Clear the input field after sending
        new OpenAIRequestTask().execute(userInput);
        new RunRequest().execute();
        Handler handler = new Handler();

        // Post a delayed action after 4 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Execute the ReadResponse AsyncTask
                new ReadResponse().execute();
            }
        }, 10000);
    }


    private class FetchInitialResponseTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";

            try {
                URL url = new URL("https://api.openai.com/v1/threads");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer HERE");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("OpenAI-Beta", "assistants=v1");

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Thread created", s);
//            initialResponse = s;
        }
    }



    private class OpenAIRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String userInput = strings[0];
            String response = "";

            try {
                URL url = new URL("https://api.openai.com/v1/threads/thread_RbcMJ4gAYQQIEWsnjKAUY5RC/messages");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer HERE");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("OpenAI-Beta", "assistants=v1");
                conn.setDoOutput(true);

                JSONObject postData = new JSONObject();
                postData.put("role", "user");
                postData.put("content", userInput);

                conn.getOutputStream().write(postData.toString().getBytes());

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return userInput;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("After adding Message", s);
            // Add the received message to the RecyclerView
            chatMessages.add(new ChatMessage(s, false)); // Assuming 'false' means it's a response from the AI
            chatAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
//            try {
//                JSONObject jsonResponse = new JSONObject(s);
//                String responseText = jsonResponse.getString("response");
//                responseTextView.setText(responseText);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private class RunRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";

            try {
                // URL and connection setup
                URL url = new URL("https://api.openai.com/v1/threads/thread_RbcMJ4gAYQQIEWsnjKAUY5RC/runs");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer HERE");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("OpenAI-Beta", "assistants=v1");
                conn.setDoOutput(true);

                // Writing data to the connection
                JSONObject postData = new JSONObject();
                postData.put("assistant_id", "asst_SR7JU2mins9cExhD279HCnIH");
                postData.put("instructions", "To the questions I ask you, get the answers only from the files I provided and also provide information on from which PDF filename the information is extracted. The filename from which the information is extracted is very important. The response should Have information regarding the query and at the end have a Citation section and give the filename in that place");

                conn.getOutputStream().write(postData.toString().getBytes());

//                 Reading response from the connection (commented out)
                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }

//                 Closing resources
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();
            } catch (IOException | JSONException e) {
                Log.e("After Model", "Got error in adding model to runner");
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("After Model", s);
            // Handle the response here if needed
        }
    }

    private class ReadResponse extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";

            try {
                URL url = new URL("https://api.openai.com/v1/threads/thread_RbcMJ4gAYQQIEWsnjKAUY5RC/messages");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer HERE");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("OpenAI-Beta", "assistants=v1");

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonResponse = new JSONObject(s);
                Log.e("ReadResponse", String.valueOf(jsonResponse));

                // Check if the JSON response contains the "data" array
                if (jsonResponse.has("data")) {
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    // Check if the "data" array is not empty
                    if (dataArray.length() > 0) {
                        JSONObject messageObject = dataArray.getJSONObject(0);

                        // Check if the messageObject contains the "content" array
                        if (messageObject.has("content")) {
                            JSONArray contentArray = messageObject.getJSONArray("content");

                            // Iterate through the contentArray to find text value
                            StringBuilder responseText = new StringBuilder();
                            for (int i = 0; i < contentArray.length(); i++) {
                                JSONObject textObject = contentArray.getJSONObject(i);
                                if (textObject.has("text")) {
                                    JSONObject textValueObject = textObject.getJSONObject("text");
                                    if (textValueObject.has("value")) {
                                        String value = textValueObject.getString("value");
                                        responseText.append(value).append("\n");

                                        // Add the received message to the RecyclerView
                                        chatMessages.add(new ChatMessage(value, true)); // Assuming 'true' means it's a user input
                                        chatAdapter.notifyDataSetChanged();
                                        scrollToBottom();
                                    }
                                }
                            }
                        } else {
                            // Handle the case where "content" key does not exist
                            Log.e("ReadResponse", "JSON message object does not contain 'content' key");
                        }
                    } else {
                        // Handle the case where "data" array is empty
                        Log.e("ReadResponse", "JSON response 'data' array is empty");
                    }
                } else {
                    // Handle the case where "data" key does not exist
                    Log.e("ReadResponse", "JSON response does not contain 'data' key");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing error
            }
        }



    }

    // Method to scroll RecyclerView to the bottom
    private void scrollToBottom() {
        // Scroll to the last item with smooth scrolling
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }

}
