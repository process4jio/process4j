package io.process4j.core.infix;

import java.util.HashMap;

import com.fathzer.soft.javaluator.AbstractVariableSet;

public final class Variables implements AbstractVariableSet<String>
{
   private final HashMap<String, String> varToValue;

   public Variables(int size)
   {
      this.varToValue = new HashMap<>(size);
   }

   @Override
   public String get(String variableName)
   {
      return this.varToValue.get(variableName);
   }

   public void set(String variableName, String value)
   {
      this.varToValue.put(variableName, value);
   }

   public void clear()
   {
      this.varToValue.clear();
   }
}
