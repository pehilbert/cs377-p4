package com.peter.project4.event

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.peter.project4.data.EventDatabaseHelper

class EventProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.peter.eventprovider"
        const val EVENTS_TABLE = "events"
        val EVENTS_URI: Uri = Uri.parse("content://$AUTHORITY/$EVENTS_TABLE")
    }

    private lateinit var database: SQLiteDatabase

    override fun onCreate(): Boolean {
        val dbHelper = EventDatabaseHelper(context!!)
        database = dbHelper.writableDatabase
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return database.query(EVENTS_TABLE, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = database.insert(EVENTS_TABLE, null, values)
        return ContentUris.withAppendedId(EVENTS_URI, id)
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?
    ): Int {
        return database.update(EVENTS_TABLE, values, selection, selectionArgs)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return database.delete(EVENTS_TABLE, selection, selectionArgs)
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/$EVENTS_TABLE"
}
