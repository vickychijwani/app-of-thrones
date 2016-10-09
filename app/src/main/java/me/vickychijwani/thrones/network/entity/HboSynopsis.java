package me.vickychijwani.thrones.network.entity;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import me.vickychijwani.thrones.BuildConfig;

public class HboSynopsis {

    private String title;
    private String image;
    private String text;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static class Deserializer implements JsonDeserializer<HboSynopsis> {
        @NonNull
        @Override
        public HboSynopsis deserialize(JsonElement json, Type typeOfT,
                                       JsonDeserializationContext context) throws JsonParseException {
            JsonObject content = json.getAsJsonObject().getAsJsonObject("content");
            HboSynopsis hboSynopsis = new HboSynopsis();
            hboSynopsis.setTitle(content.get("title").getAsString());
            hboSynopsis.setImage(content.get("image").getAsString());
            hboSynopsis.setText(content.get("text").getAsString());

            // subchapters field seems to be always empty, but check just to be sure
            if (BuildConfig.DEBUG && content.getAsJsonArray("subchapters").size() > 0) {
                Log.wtf("Deserializer", "subchapters is not empty! value = "
                        + content.getAsJsonArray("subchapters").toString());
            }

            return hboSynopsis;
        }
    }

}
