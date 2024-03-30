package com.example.hackathon_test_2;

public class ChatMessage {
    private String message;
    private boolean isUserMessage; // true if user's message, false if AI's response

    public ChatMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }
}

