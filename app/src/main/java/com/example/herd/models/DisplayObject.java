package com.example.herd.models;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DisplayObject {

    private String type;
    private QuerySnapshot snapshots;

    public DisplayObject(String type, QuerySnapshot snapshots) {
        this.type = type;
        this.snapshots = snapshots;
    }

    public String getType() { return type; }
    public QuerySnapshot getSnapshots() { return snapshots; }

}
