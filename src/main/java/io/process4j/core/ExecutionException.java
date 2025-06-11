package io.process4j.core;

public final class ExecutionException extends Exception
{
   static final String MESSAGE = "An execution occured during execution of node '%s'";
   private static final long serialVersionUID = 1L;

   private final Execution execution;

   public ExecutionException(final String message)
   {
      this(message, null, null);
   }

   public ExecutionException(final String message, final Throwable cause)
   {
      this(message, cause, null);
   }

   public ExecutionException(final String message, final Throwable cause, final Execution execution)
   {
      super(message, cause);
      this.execution = execution;
   }

   public Execution getExecution()
   {
      return this.execution;
   }
}
