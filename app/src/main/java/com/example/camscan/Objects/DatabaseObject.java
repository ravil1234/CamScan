package com.example.camscan.Objects;

import android.graphics.Point;

import java.util.ArrayList;

public class DatabaseObject {

    private int pid;
    private String OriginalUri;
    private String OriginalFilename;
    private String editedUri;
    private String editedFilename;
    private int x1,x2,x3,x4,y1,y2,y3,y4;

    public DatabaseObject(String originalUri, String originalFilename, String editedUri,
                          String editedFilename, int x1, int x2, int x3, int x4, int y1, int y2, int y3, int y4) {
        OriginalUri = originalUri;
        OriginalFilename = originalFilename;
        this.editedUri = editedUri;
        this.editedFilename = editedFilename;
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = x4;
        this.y1 = y1;
        this.y2 = y2;
        this.y3 = y3;
        this.y4 = y4;
    }

    public DatabaseObject() {
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getOriginalUri() {
        return OriginalUri;
    }

    public void setOriginalUri(String originalUri) {
        OriginalUri = originalUri;
    }

    public String getOriginalFilename() {
        return OriginalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        OriginalFilename = originalFilename;
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
