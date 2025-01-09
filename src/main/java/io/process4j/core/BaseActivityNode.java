package io.process4j.core;

public abstract class BaseActivityNode<T> extends BaseNode<T>
{
   private static final String ID_PREFIX = "Activity_";

   @Override
   final String getIdPrefix()
   {
      return ID_PREFIX;
   }

   final Flow getExit()
   {
      return this.exits.values().isEmpty() ? null : this.exits.values().iterator().next();
   }
}
