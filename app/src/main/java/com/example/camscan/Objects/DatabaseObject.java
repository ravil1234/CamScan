package com.example.camscan.Objects;

import android.graphics.Point;

import java.util.ArrayList;

public class DatabaseObject {

    private int pid;
    private int did;
    private String OriginalUri;
    private String DocName;
    private String editedUri;
    private String editedFilename;
    private int x1,x2,x3,x4,y1,y2,y3,y4;
    private long timeCreated;
    private long timeedited;

    public DatabaseObject(String originalUri, String docName, String editedUri,
                          String editedFilename,ArrayList<Point> points,long timeCreated,long timeedited) {
        OriginalUri = originalUri;
        DocName = docName;
        this.editedUri = editedUri;
        this.editedFilename = editedFilename;

        this.timeCreated=timeCreated;
        this.timeedited=timeedited;
        setCoordinates(points);
    }

    public DatabaseObject() {
    }


    public void setTimeedited(long timeedited) {
        this.timeedited = timeedited;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public long getTimeedited() {
        return timeedited;
    }



    public int getDid() {
        return did;
    }

    public int getPid() {
        return pid;
    }



    public String getOriginalUri() {
        return OriginalUri;
    }

    public void setOriginalUri(String originalUri) {
        OriginalUri = originalUri;
    }

    public String getDocName() {
        return DocName;
    }

    public void setDocName(String docName) {
        DocName = docName;
    }

    public String getEditedUri() {
        return editedUri;
    }

    public void setEditedUri(String editedUri) {
        this.editedUri = editedUri;
    }

    public String getEditedFilename() {
        return editedFilename;
    }

    public void setEditedFilename(String editedFilename) {
        this.editedFilename = editedFilename;
    }

    public void setCoordinates(ArrayList<Point> pts){
        this.x1=pts.get(0).x;
        this.x2=pts.get(1).x;
        this.x3=pts.get(2).x;
        this.x4=pts.get(3).x;

        this.y1=pts.get(0).y;
        this.y2=pts.get(1).y;
        this.y3=pts.get(2).y;
        this.y4=pts.get(3).y;
    }
    public ArrayList<Point> getCoordinates(){
        ArrayList<Point> pts= new ArrayList<Point>();
        pts.add(new Point(x1,y1));
        pts.add(new Point(x2,y2));
        pts.add(new Point(x3,y3));
        pts.add(new Point(x4,y4));
        return pts;
    }
}
