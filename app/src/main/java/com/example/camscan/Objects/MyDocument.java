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
    private String dname;
    @ColumnInfo(name="timeCreated")
    private long timeCreated;
    @ColumnInfo(name="timeEdited")
    private long timeEdited;
    @ColumnInfo(name="fp_uri")
    private String fp_uri;

    @Ignore
    public MyDocument(String dName, long timeCreated, long timeEdited,String fP_URI) {
        this.dname = dName;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
        this.fp_uri = fP_URI;
    }

    public MyDocument() {
    }



    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }


    public String getDname() {
        return dname;
    }

    public void setDname(String dName) {
        this.dname = dName;
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


    public String getFp_uri() {
        return fp_uri;
    }

    public void setFp_uri(String fP_URI) {
        this.fp_uri = fP_URI;
    }
}
