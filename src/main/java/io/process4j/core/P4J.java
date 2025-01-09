package io.process4j.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class P4J
{
   private static final Logger LOG = Logger.getLogger(P4J.class.getName());

   private static final String P4J_DEFAULT_PROPERTIES_FILENAME = "default.properties";
   private static final String P4J_PROPERTIES_FILENAME = "process4j.properties";

   private static final Properties defaults = new Properties();
   private static final Properties custom = new Properties();

   private static final String PROPERTIES_PREFIX = "process4j.";

   static final String PROPERTIES_MAX_ITERATIONS = PROPERTIES_PREFIX + "loopable.max-iterations";
   static final String PROPERTIES_RULE_VARIABLE_REGEX = PROPERTIES_PREFIX + "rule.variable-regex";

   static
   {
      // Load default properties
      try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(P4J_DEFAULT_PROPERTIES_FILENAME))
      {
         if (is != null)
         {
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8))
            {
               defaults.load(isr);
            }
         }
      }
      catch (final IOException e)
      {
         throw new UncheckedIOException(e);
      }

      // Load custom properties (if any)
      try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(P4J_PROPERTIES_FILENAME))
      {
         if (is != null)
         {
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8))
            {
               custom.load(isr);
            }
         }
      }
      catch (final IOException e)
      {
         throw new UncheckedIOException(e);
      }
   }

   private P4J()
   {
   }

   static Boolean getBoolean(final String name)
   {
      return Boolean.valueOf(P4J.getString(name));
   }

   static Double getDouble(final String name)
   {
      return Double.valueOf(P4J.getString(name));
   }

   static Float getFloat(final String name)
   {
      return Float.valueOf(P4J.getString(name));
   }

   static Long getLong(final String name)
   {
      return Long.valueOf(P4J.getString(name));
   }

   static Integer getInteger(final String name)
   {
      return Integer.valueOf(P4J.getString(name));
   }

   static String getString(final String name)
   {
      final String defaultValue = defaults.getProperty(name);

      if (defaultValue == null)
      {
         LOG.warning(String.format("Ignoring unknown property %s", name));
         return null;
      }

      String value = null;
      try
      {

         value = System.getProperty(name);

         if (value == null)
         {
            value = System.getenv(name);
         }

      }
      catch (final SecurityException e)
      {
         LOG.warning(String.format("A %s occured while getting the property %s. Defaulting to %s", e.getClass().getName(), name, defaultValue));
      }

      if (value == null)
      {
         value = custom.getProperty(name);
      }

      return value == null ? defaultValue : value;
   }
}
