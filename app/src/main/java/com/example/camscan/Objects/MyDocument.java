package com.example.camscan.Objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class MyDocument {
    @PrimaryKey(autoGenerate = true)
    private int did;
    @ColumnInfo(name="dname")
    public String dName;
    @ColumnInfo(name="timeCreated")
    private long timeCreated;
    @ColumnInfo(name="timeEdited")
    private long timeEdited;
    @ColumnInfo(name="fp_uri")
    public String fP_URI;

    @Ignore
    public MyDocument(String dName, long timeCreated, long timeEdited,String fP_URI) {
        this.dName = dName;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
        this.fP_URI = fP_URI;
    }

    public MyDocument() {
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }


    public String getdName() {
        return dName;
    }

    public void setdName(String dName) {
        this.dName = dName;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getTimeEdited() {
        return timeEdited;
    }

    public void setTimeEdited(long timeEdited) {
        this.timeEdited = timeEdited;
    }


    public String getfP_URI() {
        return fP_URI;
    }

    public void setfP_URI(String fP_URI) {
        this.fP_URI = fP_URI;
    }
}
