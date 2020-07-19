package com.example.camscan.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.camscan.Objects.MyPicture;

import java.util.List;

@Dao
public interface MyPictureDao {
    @Query("SELECT * FROM mypicture Where did=:did order by position")
    List<MyPicture> getDocPics(int did);

    @Query("SELECT * from mypicture where pid=:pid")
    MyPicture getPicWithPid(int pid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIntoDoc(MyPicture pic);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] InsertMultiplePics(MyPicture... pics);

    @Update
    void updatePic(MyPicture pic);

    @Delete
    void deletePic(MyPicture pic);


}