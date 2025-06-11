package io.process4j.core;

public class InitializationException extends Exception
{
   static final String MESSAGE = "An execution occured during initialization of node '%s'";
   private static final long serialVersionUID = 1L;

   public InitializationException(final String message, final Throwable cause)
   {
      super(message, cause);
   }
}
