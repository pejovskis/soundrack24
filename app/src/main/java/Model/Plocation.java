package Model;

import static Model.DatabaseHelper.COLUMN_PLOCATION_ID;
import static Model.DatabaseHelper.COLUMN_PLOCATION_NAME;
import static Model.DatabaseHelper.TABLE_PLOCATION;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;

public class Plocation implements Serializable {

    private long id;
    private String name;
    private DatabaseHelper dbHelper;

    public Plocation() {}

    public Plocation(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name.substring(1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void loadPlocation(long pLocationId, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                COLUMN_PLOCATION_ID,
                COLUMN_PLOCATION_NAME
        };
        String selection = COLUMN_PLOCATION_ID + " = ?";
        String[] selectionArgs = { String.valueOf(pLocationId) };

        Cursor cursor = db.query(
                TABLE_PLOCATION,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PLOCATION_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLOCATION_NAME));
            this.setId(id);
            this.setName(name);
        }

        cursor.close();
    }

    public void loadPlocationByName(String name, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {COLUMN_PLOCATION_ID, COLUMN_PLOCATION_NAME};
        String selection = COLUMN_PLOCATION_NAME + " = ?";
        String[] selectionArgs = {name};

        Cursor cursor = db.query(TABLE_PLOCATION, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PLOCATION_ID));
            this.setId(id);
            this.setName(name);
        }

        cursor.close();
    }
}
