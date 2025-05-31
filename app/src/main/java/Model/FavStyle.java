package Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;

import static Model.DatabaseHelper.*;

public class FavStyle implements Serializable {

    private long id;
    private String name;
    private Ulocation ulocation;
    private Plocation plocation;
    private Ilocation ilocation;
    private DatabaseHelper dbHelper;

    public FavStyle() {}

    public FavStyle(long id, String name, Ulocation ulocation, Plocation plocation, Ilocation ilocation) {
        this.id = id;
        this.name = name;
        this.ulocation = ulocation;
        this.plocation = plocation;
        this.ilocation = ilocation;
    }

    public FavStyle(String name, Ulocation ulocation, Plocation plocation, Ilocation ilocation) {
        this.name = name;
        this.ulocation = ulocation;
        this.plocation = plocation;
        this.ilocation = ilocation;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() {
        return (name != null && name.length() > 1) ? name : "";
    }

    public void setName(String name) { this.name = name; }

    public Ulocation getUlocation() { return ulocation; }
    public void setUlocation(Ulocation ulocation) { this.ulocation = ulocation; }

    public Plocation getPlocation() { return plocation; }
    public void setPlocation(Plocation plocation) { this.plocation = plocation; }

    public Ilocation getIlocation() { return ilocation; }
    public void setIlocation(Ilocation ilocation) { this.ilocation = ilocation; }

    public void loadFavStyle(long favStyleId, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                COLUMN_FAV_STYLE_ID,
                COLUMN_FAV_STYLE_NAME,
                COLUMN_FAV_STYLE_ULOCATION,
                COLUMN_FAV_STYLE_PLOCATION,
                COLUMN_FAV_STYLE_ILOCATION
        };
        String selection = COLUMN_FAV_STYLE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(favStyleId) };

        Cursor cursor = db.query(TABLE_FAV_STYLES, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_NAME));
            long uLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_ULOCATION));
            long pLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_PLOCATION));
            long iLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_ILOCATION));

            Ulocation u = new Ulocation(); u.loadUlocation(uLocationId, context);
            Plocation p = new Plocation(); p.loadPlocation(pLocationId, context);
            Ilocation i = new Ilocation(); i.loadIlocation(iLocationId, context);

            this.setId(id);
            this.setName(name);
            this.setUlocation(u);
            this.setPlocation(p);
            this.setIlocation(i);
        }
        cursor.close();
    }

    public void update(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FAV_STYLE_NAME, this.name != null ? this.name : "");
        values.put(COLUMN_FAV_STYLE_ULOCATION, this.ulocation != null ? this.ulocation.getId() : null);
        values.put(COLUMN_FAV_STYLE_PLOCATION, this.plocation != null ? this.plocation.getId() : null);
        values.put(COLUMN_FAV_STYLE_ILOCATION, this.ilocation != null ? this.ilocation.getId() : null);

        db.update(TABLE_FAV_STYLES, values, COLUMN_FAV_STYLE_ID + " = ?", new String[]{String.valueOf(this.id)});
    }

    public void remove(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FAV_STYLE_NAME, "");
        values.put(COLUMN_FAV_STYLE_ULOCATION, "");
        values.put(COLUMN_FAV_STYLE_PLOCATION, "");
        values.put(COLUMN_FAV_STYLE_ILOCATION, "");

        db.update(TABLE_FAV_STYLES, values, COLUMN_FAV_STYLE_ID + " = ?", new String[]{String.valueOf(this.id)});
    }

    public boolean isFsEmpty() {
        return (name == null || name.isEmpty()) &&
                (ulocation == null || ulocation.getName() == null || ulocation.getName().isEmpty()) &&
                (plocation == null || plocation.getName() == null || plocation.getName().isEmpty()) &&
                (ilocation == null || ilocation.getName() == null || ilocation.getName().isEmpty());
    }
}