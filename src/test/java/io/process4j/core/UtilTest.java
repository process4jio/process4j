package io.process4j.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.sun.codemodel.JClassAlreadyExistsException;

import io.process4j.TestUtil;
import io.process4j.core.annotations.Documentation;
import io.process4j.core.annotations.Foreach;
import io.process4j.core.annotations.Id;
import io.process4j.core.annotations.Implementation;
import io.process4j.core.annotations.Name;
import io.process4j.core.annotations.Rules;
import io.process4j.core.annotations.While;
import io.vertx.core.json.JsonObject;

class UtilTest
{
   private static final String JAVA_OUTPUT_FOLDER = "target/generated-sources";
   private static final String BPMN_OUTPUT_FOLDER = "target/generated-resources";

   private static final String JAVA_PROCESS_SOURCE_PATH = "com/acme/";

   @BeforeAll
   static void setUpBeforeClass() throws Exception
   {
   }

   @BeforeEach
   void setUp() throws Exception
   {
   }

   private Path bpmn2Java(final String bpmnFilePath, final String fullPath, final boolean stubs, final boolean force)
         throws IOException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
         NoSuchMethodException, SecurityException, SAXException, JAXBException, JClassAlreadyExistsException
   {
      final Path sPath = Paths.get(JAVA_OUTPUT_FOLDER);
      final Path fPath = Paths.get(JAVA_OUTPUT_FOLDER, fullPath);

      // Delete files and folders
      TestUtil.cleanFiles(fPath);

      // Create source output folder path
      Files.createDirectories(sPath);

      // Get BPMN file path (from test resources)
      final Path processBPMNPath = Paths.get(ClassLoader.getSystemResource(bpmnFilePath).toURI());

      Util.bpmn2java(processBPMNPath, sPath, null, stubs, force, true, false);

      return fPath;
   }

   private Map<String, Class<?>> loadPackage(final Path path, final String pkg) throws IOException
   {
      final Map<String, Class<?>> map = new HashMap<>();
      try (final URLClassLoader classLoader = new URLClassLoader(new URL[]
      {
            Paths.get(JAVA_OUTPUT_FOLDER).toUri().toURL()
      }))
      {
         Files.walk(path, 1).map(Path::toFile).filter(f -> f.getName().endsWith(".class")).map(f -> f.getName().substring(0, f.getName().lastIndexOf("."))).forEach(name -> {
            try
            {
               final String fqn = pkg + "." + name;
               map.put(name, classLoader.loadClass(fqn));
            }
            catch (final ClassNotFoundException e)
            {
               e.printStackTrace();
            }
         });
      }
      return map;
   }

   /**
    * Justering av namn på javaklasser vid modellering av process m.h.a bpmn2java.
    *
    * @result Å, Ä, Ö och å, ä, ö ersätts med A, A, O respektive å, ä, ö. Noder innehållandes flertal ord och inledande gemen ersätts med versal i klassnamnet. Namnen på klasserna får camelcase.
    */
   @Test
   public void testFixJavaClassNameBpmn2Java()
         throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
         SecurityException, URISyntaxException, SAXException, JAXBException, IOException, JClassAlreadyExistsException
   {

      final Path sourcePath = this.bpmn2Java("bpmn/com.acme.fix_names.bpmn", "/com/acme", true, true);

      // Compile sources
      final boolean result = Util.compile(sourcePath);

      Assertions.assertTrue(result);

      final List<String> sourceFiles = Arrays.asList(sourcePath.toFile().list());

      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess.java"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess.class"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess$TaskAaoAAO.class"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess$TaskAaaTask.class"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess$DecisiontableOoo.class"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess$DecisiontableAaadecisiontable.class"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess$ProcessAao.class"));
      Assertions.assertTrue(sourceFiles.contains("FixNamesProcess$GatewayAaoAAO.class"));

      // Load compiled process
      final URLClassLoader classLoader = new URLClassLoader(new URL[]
      {
            Paths.get(JAVA_OUTPUT_FOLDER).toUri().toURL()
      });

      final Class<?> clazzFixNamesProcess = classLoader.loadClass("com.acme.FixNamesProcess");
      final Class<?> clazzTaskAaoAAO = classLoader.loadClass("com.acme.FixNamesProcess$TaskAaoAAO");
      final Class<?> clazzTaskAaaTask = classLoader.loadClass("com.acme.FixNamesProcess$TaskAaaTask");
      final Class<?> clazzDecisiontableOoo = classLoader.loadClass("com.acme.FixNamesProcess$DecisiontableOoo");
      final Class<?> clazzDecisiontableAaadecisiontable = classLoader.loadClass("com.acme.FixNamesProcess$DecisiontableAaadecisiontable");
      final Class<?> clazzProcessAao = classLoader.loadClass("com.acme.FixNamesProcess$ProcessAao");
      final Class<?> clazzGatewayAaoAAO = classLoader.loadClass("com.acme.FixNamesProcess$GatewayAaoAAO");
      classLoader.close();

      final BaseProcess process1 = (BaseProcess) clazzFixNamesProcess.getConstructor().newInstance();
      final BaseTaskNode task1 = (BaseTaskNode) clazzTaskAaoAAO.getConstructor().newInstance();
      final BaseTaskNode task2 = (BaseTaskNode) clazzTaskAaaTask.getConstructor().newInstance();
      final BaseDecisionTableNode decisiontable1 = (BaseDecisionTableNode) clazzDecisiontableOoo.getConstructor().newInstance();
      final BaseDecisionTableNode decisiontable2 = (BaseDecisionTableNode) clazzDecisiontableAaadecisiontable.getConstructor().newInstance();
      final BaseProcessNode process2 = (BaseProcessNode) clazzProcessAao.getConstructor().newInstance();
      final BaseGatewayNode gateway = (BaseGatewayNode) clazzGatewayAaoAAO.getConstructor().newInstance();

      Assertions.assertEquals("FixNamesProcess", process1.getClass().getSimpleName());
      Assertions.assertEquals("task åäöÅÄÖ", task1.getName());
      Assertions.assertEquals("Task Ååå Task", task2.getName());
      Assertions.assertEquals("decisiontable Ööö", decisiontable1.getName());
      Assertions.assertEquals("decisiontable Ååådecisiontable", decisiontable2.getName());
      Assertions.assertEquals("process Åäö", process2.getName());
      Assertions.assertEquals("gateway åäö ÅÄÖ", gateway.getName());

   }

   @Test
   public void testACMEProcessGeneration() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
         SecurityException, IOException, URISyntaxException, SAXException, JAXBException, JClassAlreadyExistsException
   {

      final Path sourcePath = this.bpmn2Java("bpmn/com.acme.acme.bpmn", "com/acme", true, true);

      // Compile sources
      final boolean result = Util.compile(sourcePath);

      Assertions.assertTrue(result);

      // Assert process files
      final List<String> processFiles = Arrays.asList(sourcePath.toFile().list());
      Assertions.assertEquals(9, processFiles.size());
      Assertions.assertTrue(processFiles.contains("ACMEProcess.java"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess.class"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess$DecideType.class"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess$Type.class"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess$HandleA.class"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess$CompleteTx.class"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess$TerminateForeachEarly.class"));
      Assertions.assertTrue(processFiles.contains("ACMEProcess$TerminateWhileEarly.class"));
      Assertions.assertTrue(processFiles.contains("impl"));

      // Assert impl files
      final Path implSourcePath = Paths.get(sourcePath.toString(), "impl");
      final List<String> implFiles = Arrays.asList(implSourcePath.toFile().list());
      Assertions.assertEquals(10, implFiles.size());
      Assertions.assertTrue(implFiles.contains("DecideType.java"));
      Assertions.assertTrue(implFiles.contains("DecideType.class"));
      Assertions.assertTrue(implFiles.contains("Type.java"));
      Assertions.assertTrue(implFiles.contains("Type.class"));
      Assertions.assertTrue(implFiles.contains("HandleA.java"));
      Assertions.assertTrue(implFiles.contains("HandleA.class"));
      Assertions.assertTrue(implFiles.contains("TerminateForeachEarly.java"));
      Assertions.assertTrue(implFiles.contains("TerminateForeachEarly.class"));
      Assertions.assertTrue(implFiles.contains("TerminateWhileEarly.java"));
      Assertions.assertTrue(implFiles.contains("TerminateWhileEarly.class"));

      // Load compiled process classes
      final Map<String, Class<?>> classes = this.loadPackage(sourcePath, "com.acme");

      // Create new instance
      final BaseProcess acmeProcess = (BaseProcess) classes.get("ACMEProcess").getConstructor().newInstance();

      // Assert some stuff on the process instance
      Assertions.assertEquals(9, acmeProcess.getFlows().size());
      Assertions.assertFalse(acmeProcess.initialized);

      // Assert flows
      final Iterator<Flow> iter = acmeProcess.getFlows().iterator();
      while (iter.hasNext())
      {
         final Flow flow = iter.next();
         final BaseNode<?> target = flow.getTarget();
         final Class<?> targetClass = target == null ? null : target.getClass();
         switch (flow.getName())
         {
            case "START":
               Assertions.assertNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseDecisionTableNode);
               Assertions.assertEquals(classes.get("ACMEProcess$DecideType"), target.getClass());
               Assertions.assertEquals(acmeProcess.getDefaultPosition().getTarget(), target);
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Decide type", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Decide type", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.impl.DecideType", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertEquals("/transactions", targetClass.getDeclaredAnnotation(Foreach.class).expression());
               Assertions.assertFalse(targetClass.getDeclaredAnnotation(Foreach.class).parallel());
               Assertions.assertEquals("rules/acme.json", targetClass.getDeclaredAnnotation(Rules.class).resource());
               Assertions.assertEquals("Decide type", targetClass.getDeclaredAnnotation(Rules.class).key());
               Assertions.assertEquals("Decides (overall) type based on transaction types", targetClass.getDeclaredAnnotation(Documentation.class).value());
               break;
            case "TYPE DECIDED":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseGatewayNode);
               Assertions.assertEquals(classes.get("ACMEProcess$Type"), target.getClass());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Gateway_", target.getIdPrefix());
               Assertions.assertEquals("Type?", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Type?", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.impl.Type", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Foreach.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertEquals("Routes by type", targetClass.getDeclaredAnnotation(Documentation.class).value());
               break;
            case "A":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseTaskNode);
               Assertions.assertEquals(classes.get("ACMEProcess$HandleA"), target.getClass());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Handle A", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Handle A", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.impl.HandleA", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Foreach.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertEquals("Handles A", targetClass.getDeclaredAnnotation(Documentation.class).value());
               break;
            case "A HANDLED":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseProcessNode);
               Assertions.assertEquals(classes.get("ACMEProcess$CompleteTx"), target.getClass());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Complete tx", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Complete tx", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.completetx.CompleteTXProcess", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertEquals("/transactions", targetClass.getDeclaredAnnotation(Foreach.class).expression());
               Assertions.assertFalse(targetClass.getDeclaredAnnotation(Foreach.class).parallel());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertEquals("Complete transaction", targetClass.getDeclaredAnnotation(Documentation.class).value());
               break;
            case "B":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseProcessNode);
               Assertions.assertEquals(classes.get("ACMEProcess$CompleteTx"), target.getClass());
               Assertions.assertEquals(target, acmeProcess.getFlows().stream().filter(f -> "A HANDLED".equals(f.getName())).findAny().get().getTarget());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Complete tx", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Complete tx", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.completetx.CompleteTXProcess", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertEquals("/transactions", targetClass.getDeclaredAnnotation(Foreach.class).expression());
               Assertions.assertFalse(targetClass.getDeclaredAnnotation(Foreach.class).parallel());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertEquals("Complete transaction", targetClass.getDeclaredAnnotation(Documentation.class).value());
               break;
            case "COMPLETED TXS":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseTaskNode);
               Assertions.assertEquals(classes.get("ACMEProcess$TerminateForeachEarly"), target.getClass());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Terminate foreach early", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Terminate foreach early", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.impl.TerminateForeachEarly", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Foreach.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Documentation.class));
               break;
            case "FOREACH TERMINATED EARLY":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseTaskNode);
               Assertions.assertEquals(classes.get("ACMEProcess$TerminateWhileEarly"), target.getClass());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Terminate while early", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Terminate while early", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.impl.TerminateWhileEarly", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Foreach.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Documentation.class));
               break;
            case "WHILE TERMINATED EARLY":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertNull(flow.getTarget());
               break;
            case "OTHER":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertNull(flow.getTarget());
               break;
            default:
               throw new IllegalStateException();
         }
      }
   }

   @Test
   public void testCompleteTXProcessGeneration()
         throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
         SecurityException, IOException, URISyntaxException, SAXException, JAXBException, JClassAlreadyExistsException
   {

      final Path sourcePath = this.bpmn2Java("bpmn/com.acme.completetx.complete-tx.bpmn", "com/acme/completetx", true, true);

      // Compile sources
      final boolean result = Util.compile(sourcePath);

      Assertions.assertTrue(result);

      // Assert process files
      final List<String> processFiles = Arrays.asList(sourcePath.toFile().list());
      Assertions.assertEquals(5, processFiles.size());
      Assertions.assertTrue(processFiles.contains("CompleteTXProcess.java"));
      Assertions.assertTrue(processFiles.contains("CompleteTXProcess.class"));
      Assertions.assertTrue(processFiles.contains("CompleteTXProcess$DecideCurrency.class"));
      Assertions.assertTrue(processFiles.contains("CompleteTXProcess$TruncateAmount.class"));
      Assertions.assertTrue(processFiles.contains("impl"));

      // Assert impl files
      final Path implSourcePath = Paths.get(sourcePath.toString(), "impl");
      final List<String> implFiles = Arrays.asList(implSourcePath.toFile().list());
      Assertions.assertEquals(4, implFiles.size());
      Assertions.assertTrue(implFiles.contains("DecideCurrency.java"));
      Assertions.assertTrue(implFiles.contains("DecideCurrency.class"));
      Assertions.assertTrue(implFiles.contains("TruncateAmount.java"));
      Assertions.assertTrue(implFiles.contains("TruncateAmount.class"));

      // Load compiled process classes
      final Map<String, Class<?>> classes = this.loadPackage(sourcePath, "com.acme.completetx");

      // Create new instance
      final BaseProcess completeTxProcess = (BaseProcess) classes.get("CompleteTXProcess").getConstructor().newInstance();

      // Assert some stuff on the process instance
      Assertions.assertEquals(3, completeTxProcess.getFlows().size());
      Assertions.assertFalse(completeTxProcess.initialized);

      // Assert flows
      final Iterator<Flow> iter = completeTxProcess.getFlows().iterator();
      while (iter.hasNext())
      {
         final Flow flow = iter.next();
         final BaseNode<?> target = flow.getTarget();
         final Class<?> targetClass = target == null ? null : target.getClass();
         switch (flow.getName())
         {
            case "START":
               Assertions.assertNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseDecisionTableNode);
               Assertions.assertEquals(classes.get("CompleteTXProcess$DecideCurrency"), target.getClass());
               Assertions.assertEquals(completeTxProcess.getDefaultPosition().getTarget(), target);
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Decide currency", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Decide currency", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.completetx.impl.DecideCurrency", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Foreach.class));
               Assertions.assertEquals("rules/complete-tx.json", targetClass.getDeclaredAnnotation(Rules.class).resource());
               Assertions.assertEquals("Decide currency", targetClass.getDeclaredAnnotation(Rules.class).key());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Documentation.class));
               break;
            case "CURRENCY DECIDED":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertTrue(target instanceof BaseTaskNode);
               Assertions.assertEquals(classes.get("CompleteTXProcess$TruncateAmount"), target.getClass());
               Assertions.assertNotNull(target.getId());
               Assertions.assertEquals("Activity_", target.getIdPrefix());
               Assertions.assertEquals("Truncate amount", target.getName());
               Assertions.assertNull(target.getImpl());

               Assertions.assertNotNull(targetClass.getDeclaredAnnotation(Id.class));
               Assertions.assertEquals("Truncate amount", targetClass.getDeclaredAnnotation(Name.class).value());
               Assertions.assertEquals("com.acme.completetx.impl.TruncateAmount", targetClass.getDeclaredAnnotation(Implementation.class).value());
               Assertions.assertNull(targetClass.getDeclaredAnnotation(While.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Foreach.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Rules.class));
               Assertions.assertNull(targetClass.getDeclaredAnnotation(Documentation.class));
               break;
            case "AMOUNT TRUNCATED":
               Assertions.assertNotNull(flow.getSource());
               Assertions.assertNull(flow.getTarget());
               break;
            default:
               throw new IllegalStateException();
         }
      }
   }

   @Test
   void testCompleteTxProcessExecution() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
         SecurityException, IOException, URISyntaxException, SAXException, JAXBException, JClassAlreadyExistsException, InitializationException, ExecutionException
   {
      final Path sourcePath = this.bpmn2Java("bpmn/com.acme.completetx.complete-tx.bpmn", "com/acme/completetx", true, true);

      // Compile sources
      final boolean result = Util.compile(sourcePath);

      Assertions.assertTrue(result);

      // Load compiled process classes
      final Map<String, Class<?>> classes = this.loadPackage(sourcePath, "com.acme.completetx");
      // final Map<String, Class<?>> implClasses = this.loadPackage(Paths.get(JAVA_OUTPUT_FOLDER, "com/acme/completetx/impl"), "com.acme.completetx.impl");

      // Create new instance
      final BaseProcess completeTxProcess = (BaseProcess) classes.get("CompleteTXProcess").getConstructor().newInstance();

      Assertions.assertFalse(completeTxProcess.initialized);
      completeTxProcess.getFlows().stream().filter(flow -> flow.getTarget() != null).forEach(flow -> Assertions.assertFalse(flow.getTarget().initialized));

      completeTxProcess.init();

      Assertions.assertTrue(completeTxProcess.initialized);
      completeTxProcess.getFlows().stream().filter(flow -> flow.getTarget() != null).forEach(flow -> Assertions.assertTrue(flow.getTarget().initialized));

      final JsonObject businessData = TestUtil.getJsonObjectResource("data/businessdata.02.json");

      final Execution execution = completeTxProcess.execute(businessData, new ProcessData());

      Assertions.assertEquals("START:0 -> CURRENCY DECIDED:0 -> AMOUNT TRUNCATED:0", execution.getPath());
      final JsonObject expectedBusinessData = TestUtil.getJsonObjectResource("data/expected/businessdata/io.process4j.core.UtilTest.testCompleteTxProcessExecution.json");
      Assertions.assertEquals(expectedBusinessData, execution.getCompletionState().getBusinessData());
      final JsonObject expectedProcessData = TestUtil.getJsonObjectResource("data/expected/processdata/io.process4j.core.UtilTest.testCompleteTxProcessExecution.json");
      Assertions.assertEquals(expectedProcessData, execution.getCompletionState().getProcessData().getData());
   }

   @Test
   void testACMEProcessExecution() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
         SecurityException, IOException, URISyntaxException, SAXException, JAXBException, JClassAlreadyExistsException, InitializationException, ExecutionException
   {
      final Path sourcePath = this.bpmn2Java("bpmn/com.acme.acme.bpmn", "com/acme", true, true);

      // Compile sources
      final boolean result = Util.compile(sourcePath);

      Assertions.assertTrue(result);

      // Load compiled process classes
      final Map<String, Class<?>> classes = this.loadPackage(sourcePath, "com.acme");
      // final Map<String, Class<?>> implClasses = this.loadPackage(Paths.get(JAVA_OUTPUT_FOLDER, "com/acme/impl"), "com.acme.impl");

      // Create new instance
      final BaseProcess acmeProcess = (BaseProcess) classes.get("ACMEProcess").getConstructor().newInstance();

      Assertions.assertFalse(acmeProcess.initialized);
      acmeProcess.getFlows().stream().filter(flow -> flow.getTarget() != null).forEach(flow -> Assertions.assertFalse(flow.getTarget().initialized));

      acmeProcess.init();

      Assertions.assertTrue(acmeProcess.initialized);
      acmeProcess.getFlows().stream().filter(flow -> flow.getTarget() != null).forEach(flow -> Assertions.assertTrue(flow.getTarget().initialized));

      final JsonObject businessData = TestUtil.getJsonObjectResource("data/businessdata.02.json");

      final Execution execution = acmeProcess.execute(businessData, new ProcessData());

      Assertions.assertEquals(
            "START:0 -> TYPE DECIDED:0 -> TYPE DECIDED:1 -> TYPE DECIDED:2 -> A:0 -> A HANDLED:0 -> COMPLETED TXS:0 [Complete tx.START:0 -> Complete tx.CURRENCY DECIDED:0 -> Complete tx.AMOUNT TRUNCATED:0] -> COMPLETED TXS:1 [Complete tx.START:1 -> Complete tx.CURRENCY DECIDED:1 -> Complete tx.AMOUNT TRUNCATED:1] -> COMPLETED TXS:2 [Complete tx.START:2 -> Complete tx.CURRENCY DECIDED:2 -> Complete tx.AMOUNT TRUNCATED:2] -> FOREACH TERMINATED EARLY:0 -> FOREACH TERMINATED EARLY:1 -> WHILE TERMINATED EARLY:0 -> WHILE TERMINATED EARLY:1 -> WHILE TERMINATED EARLY:2 -> WHILE TERMINATED EARLY:3 -> WHILE TERMINATED EARLY:4 -> WHILE TERMINATED EARLY:5 -> WHILE TERMINATED EARLY:6 -> WHILE TERMINATED EARLY:7",
            execution.getPath());
      final JsonObject expectedBusinessData = TestUtil.getJsonObjectResource("data/expected/businessdata/io.process4j.core.UtilTest.testACMEProcessExecution.json");
      Assertions.assertEquals(expectedBusinessData, execution.getCompletionState().getBusinessData());
      final JsonObject expectedProcessData = TestUtil.getJsonObjectResource("data/expected/processdata/io.process4j.core.UtilTest.testACMEProcessExecution.json");
      Assertions.assertEquals(expectedProcessData, execution.getCompletionState().getProcessData().getData());

   }

   void printStates(final Execution e, final String indentation)
   {
      final Iterator<State> i = e.getStates().iterator();
      while (i.hasNext())
      {
         final State s = i.next();
         System.out.println(indentation + s.position() + ":" + s.getIteration());
         if (s.getExecution() != null)
         {
            this.printStates(s.getExecution(), indentation + "  ");
         }
      }
   }

}
