package io.process4j.core;

public class Iteration
{
   private final int value;
   private final Iteration outer;
   private boolean terminated;

   public static Iteration nr(final int value)
   {
      return new Iteration(value, null);
   }

   public static Iteration nr(final int value, final Iteration outer)
   {
      return new Iteration(value, outer);
   }

   private Iteration(final int value, final Iteration outer)
   {
      this.value = value;
      this.outer = outer;
   }

   boolean isTerminated()
   {
      return this.terminated;
   }

   public Iteration terminate()
   {
      this.terminated = true;
      return this;
   }

   public int value()
   {
      return this.value;
   }

   public Iteration outer()
   {
      return this.outer;
   }

   @Override
   public String toString()
   {
      return String.valueOf(this.value);
   }

}
