package io.process4j.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;

public final class ProcessData
{
   @JsonIgnore
   private final Map<String, Object> sharedData = new HashMap<>();

   private final JsonObject data;

   public ProcessData()
   {
      this.data = new JsonObject();
   }

   public ProcessData(JsonObject data)
   {
      this.data = data;
   }

   public Map<String, Object> getSharedData()
   {
      return this.sharedData;
   }

   public JsonObject getData()
   {
      return this.data;
   }

   public ProcessData copy()
   {
      final ProcessData copy = new ProcessData(this.data.copy());
      copy.sharedData.putAll(this.sharedData);
      return copy;
   }

   @JsonSerialize
   public Set<String> sharedData()
   {
      return this.sharedData.keySet();
   }
}
