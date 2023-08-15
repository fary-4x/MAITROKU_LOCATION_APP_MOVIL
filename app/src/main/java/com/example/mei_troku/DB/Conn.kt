package com.example.mei_troku.DB

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.mei_troku.DataClass.Places

class Conn(context: Context) {
    private val db: SQLiteDatabase = DataB(context).writableDatabase

    fun create(lat: Double, lon: Double, img: ByteArray) {
        val values = ContentValues()
        values.put("lat", lat)
        values.put("lon", lon)
        values.put("img", img)
        db.insert("places", null, values)
    }

    fun get(): MutableList<Places> {
        val placesList = mutableListOf<Places>()

        val cursor = db.rawQuery("SELECT _id, lat, lon, img FROM places", null)
        while (cursor.moveToNext()) {
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"))
            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow("lon"))
            val imgByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow("img"))
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))

            val place = Places(id, lat, lon, imgByteArray)
            placesList.add(place)
        }
        return placesList
    }

    fun getByLatLon(lat: Double, lon: Double): List<Places> {
        val selection = "lat = ? AND lon = ?"
        val selectionArgs = arrayOf(lat.toString(), lon.toString())

        val cursor = db.query("places", arrayOf("_id", "lat", "lon", "img"), selection, selectionArgs, null, null, null)
        val placesList = mutableListOf<Places>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
            val imgByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow("img"))
            val place = Places(id, lat, lon, imgByteArray)
            placesList.add(place)
        }

        cursor.close()
        return placesList
    }

//    fun getByID(id: Int): Places? {
//        val cursor = db.rawQuery("SELECT _id, lat, lon, img FROM places WHERE _id = $id", null)
//        var place: Places? = null
//        while (cursor.moveToNext()) {
//            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"))
//            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow("lon"))
//            val imgByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow("img"))
//            place = Places(id, lat, lon, imgByteArray)
//        }
//        return place
//    }
//
//    fun delete(id: Int): Boolean {
//        val whereClause = "_id = ?"
//        val whereArgs = arrayOf(id.toString())
//        val deletedRows = db.delete("places", whereClause, whereArgs)
//        return deletedRows > 0
//    }
//
//    fun update(id: Int, lat: Double, lon: Double, img: ByteArray): Boolean {
//        val values = ContentValues()
//        values.put("lat", lat)
//        values.put("lon", lon)
//        values.put("img", img)
//        val whereClause = "_id = ?"
//        val whereArgs = arrayOf(id.toString())
//        val updatedRows = db.update("places", values, whereClause, whereArgs)
//        return updatedRows > 0
//    }
}
