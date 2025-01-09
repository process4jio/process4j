package io.process4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

public abstract class TestUtil
{
   private static final Logger LOG = Logger.getLogger(TestUtil.class.getName());

   private TestUtil()
   {
   }

   public static JsonObject getJsonObjectResource(final String filename) throws URISyntaxException, IOException
   {
      return new JsonObject(Files.readAllLines(Paths.get(ClassLoader.getSystemResource(filename).toURI())).stream()
            .collect(Collectors.joining()));
   }

   public static void cleanFiles(final Path rootPath) throws IOException
   {
      if (!rootPath.toFile().exists())
      {
         return;
      }

      LOG.fine(String.format("Recursive deletion from %s", rootPath.toRealPath()));

      if (!rootPath.toString().contains("target"))
      {
         throw new IllegalArgumentException(String.format("Not allowed to clean outside %s folder!", "target"));
      }

      try (final Stream<Path> walk = Files.walk(rootPath))
      {
         walk.sorted(Comparator.reverseOrder())
               .map(Path::toFile)
               .peek(file -> LOG.fine(String.format("Deleting %s", file)))
               .forEach(File::delete);
      }
   }
}
