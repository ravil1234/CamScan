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
    @ColumnInfo(name="pCount")
    public int pCount;
    @ColumnInfo(name="fp_uri")
    public String fP_URI;
    @ColumnInfo(name="pdf_uri")
    public String pdf_uri;

    @Ignore
    public MyDocument(String dName, long timeCreated, long timeEdited, int pCount, String fP_URI) {
        this.dName = dName;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
        this.pCount = pCount;
        this.fP_URI = fP_URI;
        pdf_uri=null;
    }

    public MyDocument() {
    }

    public String getPdf_uri() {
        return pdf_uri;
    }

    public void setPdf_uri(String pdf_uri) {
        this.pdf_uri = pdf_uri;
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

    public int getpCount() {
        return pCount;
    }

    public void setpCount(int pCount) {
        this.pCount = pCount;
    }

    public String getfP_URI() {
        return fP_URI;
    }

    public void setfP_URI(String fP_URI) {
        this.fP_URI = fP_URI;
    }
}
