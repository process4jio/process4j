package io.process4j.core;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.process4j.core.annotations.Id;
import io.process4j.core.annotations.Implementation;
import io.process4j.core.annotations.Name;

public abstract class BaseNode<T>
{
   final Map<String, Flow> exits = new LinkedHashMap<>();
   boolean initialized;
   T impl;

   public final T getImpl()
   {
      return this.impl;
   }

   @SuppressWarnings("unchecked")
   public final <Q extends BaseNode<T>> Q setImpl(final T impl)
   {
      this.impl = impl;
      return (Q) this;
   }

   public final String getId()
   {
      final Id id = this.getClass().getDeclaredAnnotation(Id.class);
      return id == null ? this.getIdPrefix() + UUID.nameUUIDFromBytes(this.getName().getBytes(StandardCharsets.UTF_8)).toString().substring(0, 7) : id.value();
   }

   public final String getName()
   {
      final Name name = this.getClass().getDeclaredAnnotation(Name.class);
      return name == null ? this.getClass().getSimpleName() : name.value();
   }

   private final String getImplementation()
   {
      final Implementation impl = this.getClass().getDeclaredAnnotation(Implementation.class);
      return impl == null ? null : impl.value();
   }

   abstract String getIdPrefix();

   @SuppressWarnings("unchecked")
   final T newImpl() throws InitializationException
   {
      final String className = this.getImplementation();
      if (className == null)
      {
         return null;
      }
      Class<?> clazz;
      try
      {
         clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
         return (T) clazz.getDeclaredConstructor().newInstance();
      }
      catch (InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException e)
      {
         throw new InitializationException(String.format(InitializationException.MESSAGE, this.getName()), e);
      }
   }

   void doInit() throws InitializationException
   {
      if (this.impl == null)
      {
         this.impl = this.newImpl();
      }
   }

   final BaseNode<T> init() throws InitializationException
   {
      if (this.initialized)
      {
         return this;
      }

      this.doInit();

      this.initialized = true;

      return this;
   }

   abstract State executeImpl(final State state) throws ExecutionException;

   final synchronized State execute(final State state) throws ExecutionException
   {
      try
      {
         return this.init().executeImpl(state);
      }
      catch (final InitializationException | RuntimeException e)
      {
         throw new ExecutionException(String.format(ExecutionException.MESSAGE, this.getName()), e);
      }
   }
}
