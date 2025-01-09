package io.process4j.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JMods;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JVar;

import io.process4j.core.annotations.Documentation;
import io.process4j.core.annotations.Foreach;
import io.process4j.core.annotations.Generated;
import io.process4j.core.annotations.Id;
import io.process4j.core.annotations.Implementation;
import io.process4j.core.annotations.Name;
import io.process4j.core.annotations.Rules;
import io.process4j.core.annotations.While;
import io.process4j.core.bpmn.annotations.BPMNAnnotation;
import io.process4j.core.bpmn.annotations.BPMNEdge;
import io.process4j.core.bpmn.annotations.BPMNEdgeLabel;
import io.process4j.core.bpmn.annotations.BPMNEndEventLabel;
import io.process4j.core.bpmn.annotations.BPMNEndEventShape;
import io.process4j.core.bpmn.annotations.BPMNNodeLabel;
import io.process4j.core.bpmn.annotations.BPMNNodeShape;
import io.process4j.core.bpmn.annotations.BPMNStartEventLabel;
import io.process4j.core.bpmn.annotations.BPMNStartEventShape;
import io.process4j.core.bpmn.di.Diagram;
import io.process4j.core.bpmn.di.Edge;
import io.process4j.core.bpmn.di.Shape;
import io.process4j.core.bpmn.di.Waypoint;
import io.process4j.core.bpmn.model.BPMNActivity;
import io.process4j.core.bpmn.model.BPMNAssociation;
import io.process4j.core.bpmn.model.BPMNBusinessRuleTask;
import io.process4j.core.bpmn.model.BPMNDefinitions;
import io.process4j.core.bpmn.model.BPMNDocumentation;
import io.process4j.core.bpmn.model.BPMNExclusiveGateway;
import io.process4j.core.bpmn.model.BPMNProcess;
import io.process4j.core.bpmn.model.BPMNSequenceFlow;
import io.process4j.core.bpmn.model.BPMNSubProcess;
import io.process4j.core.bpmn.model.BPMNTask;
import io.process4j.core.bpmn.model.BPMNTextAnnotation;
import io.process4j.core.bpmn.model.NodeElement;
import io.vertx.core.json.JsonObject;

public class Util
{
   private static final Logger LOG = Logger.getLogger(Util.class.getName());

   static final String KEYWORD_FOREACH = "foreach";
   static final String KEYWORD_WHILE = "while";
   static final String KEYWORD_IMPL = "impl";

   private static final String EXPORTING_BPMN_PROCESS_DEFINITIONS = "Exporting BPMN process definitions %s to %s";
   private static final String BPMN_FILENAME_TEMPLATE = "%s.bpmn";
   private static final String HTTP_WWW_W3_ORG_2001_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   private static final String BPMN20_SCHEMA = "schema/BPMN20.xsd";
   private static final String MISSING_FOREACH_ANNOTATION = "Multi Instance Activity Node %s is missing mandatory '%s' documentation";
   private static final String MISSING_WHILE_ANNOTATION = "Loop Activity Node %s is missing mandatory '%s' documentation";

   public static void java2bpmn(final BaseProcess process, final Path outputFolder, final PrintStream ps) throws Exception
   {
      Util.java2bpmn(process, outputFolder, true, true, ps);
   }

   public static void java2bpmn(final BaseProcess process, final Path outputFolder, final boolean validateBySchema, final boolean recursive, final PrintStream ps) throws Exception
   {

      process.init();

      // Get BPMN definitions
      final BPMNDefinitions definitions = BPMN.createBPMNDefinitions(process);

      // Create context and marshaller
      final JAXBContext context = JAXBContext.newInstance(BPMNDefinitions.class);
      final Marshaller marshaller = context.createMarshaller();

      // Set schema (for validation)
      if (validateBySchema)
      {
         final SchemaFactory factory = SchemaFactory.newInstance(HTTP_WWW_W3_ORG_2001_XML_SCHEMA);
         final Schema schema = factory.newSchema(ClassLoader.getSystemResource(BPMN20_SCHEMA));
         marshaller.setSchema(schema);
      }

      // Create directories and get the output file path
      final Path path = Paths.get(Files.createDirectories(outputFolder).toString(), String.format(BPMN_FILENAME_TEMPLATE, definitions.getId()));

      // Export to BPMN file
      marshaller.marshal(definitions, path.toFile());

      if (ps != null)
      {
         ps.println(String.format(EXPORTING_BPMN_PROCESS_DEFINITIONS, definitions.getId(), path.toRealPath()));
      }

      // Export sub processes
      if (recursive)
      {
         for (final BaseProcessNode subProcessNode : process.getFlows().stream().filter(flow -> (flow.getTarget() != null && flow.getTarget() instanceof BaseProcessNode))
               .map(flow -> ((BaseProcessNode) flow.getTarget())).collect(Collectors.toList()))
         {
            Util.java2bpmn(subProcessNode.getImpl(), outputFolder, validateBySchema, recursive, ps);
         }
      }
   }

   // public static void bpmn2java(final Path bpmn, final Path sourceOutputFolder, final PrintStream ps) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
   // IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SAXException, JAXBException, IOException, JClassAlreadyExistsException
   // {
   // Util.bpmn2java(bpmn, sourceOutputFolder, ps, false, false);
   // }

   public static void bpmn2java(final Path bpmn, final Path sourceOutputFolder, final PrintStream ps, final boolean stubs, final boolean force, final boolean create, final boolean debug)
         throws SAXException, JAXBException, ClassNotFoundException, InstantiationException, IllegalAccessException,
         IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, JClassAlreadyExistsException
   {
      // Create context and marshaller
      final JAXBContext context = JAXBContext.newInstance(BPMNDefinitions.class);
      final Unmarshaller unmarshaller = context.createUnmarshaller();

      // Set schema (for validation)
      final SchemaFactory factory = SchemaFactory.newInstance(HTTP_WWW_W3_ORG_2001_XML_SCHEMA);
      final Schema schema = factory.newSchema(ClassLoader.getSystemResource(BPMN20_SCHEMA));
      unmarshaller.setSchema(schema);

      // Import BPMN definitions from BPMN file
      final BPMNDefinitions definitions = unmarshaller.unmarshal(new StreamSource(bpmn.toFile()), BPMNDefinitions.class).getValue();

      // Get BPMN Process
      final BPMNProcess bpmnProcess = definitions.getProcess();

      final String fullyQualifiedProcessClassName = bpmnProcess.getDocumentation().stream().filter(d -> d.getId() != null && ("p4j-implementation-for-" + bpmnProcess.getId()).equals(d.getId()))
            .findAny().get()
            .getText();

      // Get BPMN Diagram
      final Diagram bpmnDiagram = definitions.getDiagram();

      // Create source model
      final JCodeModel model = new JCodeModel();

      // Create Process source
      final JDefinedClass processSrc = model._class(fullyQualifiedProcessClassName);

      // Add target package
      final String targetPackage = fullyQualifiedProcessClassName.substring(0, fullyQualifiedProcessClassName.lastIndexOf("."));
      model._package(targetPackage);

      // Node sources (indexed by corresponding BPMN Element id)
      final Map<String, JDefinedClass> nodesSrc = new HashMap<>();

      for (final NodeElement node : bpmnProcess.getNodes())
      {
         final String nodeClassName = Util.fixName(node.getName());

         // Create node source
         nodesSrc.put(node.getId(), Util.createNodeSrc(processSrc, node, bpmnProcess, bpmnDiagram, nodeClassName));

         // Create node implementation source
         if (stubs)
         {
            Util.createImplSrc(model, node, bpmnProcess, bpmnDiagram, bpmn, Util.getMatchingImplementationInterface(node));
         }
      }

      // Extend BaseProcess
      processSrc._extends(BaseProcess.class);

      // Create default constructor and make super call
      final JMethod constructor = processSrc.constructor(JMod.PUBLIC);
      final JBlock constructorBody = constructor.body();

      // Make super call
      constructorBody.invoke("super"); // only no-args constructor supported!

      // Declare a local variable in the constructor for each process node
      final Map<String, JVar> processNodes = new HashMap<>();
      for (final BPMNSequenceFlow seq : bpmnProcess.getSequenceFlow())
      {
         final JDefinedClass targetNodeSrc = nodesSrc.get(seq.getTargetRef());

         if (targetNodeSrc != null && processNodes.get(seq.getTargetRef()) == null)
         {
            final JInvocation nodeConstructorCall = JExpr._new(targetNodeSrc);
            if (targetNodeSrc.constructors().hasNext())
            {
               for (final JVar param : targetNodeSrc.constructors().next().listParams())
               {
                  if (param.type().isPrimitive())
                  {
                     nodeConstructorCall.arg(Util.getPrimitive(model, (JPrimitiveType) param.type()));
                  }
                  else
                  {
                     nodeConstructorCall.arg(JExpr._null());
                  }
               }
            }
            processNodes.put(seq.getTargetRef(),
                  constructorBody.decl(targetNodeSrc/* model._ref(BaseNode.class) */, targetNodeSrc.name().substring(0, 1).toLowerCase() + targetNodeSrc.name().substring(1), nodeConstructorCall));
         }
      }

      // Connect the process nodes in the constructor
      for (final BPMNSequenceFlow seq : bpmnProcess.getSequenceFlow())
      {
         final JDefinedClass sourceNodeSrc = nodesSrc.get(seq.getSourceRef());
         final JDefinedClass targetNodeSrc = nodesSrc.get(seq.getTargetRef());

         JInvocation inv;
         final JClass flowClass = model.ref(Flow.class);
         if (sourceNodeSrc == null)
         {
            inv = flowClass.staticInvoke("startFlow").arg(seq.getId()).arg(seq.getName()).arg(processNodes.get(seq.getTargetRef()));
         }

         else if (targetNodeSrc == null)
         {
            inv = flowClass.staticInvoke("endFlow").arg(seq.getId()).arg(seq.getName()).arg(processNodes.get(seq.getSourceRef()));
         }

         else
         {
            inv = flowClass.staticInvoke("flow").arg(seq.getId()).arg(seq.getName()).arg(processNodes.get(seq.getSourceRef())).arg(processNodes.get(seq.getTargetRef()));
         }

         constructorBody.add(JExpr._this().invoke("addFlow").arg(inv));

         final Edge edge = bpmnDiagram.getPlane().getEdges().stream().filter(e -> e.getBpmnElement().equals(seq.getId())).findAny().get();

         final List<String> waypointCoordinates = new ArrayList<>();
         for (final Waypoint wp : edge.getWaypoints())
         {
            waypointCoordinates.add(wp.getX() + "," + wp.getY());
         }

         final JAnnotationUse jau = processSrc.annotate(BPMNEdge.class);
         jau.param("flow", seq.getId());
         if (!waypointCoordinates.isEmpty())
         {
            jau.param("waypointCoordinates", waypointCoordinates.stream().collect(Collectors.joining(";")));
         }

         if (edge.getLabel() != null)
         {
            final JAnnotationUse jau2 = processSrc.annotate(BPMNEdgeLabel.class);
            jau2.param("flow", seq.getId());
            jau2.param("x", JExpr.lit(edge.getLabel().getBounds().getX()));
            jau2.param("y", JExpr.lit(edge.getLabel().getBounds().getY()));
            jau2.param("width", JExpr.lit(edge.getLabel().getBounds().getWidth()));
            jau2.param("height", JExpr.lit(edge.getLabel().getBounds().getHeight()));
         }
      }

      Util.annotateWithShapeAndLabel(processSrc, bpmnDiagram, BPMNStartEventShape.class, BPMNStartEventLabel.class, bpmnProcess.getStartEvent());

      Util.annotateWithShapeAndLabel(processSrc, bpmnDiagram, BPMNEndEventShape.class, BPMNEndEventLabel.class, bpmnProcess.getEndEvent().toArray(new NodeElement[bpmnProcess.getEndEvent().size()]));

      Util.annotateWithGenerationDetails(processSrc, bpmn);

      // Create missing output folder
      if (create)
      {
         if (debug && !Files.exists(sourceOutputFolder))
         {
            ps.println(String.format("Creating missing output folder %s", sourceOutputFolder));
         }

         Files.createDirectories(sourceOutputFolder);
      }

      // Build and replace!
      if (force)
      {
         model.build(sourceOutputFolder.toFile(), ps);
      }

      // Build
      else
      {
         build(model, sourceOutputFolder, ps, debug);
      }
   }

   private static void build(final JCodeModel model, final Path destDir, final PrintStream ps, final boolean debug) throws IOException
   {
      final Path tmpDir = Files.createTempDirectory(destDir, "p4j.");

      if (debug)
      {
         ps.println(String.format("Creating tmp dir %s", tmpDir.toString()));
      }

      model.build(tmpDir.toFile(), tmpDir.toFile(), null);

      final List<Path> tmpFiles = Files.walk(tmpDir).map(Path::toFile).filter(File::isFile).map(File::toPath).map(p -> p.subpath(destDir.getNameCount() + 1, p.getNameCount()))
            .collect(Collectors.toList());

      for (final Path tmpFile : tmpFiles)
      {
         final Path destFile = Paths.get(destDir.toString(), tmpFile.toString());
         if (Files.exists(destFile))
         {
            if (debug)
            {
               ps.println(String.format("Dest file %s exists", destFile.toString()));
            }

            ps.println(String.format("[omitted] %s", tmpFile.toString()));
            continue;
         }

         ps.println(tmpFile.toString());

         if (debug)
         {
            ps.println(String.format("Dest file %s does not exists", destFile.toString()));
         }

         if (debug)
         {
            ps.println(String.format("Creating dest dir %s", destFile.getParent().toString()));
         }

         Files.createDirectories(destFile.getParent());

         final Path source = Paths.get(tmpDir.toString(), tmpFile.toString());

         if (debug)
         {
            ps.println(String.format("Renaming (moving) file from %s to %s", source.toString(), destFile.toString()));
         }

         source.toFile().renameTo(destFile.toFile());
      }

      Runtime.getRuntime().addShutdownHook(new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               Files.walk(tmpDir)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(file -> {
                        if (debug)
                        {
                           ps.println(String.format("Cleaning %s", file.toString()));
                        }
                        file.delete();
                     });
            }
            catch (final IOException e)
            {
               e.printStackTrace();
            }
         }
      });
   }

   private static void annotate(final JDefinedClass src, final Diagram diagram, final String nodeId, final List<BPMNAssociation> associations, final List<BPMNTextAnnotation> annotations)
   {
      final List<BPMNAssociation> ass = associations.stream().filter(a -> a.getSourceRef().equals(nodeId)).collect(Collectors.toList());

      for (final BPMNAssociation association : ass)
      {
         final BPMNTextAnnotation textAnnotation = annotations.stream().filter(ta -> ta.getId().equals(association.getTargetRef())).findAny().get();

         final Shape shape = diagram.getPlane().getShapes().stream().filter(s -> s.getBpmnElement().equals(textAnnotation.getId())).findAny().get();

         final Edge edge = diagram.getPlane().getEdges().stream().filter(e -> e.getBpmnElement().equals(association.getId())).findAny().get();

         final List<String> waypointCoordinates = new ArrayList<>();
         for (final Waypoint wp : edge.getWaypoints())
         {
            waypointCoordinates.add(wp.getX() + "," + wp.getY());
         }

         final JAnnotationUse jau = src.annotate(BPMNAnnotation.class);
         jau.param("association", association.getId());
         jau.param("textAnnotation", textAnnotation.getId());
         jau.param("value", textAnnotation.getText().getValue().trim());
         jau.param("x", JExpr.lit(shape.getBounds().getX()));
         jau.param("y", JExpr.lit(shape.getBounds().getY()));
         jau.param("width", JExpr.lit(shape.getBounds().getWidth()));
         jau.param("height", JExpr.lit(shape.getBounds().getHeight()));

         if (!waypointCoordinates.isEmpty())
         {
            jau.param("waypointCoordinates", waypointCoordinates.stream().collect(Collectors.joining(";")));
         }
      }
   }

   private static void annotateWithShapeAndLabel(final JDefinedClass src, final Diagram bpmnDiagram, final Class<? extends Annotation> shapeAnnotation,
         final Class<? extends Annotation> labelAnnotation, final NodeElement... bpmnElements)
   {
      for (final NodeElement bpmnElement : bpmnElements)
      {
         final Shape shape = bpmnDiagram.getPlane().getShapes().stream().filter(s -> s.getBpmnElement().equals(bpmnElement.getId())).findAny().get();

         final JAnnotationUse jau = src.annotate(shapeAnnotation);
         jau.param("x", JExpr.lit(shape.getBounds().getX()));
         jau.param("y", JExpr.lit(shape.getBounds().getY()));

         if (shape.getLabel() != null)
         {
            final JAnnotationUse jau2 = src.annotate(labelAnnotation);
            jau2.param("x", JExpr.lit(shape.getLabel().getBounds().getX()));
            jau2.param("y", JExpr.lit(shape.getLabel().getBounds().getY()));
            jau2.param("width", JExpr.lit(shape.getLabel().getBounds().getWidth()));
            jau2.param("height", JExpr.lit(shape.getLabel().getBounds().getHeight()));
         }
      }
   }

   private static void annotateWithGenerationDetails(final JDefinedClass src, final Path path)
   {
      final String bpmn = path.getFileName().toString();
      String pkg = Util.class.getPackage().getImplementationTitle();
      if (pkg == null)
      {
         pkg = Util.class.getPackage().getName();
      }
      String version = Util.class.getPackage().getImplementationVersion();
      if (version == null)
      {
         version = "n/a";
      }
      final LocalDateTime now = LocalDateTime.now();
      final String tstamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now);
      final JAnnotationUse jau = src.annotate(Generated.class);
      jau.param("from", bpmn);
      jau.param("with", pkg + "-" + version);
      jau.param("timestamp", tstamp);
   }

   private static Class<? extends BaseNode<?>> getBaseClass(final NodeElement bpmnElement)
   {
      if (bpmnElement instanceof BPMNBusinessRuleTask)
      {
         return BaseDecisionTableNode.class;
      }

      if (bpmnElement instanceof BPMNExclusiveGateway)
      {
         return BaseGatewayNode.class;
      }

      if (bpmnElement instanceof BPMNSubProcess)
      {
         return BaseProcessNode.class;
      }

      if (bpmnElement instanceof BPMNTask)
      {
         return BaseTaskNode.class;
      }
      return null;
   }

   private static Class<?> getMatchingImplementationInterface(final NodeElement bpmnElement)
   {
      if (bpmnElement instanceof BPMNBusinessRuleTask)
      {
         return BaseDecisionTable.class;
      }

      if (bpmnElement instanceof BPMNExclusiveGateway)
      {
         return Gateway.class;
      }

      if (bpmnElement instanceof BPMNSubProcess)
      {
         return Process.class;
      }

      if (bpmnElement instanceof BPMNTask)
      {
         return Task.class;
      }
      return null;
   }

   private static JDefinedClass createNodeSrc(final JDefinedClass processSrc, final NodeElement bpmnElement, final BPMNProcess bpmnProcess, final Diagram diagram, final String fqn)
         throws JClassAlreadyExistsException, ClassNotFoundException
   {
      // Create node source
      final JDefinedClass nodeSrc = processSrc._class(JMod.STATIC, fqn);

      final JMods mods = nodeSrc.mods();
      mods.setPublic();
      mods.setFinal(true);

      // Base class
      final Class<?> baseClass = Util.getBaseClass(bpmnElement);

      // Extend base class
      nodeSrc._extends(baseClass);

      // Set loop characteristics
      if (bpmnElement instanceof BPMNActivity)
      {
         Util.setLoopCharacteristics(nodeSrc, (BPMNActivity) bpmnElement);
      }

      // Annotate comments
      Util.annotate(nodeSrc, diagram, bpmnElement.getId(), bpmnProcess.getAssociation(), bpmnProcess.getTextAnnotation());

      // Annotate shape and label
      Util.annotateWithShapeAndLabel(nodeSrc, diagram, BPMNNodeShape.class, BPMNNodeLabel.class, bpmnElement);

      // Annotate implementation
      if (bpmnElement.getDocumentation() != null)
      {
         final Optional<BPMNDocumentation> implOptional = bpmnElement.getDocumentation().stream().filter(d -> ("p4j-implementation-for-" + bpmnElement.getId()).equals(d.getId())).findAny();
         if (implOptional.isPresent())
         {
            final String impl = implOptional.get().getText().trim();
            final JAnnotationUse jau = nodeSrc.annotate(Implementation.class);
            jau.param("value", impl);
         }
         else
         {
            LOG.warning(String.format("Node %s has no implementation assigned. Executing the node is futile", bpmnElement.getName()));
         }
      }
      else
      {
         LOG.warning(String.format("Node %s has no implementation assigned. Executing the node is futile", bpmnElement.getName()));
      }

      // Annotate rules
      if (bpmnElement instanceof BPMNBusinessRuleTask)
      {
         if (bpmnElement.getDocumentation() != null)
         {
            final Optional<BPMNDocumentation> resourceOptional = bpmnElement.getDocumentation().stream().filter(d -> ("p4j-rules-resource-for-" + bpmnElement.getId()).equals(d.getId())).findAny();
            final Optional<BPMNDocumentation> keyOptional = bpmnElement.getDocumentation().stream().filter(d -> ("p4j-rules-key-for-" + bpmnElement.getId()).equals(d.getId())).findAny();
            if (resourceOptional.isPresent())
            {
               final String resource = resourceOptional.get().getText().trim();
               final JAnnotationUse jau = nodeSrc.annotate(Rules.class);
               jau.param("resource", resource);
               if (keyOptional.isPresent())
               {
                  final String key = keyOptional.get().getText().trim();
                  jau.param("key", key);
               }
            }
            else
            {
               LOG.warning(String.format("Decision Table Node %s has no rules resource. Executing the node without rules at runtime will be futile", bpmnElement.getName()));
            }
         }
         else
         {
            LOG.warning(String.format("Decision Table Node %s has no rules resource. Executing the node without rules at runtime will be futile", bpmnElement.getName()));
         }
      }

      // Annotate documentation
      if (bpmnElement.getDocumentation() != null)
      {
         final Optional<BPMNDocumentation> docOptional = bpmnElement.getDocumentation().stream().filter(d -> d.getId() == null).findAny();
         if (docOptional.isPresent())
         {
            final String doc = docOptional.get().getText().trim();
            final JAnnotationUse jau = nodeSrc.annotate(Documentation.class);
            jau.param("value", doc);
         }
      }

      // Annotate id
      final JAnnotationUse jau2 = nodeSrc.annotate(Id.class);
      jau2.param("value", bpmnElement.getId());

      // Annotate name
      final JAnnotationUse jau3 = nodeSrc.annotate(Name.class);
      jau3.param("value", bpmnElement.getName());

      return nodeSrc;
   }

   private static void setLoopCharacteristics(final JDefinedClass nodeSrc, final BPMNActivity activity)
   {
      // Multi Instance Activity
      if (activity.getMultiInstanceLoopCharacteristics() != null)
      {
         if (!activity.getMultiInstanceLoopCharacteristics().getIsSequential())
         {
            throw new IllegalStateException(String.format("Parallel execution is not supported. Please revice node %s", activity.getName()));
         }
         if (activity.getDocumentation() == null)
         {
            throw new IllegalStateException(String.format(MISSING_FOREACH_ANNOTATION, activity.getName(), KEYWORD_FOREACH));
         }
         final Optional<BPMNDocumentation> foreachOptional = activity.getDocumentation().stream().filter(d -> ("p4j-foreach-expression-for-" + activity.getId()).equals(d.getId())).findAny();
         if (!foreachOptional.isPresent())
         {
            throw new IllegalStateException(String.format(MISSING_FOREACH_ANNOTATION, activity.getName(), KEYWORD_FOREACH));
         }
         final String foreachExpression = foreachOptional.get().getText().trim();

         final JAnnotationUse jau = nodeSrc.annotate(Foreach.class);
         jau.param("expression", foreachExpression);

         if (!activity.getMultiInstanceLoopCharacteristics().getIsSequential())
         {
            jau.param("parallel", true);
         }
      }

      // Loop Activity
      else if (activity.getStandardLoopCharacteristics() != null)
      {
         if (activity.getDocumentation() == null)
         {
            throw new IllegalStateException(String.format(MISSING_WHILE_ANNOTATION, activity.getName(), KEYWORD_WHILE));
         }
         final Optional<BPMNDocumentation> whileOptional = activity.getDocumentation().stream().filter(d -> ("p4j-while-condition-for-" + activity.getId()).equals(d.getId())).findAny();
         if (!whileOptional.isPresent())
         {
            throw new IllegalStateException(String.format(MISSING_WHILE_ANNOTATION, activity.getName(), KEYWORD_WHILE));
         }
         final String whileExpression = whileOptional.get().getText().trim();

         final JAnnotationUse jau = nodeSrc.annotate(While.class);
         jau.param("expression", whileExpression);
      }
   }

   private static void createImplSrc(final JCodeModel model, final NodeElement bpmnElement, final BPMNProcess bpmnProcess, final Diagram diagram, final Path bpmn, final Class<?> interfaceClass)
         throws JClassAlreadyExistsException, ClassNotFoundException
   {
      if (interfaceClass.isAssignableFrom(Process.class))
      {
         return; // Process implementation must be created from BPMN model
      }

      if (bpmnElement.getDocumentation() != null)
      {
         final Optional<BPMNDocumentation> implAnnotation = bpmnElement.getDocumentation().stream().filter(d -> ("p4j-implementation-for-" + bpmnElement.getId()).equals(d.getId())).findAny();
         if (implAnnotation.isPresent())
         {
            final String fqn = implAnnotation.get().getText().trim();

            if (model._getClass(fqn) != null)
            {
               return;
            }

            // Create impl source
            final JDefinedClass implSrc = model._class(fqn);

            // Implement interface
            if (interfaceClass.isInterface())
            {
               implSrc._implements(interfaceClass);
            }
            else
            {
               implSrc._extends(interfaceClass);
            }

            // Override interface method
            Util.override(interfaceClass, implSrc, model);

            Util.annotateWithGenerationDetails(implSrc, bpmn);
         }
      }
   }

   private static void override(final Class<?> interfaceClass, final JDefinedClass implSrc, final JCodeModel model)
   {
      // Override abstract method 'result'
      if (interfaceClass.equals(BaseDecisionTable.class))
      {
         final JMethod applyMethod = implSrc.method(JMod.PUBLIC, model.VOID, "apply");
         final JClass ruleType = model.ref(String.class);
         applyMethod.param(ruleType, "result");
         applyMethod.param(JsonObject.class, "businessData");
         applyMethod.param(ProcessData.class, "processData");
         applyMethod.param(Iteration.class, "iteration");
         applyMethod.annotate(Override.class);
         applyMethod.body().directStatement("// TODO: Apply the result of your matching rule here");
      }

      else if (interfaceClass.equals(Task.class))
      {
         final JMethod executeTaskMethod = implSrc.method(JMod.PUBLIC, model.VOID, "execute");
         executeTaskMethod.param(JsonObject.class, "businessData");
         executeTaskMethod.param(ProcessData.class, "processData");
         executeTaskMethod.param(Iteration.class, "iteration");
         executeTaskMethod.annotate(Override.class);
         executeTaskMethod.body().directStatement("// TODO: Implement your task logic here");
      }

      // Override abstract executeGateway method
      else if (interfaceClass.equals(Gateway.class))
      {
         final JMethod executeGatewayMethod = implSrc.method(JMod.PUBLIC, String.class, "execute");
         executeGatewayMethod.param(JsonObject.class, "businessData");
         executeGatewayMethod.param(ProcessData.class, "processData");
         executeGatewayMethod.param(Iteration.class, "iteration");
         executeGatewayMethod.annotate(Override.class);
         executeGatewayMethod.body().directStatement("// TODO: Return the appropriate flow");
         executeGatewayMethod.body()._return(JExpr._null());
      }
   }

   private static JExpression getPrimitive(final JCodeModel model, final JPrimitiveType type)
   {
      if (type.equals(model.BOOLEAN))
      {
         return JExpr.lit(false);
      }

      if (type.equals(model.BYTE))
      {
         return JExpr.cast(model.BYTE, JExpr.lit(0));
      }

      if (type.equals(model.CHAR))
      {
         return JExpr.lit('0');
      }

      if (type.equals(model.DOUBLE) || type.equals(model.FLOAT) || type.equals(model.INT) || type.equals(model.LONG))
      {
         return JExpr.lit(0);
      }

      if (type.equals(model.SHORT))
      {
         return JExpr.cast(model.SHORT, JExpr.lit(0));
      }

      throw new RuntimeException(String.format("Primitive type %s not supported", type.name()));
   }

   private static String fixName(final String name)
   {
      final String result = name.trim();
      final StringBuilder sb = new StringBuilder();
      final String[] parts = result.replaceAll("ä|å", "a").replaceAll("ö", "o").replaceAll("Ä|Å", "A").replaceAll("Ö", "O").split("\\s");
      for (String part : parts)
      {
         part = part.trim();
         if (part.isEmpty())
         {
            continue;
         }
         sb.append(part.substring(0, 1).toUpperCase() + part.substring(1));
      }
      return sb.toString().replaceAll("\\W", "");
   }

   public static boolean compile(final Path javaSourcePath) throws IOException, ClassNotFoundException
   {

      // Get Java compiler and associated tooling
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
      final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
      final List<JavaFileObject> compilationUnits = new ArrayList<>();

      // Add generated sub process source files for compilation
      Files.walk(javaSourcePath).filter(path -> (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))).forEach(path -> {
         for (final JavaFileObject jfo : fileManager.getJavaFileObjects(path.toString()))
         {
            compilationUnits.add(jfo);
         }
      });

      // Compile sub process
      final CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
      final boolean result = task.call();

      // Print diagnostics
      LOG.fine("Compilation succeeded without errors: " + result);
      for (final Diagnostic<?> diagnostic : diagnostics.getDiagnostics())
      {
         System.out.println(diagnostic.getCode());
         System.out.println(diagnostic.getKind());
         System.out.println(diagnostic.getPosition());
         System.out.println(diagnostic.getStartPosition());
         System.out.println(diagnostic.getEndPosition());
         System.out.println(diagnostic.getSource());
         System.out.println(diagnostic.getMessage(null));
      }
      return result;
   }
}
