package com.example.camscan.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.camscan.Objects.MyDocument;

import java.util.List;

@Dao
public interface MyDocumentDao {

@Query("SELECT * FROM mydocument")
    List<MyDocument> getAllDocs();

@Query("SELECT * fROM mydocument where did=:did")
    MyDocument getDocumentWithId(int did);

@Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNewDoc(MyDocument doc);

@Update
    void updateDoc(MyDocument doc);

@Delete
    void deleteDoc(MyDocument doc);
}
