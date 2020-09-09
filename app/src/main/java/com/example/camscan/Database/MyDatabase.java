package com.example.camscan.Database;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
@Database(entities = {MyPicture.class, MyDocument.class},version = 1,exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {

    private static volatile MyDatabase INSTANCE;

    public abstract MyPictureDao myPicDao();

    public abstract MyDocumentDao myDocumentDao();

    public static MyDatabase getInstance(Context context){
        if(INSTANCE==null){
            synchronized (MyDatabase.class){
                if(INSTANCE==null){
                    INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                            MyDatabase.class,"CamScanDatabase")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    }
