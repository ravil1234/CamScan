package com.example.camscan.Objects;

import android.graphics.Bitmap;
import android.graphics.Point;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import static androidx.room.ForeignKey.CASCADE;

@Entity
public class MyPicture {
    @PrimaryKey(autoGenerate = true)
    private int pid;
    @ForeignKey(entity = MyDocument.class,parentColumns = "did",childColumns = "did",onDelete = CASCADE)
    private int did;
    @ColumnInfo(name = "originalUri")
    private String originalUri;
    @ColumnInfo(name="editedURI")
    private String editedUri;
    @ColumnInfo(name ="editedName")
    private String editedName;
    @ColumnInfo(name="x1")
    private int x1;
    @ColumnInfo(name="x2")
    private int x2;
    @ColumnInfo(name="x3")
    private int x3;
    @ColumnInfo(name="x4")
    private int x4;
    @ColumnInfo(name="y1")
    private int y1;
    @ColumnInfo(name="y2")
    private int y2;
    @ColumnInfo(name="y3")
    private int y3;
    @ColumnInfo(name="y4")
    private int y4;
    @ColumnInfo(name = "position")
    private int position;

    @ColumnInfo(name= "s_width")
    private int s_width;

    @ColumnInfo(name= "s_height")
    private int s_height;


    public MyPicture() {
    }


    public MyPicture(int did,String originalUri, String editedUri, String editedName, int position, ArrayList<Point> coordinates,int width,int height) {
        this.originalUri = originalUri;
        this.editedUri = editedUri;
        this.editedName = editedName;
        this.position = position;
        setCoordinates(coordinates);
        s_width=width;
        s_height=height;
        this.did=did;
    }

    public int getS_width() {
        return s_width;
    }

    public void setS_width(int s_width) {
        this.s_width = s_width;
    }

    public int getS_height() {
        return s_height;
    }

    public void setS_height(int s_height) {
        this.s_height = s_height;
    }

    public void setCoordinates(ArrayList<Point> pts){
        if(pts==null){
            x1=x2=x3=x4=y1=y2=y3=y4=0;
            return;
        }
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
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public String getOriginalUri() {
        return originalUri;
    }

    public void setOriginalUri(String originalUri) {
        this.originalUri = originalUri;
    }

    public String getEditedUri() {
        return editedUri;
    }

    public void setEditedUri(String editedUri) {
        this.editedUri = editedUri;
    }

    public String getEditedName() {
        return editedName;
    }

    public void setEditedName(String editedName) {
        this.editedName = editedName;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public void setX3(int x3) {
        this.x3 = x3;
    }

    public void setX4(int x4) {
        this.x4 = x4;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public void setY3(int y3) {
        this.y3 = y3;
    }

    public void setY4(int y4) {
        this.y4 = y4;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getX3() {
        return x3;
    }

    public int getX4() {
        return x4;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public int getY3() {
        return y3;
    }

    public int getY4() {
        return y4;
    }
}
