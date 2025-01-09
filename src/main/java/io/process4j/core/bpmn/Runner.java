package io.process4j.core.bpmn;

import java.nio.file.Paths;

import io.process4j.core.BaseProcess;
import io.process4j.core.Util;

public class Runner
{
   private static final String USAGE_BPMN2JAVA = "Usage: %s <bpmn-input-file> <output-folder> <stubs> <force> <create>";
   private static final String USAGE_JAVA2BPMN = "Usage: %s <process-fqn> <bpmn-output-folder>";
   private static final String JAVA2BPMN_COMMAND = "java2bpmn";
   private static final String BPMN2JAVA_COMMAND = "bpmn2java";

   public static void main(final String[] args) throws Exception
   {
      if (BPMN2JAVA_COMMAND.equalsIgnoreCase(args[0]))
      {
         if (args.length < 7)
         {
            throw new IllegalArgumentException(String.format(USAGE_BPMN2JAVA, BPMN2JAVA_COMMAND));
         }

         Util.bpmn2java(Paths.get(args[1]), Paths.get(args[2]), System.out, Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
      }

      else if (JAVA2BPMN_COMMAND.equalsIgnoreCase(args[0]))
      {
         if (args.length < 3)
         {
            throw new IllegalArgumentException(String.format(USAGE_JAVA2BPMN, JAVA2BPMN_COMMAND));
         }

         Util.java2bpmn((BaseProcess) Class.forName(args[1]).getConstructor().newInstance(), Paths.get(args[2]), System.out);
      }
   }
}
