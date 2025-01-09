package io.process4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.json.pointer.JsonPointer;

public class Pointers
{
   // Note! Pattern will also find process data pointers
   private static final Pattern PROCESSDATA_PTR_PATTERN = Pattern.compile("//\\S*");
   private static final Pattern BUSINESSDATA_PTR_PATTERN = Pattern.compile("/\\S*");

   private final List<JsonPointer> processDataPointers = new ArrayList<>();
   private final List<JsonPointer> businessDataPointers = new ArrayList<>();

   Pointers(final String input)
   {
      final List<String> pdGroups = new ArrayList<>();

      // Find all process data pointers
      final Matcher processDataPointerMatcher = PROCESSDATA_PTR_PATTERN.matcher(input);
      while (processDataPointerMatcher.find())
      {
         final String group = processDataPointerMatcher.group();
         pdGroups.add(group);
         // Note! These pointers will start with double slash //
         this.processDataPointers.add(JsonPointer.from(group));
      }

      // Find all business data pointers
      final Matcher businessDataPointerMatcher = BUSINESSDATA_PTR_PATTERN.matcher(input);
      while (businessDataPointerMatcher.find())
      {
         final String group = businessDataPointerMatcher.group();
         // Note! Discard the false positives
         if (!pdGroups.contains(group))
         {
            this.businessDataPointers.add(JsonPointer.from(group));
         }
      }
   }

   public List<JsonPointer> getProcessDataPointers()
   {
      return this.processDataPointers;
   }

   public List<JsonPointer> getBusinessDataPointers()
   {
      return this.businessDataPointers;
   }

   public int size()
   {
      return this.processDataPointers.size() + this.businessDataPointers.size();
   }
}
