package me.vickychijwani.thrones.network.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class HboSynopsisId {

    private int id = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static class Deserializer implements JsonDeserializer<HboSynopsisId> {
        @Override
        public HboSynopsisId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            HboSynopsisId hboSynopsisId = new HboSynopsisId();
            JsonArray sections = json.getAsJsonObject().getAsJsonArray("sections");
            boolean found = false;
            outerloop:
            for (int i = 0; i < sections.size(); ++i) {
                JsonObject section = sections.get(i).getAsJsonObject();
                if (section.has("data")) {
                    JsonObject sectionData = section.getAsJsonObject("data");
                    if (sectionData.has("marquee")) {
                        JsonObject marquee = sectionData.getAsJsonObject("marquee");
                        if (marquee.has("cta")) {
                            JsonArray cta = marquee.getAsJsonArray("cta");
                            for (int j = 0; j < cta.size(); ++j) {
                                JsonObject obj = cta.get(j).getAsJsonObject();
                                if (obj.has("type") && "article".equals(obj.get("type").getAsString())) {
                                    hboSynopsisId.setId(obj.getAsJsonObject("takeover").get("id").getAsInt());
                                    found = true;
                                    break outerloop;
                                }
                            }
                        }
                    }
                }
            }
            if (! found) {
                throw new JsonParseException("Required field not found at path sections[].data.marquee.cta[].takeover.id");
            }
            return hboSynopsisId;
        }
    }

}
