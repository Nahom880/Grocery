package com.example.nahom.grocery;




import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDatabase.dbname, version = 1)

/** 
 * Creating Database for Grocery
 * created by Nahom on 10/02/15.
 */
public class AppDatabase {

    //creating a variable for the database to access it from another class
    public static final String dbname = "app_database";
}
