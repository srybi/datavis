package de.th.ro.datavis.database.converter;


import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.constants.InterpretationMode;

public class Converter {

        @TypeConverter
        public static ArrayList<Sphere> fromString(String value) {
            Type listType = new TypeToken<List<Sphere>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayList(List<Sphere> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }

    /**
     * Convert InterpretationMode to an integer
     */
    @TypeConverter
    public static int fromIntModeToInt(InterpretationMode value) {
        return value.ordinal();
    }

    /**
     * Convert an integer to InterpretationMode
     */
    @TypeConverter
    public static InterpretationMode fromIntToIntMode(int value) {
        return InterpretationMode.values()[value];
    }
}
