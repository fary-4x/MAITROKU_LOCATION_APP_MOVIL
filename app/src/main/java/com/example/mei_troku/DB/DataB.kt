package com.example.mei_troku.DB

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataB(context: Context) : SQLiteOpenHelper(
    context,
    "places",
    null,
    1
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table places(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lat REAL, " +
                "lon REAL," +
                "img BLOB)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS places")
    }
}