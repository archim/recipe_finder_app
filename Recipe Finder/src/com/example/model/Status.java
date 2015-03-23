package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Archi M
 */

public class Status {

    private List<String> messages = new ArrayList<String>();

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }
}