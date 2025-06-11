package io.process4j.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.process4j.core.bpmn.annotations.BPMNAnnotation;
import io.process4j.core.bpmn.annotations.BPMNEdge;
import io.process4j.core.bpmn.annotations.BPMNEdgeLabel;
import io.process4j.core.bpmn.annotations.BPMNEndEventLabel;
import io.process4j.core.bpmn.annotations.BPMNEndEventShape;
import io.process4j.core.bpmn.annotations.BPMNNodeLabel;
import io.process4j.core.bpmn.annotations.BPMNNodeShape;
import io.process4j.core.bpmn.annotations.BPMNStartEventLabel;
import io.process4j.core.bpmn.annotations.BPMNStartEventShape;
import io.process4j.core.bpmn.di.Bounds;
import io.process4j.core.bpmn.di.Diagram;
import io.process4j.core.bpmn.di.Edge;
import io.process4j.core.bpmn.di.Label;
import io.process4j.core.bpmn.di.Plane;
import io.process4j.core.bpmn.di.Shape;
import io.process4j.core.bpmn.di.Waypoint;
import io.process4j.core.bpmn.model.BPMNAssociation;
import io.process4j.core.bpmn.model.BPMNBusinessRuleTask;
import io.process4j.core.bpmn.model.BPMNDefinitions;
import io.process4j.core.bpmn.model.BPMNEndEvent;
import io.process4j.core.bpmn.model.BPMNExclusiveGateway;
import io.process4j.core.bpmn.model.BPMNIncoming;
import io.process4j.core.bpmn.model.BPMNOutgoing;
import io.process4j.core.bpmn.model.BPMNProcess;
import io.process4j.core.bpmn.model.BPMNSequenceFlow;
import io.process4j.core.bpmn.model.BPMNStartEvent;
import io.process4j.core.bpmn.model.BPMNSubProcess;
import io.process4j.core.bpmn.model.BPMNTask;
import io.process4j.core.bpmn.model.BPMNText;
import io.process4j.core.bpmn.model.BPMNTextAnnotation;
import io.process4j.core.bpmn.model.NodeElement;

public class BPMN
{
   private static BPMNStartEvent createBPMNStartEventElement(final BaseProcess process)
   {
      return new BPMNStartEvent(BPMNStartEvent.DEFAULT_ID, BPMNStartEvent.DEFAULT_NAME, new BPMNOutgoing(process.getDefaultPosition().getId()));
   }

   private static BPMNEndEvent createBPMNEndEventElement(final BaseProcess process)
   {
      final List<BPMNIncoming> incoming = process.getFlows().stream().filter(flow -> (flow.getTarget() == null)).map(flow -> new BPMNIncoming(flow.getId())).collect(Collectors.toList());

      return new BPMNEndEvent(BPMNEndEvent.DEFAULT_ID, BPMNEndEvent.DEFAULT_NAME, incoming.toArray(new BPMNIncoming[incoming.size()]));
   }

   private static List<BPMNTextAnnotation> createBPMNAnnotationElements(final BaseNode<?> node)
   {
      final List<BPMNTextAnnotation> annotations = new ArrayList<>();
      for (final BPMNAnnotation a : node.getClass().getAnnotationsByType(BPMNAnnotation.class))
      {
         annotations.add(new BPMNTextAnnotation(a.textAnnotation(), new BPMNText(a.value())));
      }
      return annotations;
   }

   private static List<BPMNAssociation> createBPMNAssociationElements(final BaseNode<?> node)
   {
      final List<BPMNAssociation> associations = new ArrayList<>();
      for (final BPMNAnnotation a : node.getClass().getAnnotationsByType(BPMNAnnotation.class))
      {
         associations.add(new BPMNAssociation(a.association(), node.getId(), a.textAnnotation()));
      }
      return associations;
   }

   private static BPMNBusinessRuleTask createBPMNElement(final BaseDecisionTableNode node)
   {
      return new BPMNBusinessRuleTask(node.getId(), node.getName(), new BPMNOutgoing(node.getExit().getId()));
   }

   private static BPMNTask createBPMNElement(final BaseTaskNode node)
   {
      return new BPMNTask(node.getId(), node.getName(), new BPMNOutgoing(node.getExit().getId()));
   }

   private static BPMNExclusiveGateway createBPMNElement(final BaseGatewayNode node)
   {
      final List<BPMNIncoming> incoming = new ArrayList<>();
      final List<BPMNOutgoing> outgoing = node.getExits().values().stream().map(f -> new BPMNOutgoing(f.getId())).collect(Collectors.toList());
      return new BPMNExclusiveGateway(node.getId(), node.getName(), incoming, outgoing);
   }

   private static BPMNSubProcess createBPMNElement(final BaseProcessNode node)
   {
      return new BPMNSubProcess(node.getId(), node.getName(), new BPMNOutgoing(node.getExit().getId()));
   }

   private static NodeElement createBPMNElement(final BaseNode<?> node)
   {
      if (node instanceof BaseDecisionTableNode)
      {
         return BPMN.createBPMNElement((BaseDecisionTableNode) node);
      }

      if (node instanceof BaseGatewayNode)
      {
         return BPMN.createBPMNElement((BaseGatewayNode) node);
      }

      if (node instanceof BaseProcessNode)
      {
         return BPMN.createBPMNElement((BaseProcessNode) node);
      }

      if (node instanceof BaseTaskNode)
      {
         return BPMN.createBPMNElement((BaseTaskNode) node);
      }
      return null;
   }

   private static BPMNSequenceFlow createBPMNElement(final Flow flow)
   {
      final String sourceId = flow.getSource() == null ? BPMNStartEvent.DEFAULT_ID : BPMN.createBPMNElement(flow.getSource()).getId();
      final String targetId = flow.getTarget() == null ? BPMNEndEvent.DEFAULT_ID : BPMN.createBPMNElement(flow.getTarget()).getId();
      return new BPMNSequenceFlow(flow.getId(), flow.getName(), sourceId, targetId);
   }

   private static Edge createBPMNEdge(final Flow flow, final BaseProcess process)
   {
      final List<Waypoint> waypoints = new ArrayList<>();
      Label label = null;

      // Get corresponding BPMNEdge annotation
      final Optional<BPMNEdge> flowAnnotation = Arrays.asList(process.getClass().getAnnotationsByType(BPMNEdge.class)).stream().filter(annotation -> annotation.flow().equals(flow.getId())).findAny();

      // Set flow coordinates
      if (flowAnnotation.isPresent())
      {
         Arrays.asList(flowAnnotation.get().waypointCoordinates().split(";")).stream().map(wpCoords -> {
            final String[] coords = wpCoords.split(",");
            return new Waypoint(coords[0], coords[1]);
         }).forEach(wp -> {
            waypoints.add(wp);
         });
      }

      // Get corresponding BPMNEdgeLabel annotation
      final Optional<BPMNEdgeLabel> flowLabelAnnotation = Arrays.asList(process.getClass().getAnnotationsByType(BPMNEdgeLabel.class)).stream()
            .filter(annotation -> annotation.flow().equals(flow.getId())).findAny();

      // Set flow label
      if (flowLabelAnnotation.isPresent())
      {
         final BPMNEdgeLabel flowLabel = flowLabelAnnotation.get();
         label = new Label(new Bounds(flowLabel.x(), flowLabel.y(), flowLabel.width(), flowLabel.height()));
      }

      if (waypoints.isEmpty())
      {
         return new Edge(flow.getId(), label, new Waypoint("0", "0"), new Waypoint("100", "100"));
      }
      else
      {
         return new Edge(flow.getId(), label, waypoints.toArray(new Waypoint[waypoints.size()]));
      }
   }

   private static BPMNProcess createBPMNProcess(final BaseProcess process)
   {
      final Map<String, NodeElement> nodeCache = new HashMap<>();
      final BPMNProcess bpmnProcess = new BPMNProcess("Process", BPMN.getBPMNProcessName(process), BPMN.createBPMNStartEventElement(process),
            BPMN.createBPMNEndEventElement(process));
      for (final Flow flow : process.getFlows())
      {

         // SequenceFlow
         bpmnProcess.getSequenceFlow().add(BPMN.createBPMNElement(flow));

         final BaseNode<?> target = flow.getTarget();

         if (target == null)
         {
            continue;
         }

         final NodeElement nodeElement = nodeCache.get(target.getId());

         if (nodeElement == null)
         {

            // BusinessRuleTask
            if (target instanceof BaseDecisionTableNode)
            {
               final BPMNBusinessRuleTask bpmnElement = BPMN.createBPMNElement((BaseDecisionTableNode) target);
               nodeCache.put(target.getId(), bpmnElement);
               bpmnProcess.getBusinessRuleTask().add(bpmnElement);
            }

            // Task
            else if (target instanceof BaseTaskNode)
            {
               final BPMNTask bpmnElement = BPMN.createBPMNElement((BaseTaskNode) target);
               nodeCache.put(target.getId(), bpmnElement);
               bpmnProcess.getTask().add(bpmnElement);
            }

            // ExclusiveGateway
            else if (target instanceof BaseGatewayNode)
            {
               final BPMNExclusiveGateway bpmnElement = BPMN.createBPMNElement((BaseGatewayNode) target);
               nodeCache.put(target.getId(), bpmnElement);
               bpmnProcess.getExclusiveGateway().add(bpmnElement);
            }

            // SubProcess
            else if (target instanceof BaseProcessNode)
            {
               final BPMNSubProcess bpmnElement = BPMN.createBPMNElement((BaseProcessNode) target);
               nodeCache.put(target.getId(), bpmnElement);
               bpmnProcess.getSubProcess().add(bpmnElement);
            }

            // Annotations
            bpmnProcess.getTextAnnotation().addAll(BPMN.createBPMNAnnotationElements(target));
            bpmnProcess.getAssociation().addAll(BPMN.createBPMNAssociationElements(target));
         }

         // Incoming
         nodeCache.get(target.getId()).getIncoming().add(new BPMNIncoming(flow.getId()));
      }

      return bpmnProcess;
   }

   private static Shape createBPMNStartEventShape(final BaseProcess process)
   {
      final BPMNStartEvent bpmnElement = BPMN.createBPMNStartEventElement(process);
      final Shape shape = new Shape(bpmnElement.getId(), new Bounds("100", "100", "36", "36"), new Label(new Bounds("100", "100", "36", "36")));
      final BPMNStartEventShape shapeAnnotation = process.getClass().getAnnotation(BPMNStartEventShape.class);
      if (shapeAnnotation != null)
      {
         shape.getBounds().setX(shapeAnnotation.x()).setY(shapeAnnotation.y());
         final BPMNStartEventLabel labelAnnotation = process.getClass().getAnnotation(BPMNStartEventLabel.class);
         if (labelAnnotation != null)
         {
            shape.getLabel().getBounds().setX(labelAnnotation.x());
            shape.getLabel().getBounds().setY(labelAnnotation.y());
            shape.getLabel().getBounds().setWidth(labelAnnotation.width());
            shape.getLabel().getBounds().setHeight(labelAnnotation.height());
         }
      }
      return shape;
   }

   private static Shape createBPMNEndEventShape(final BaseProcess process)
   {
      final BPMNEndEvent bpmnElement = BPMN.createBPMNEndEventElement(process);
      final Shape shape = new Shape(bpmnElement.getId(), new Bounds("500", "100", "36", "36"), new Label(new Bounds("500", "100", "36", "36")));
      final BPMNEndEventShape shapeAnnotation = process.getClass().getAnnotation(BPMNEndEventShape.class);
      if (shapeAnnotation != null)
      {
         shape.getBounds().setX(shapeAnnotation.x()).setY(shapeAnnotation.y());
         final BPMNEndEventLabel labelAnnotation = process.getClass().getAnnotation(BPMNEndEventLabel.class);
         if (labelAnnotation != null)
         {
            shape.getLabel().getBounds().setX(labelAnnotation.x());
            shape.getLabel().getBounds().setY(labelAnnotation.y());
            shape.getLabel().getBounds().setWidth(labelAnnotation.width());
            shape.getLabel().getBounds().setHeight(labelAnnotation.height());
         }
      }
      return shape;
   }

   private static Shape createBPMNShape(final BaseNode<?> node)
   {
      Shape shape = null;
      if (node instanceof BaseActivityNode<?>)
      {
         shape = new Shape(node.getId(), new Bounds("300", "100", "100", "80"));
      }
      else if (node instanceof BaseGatewayNode)
      {
         shape = new Shape(node.getId(), new Bounds("300", "100", "50", "50"), new Label(new Bounds("0", "0", "88", "14")));
      }
      final BPMNNodeShape shapeAnnotation = node.getClass().getAnnotation(BPMNNodeShape.class);
      if (shapeAnnotation != null)
      {
         shape.getBounds().setX(shapeAnnotation.x()).setY(shapeAnnotation.y());
         final BPMNNodeLabel labelAnnotation = node.getClass().getAnnotation(BPMNNodeLabel.class);
         if (labelAnnotation != null)
         {
            shape.getLabel().getBounds().setX(labelAnnotation.x());
            shape.getLabel().getBounds().setY(labelAnnotation.y());
            shape.getLabel().getBounds().setWidth(labelAnnotation.width());
            shape.getLabel().getBounds().setHeight(labelAnnotation.height());
         }
      }
      return shape;
   }

   private static List<Shape> createBPMNAnnotationShapes(final BaseNode<?> node)
   {
      final List<Shape> shapes = new ArrayList<>();
      final List<BPMNTextAnnotation> bpmnAnnotationElements = BPMN.createBPMNAnnotationElements(node);
      for (final BPMNTextAnnotation a : bpmnAnnotationElements)
      {
         final BPMNAnnotation b = Arrays.asList(node.getClass().getAnnotationsByType(BPMNAnnotation.class)).stream().filter(ann -> ann.textAnnotation().equals(a.getId())).findAny().get();

         shapes.add(new Shape(a.getId(), new Bounds(b.x(), b.y(), b.width(), b.height())));
      }
      return shapes;
   }

   private static List<Edge> createBPMNAssociationEdges(final BaseNode<?> node)
   {
      final List<Edge> edges = new ArrayList<>();
      final List<BPMNAssociation> associations = BPMN.createBPMNAssociationElements(node);
      for (final BPMNAssociation association : associations)
      {
         final List<Waypoint> waypoints = Arrays.asList(Arrays.asList(node.getClass().getAnnotationsByType(BPMNAnnotation.class)).stream().filter(a -> a.association().equals(association.getId()))
               .findAny().get().waypointCoordinates().split(";")).stream().map(coord -> {
                  final String[] parts = coord.split(",");
                  return new Waypoint(parts[0], parts[1]);
               }).collect(Collectors.toList());

         edges.add(new Edge(association.getId(), null, waypoints.toArray(new Waypoint[waypoints.size()])));
      }
      return edges;
   }

   private static List<Shape> createBPMNShapes(final BaseProcess process)
   {
      final Set<String> shapeCache = new HashSet<>();
      final List<Shape> shapes = new ArrayList<>();
      shapes.add(BPMN.createBPMNStartEventShape(process));
      shapes.add(BPMN.createBPMNEndEventShape(process));
      for (final Flow flow : process.getFlows())
      {

         final BaseNode<?> target = flow.getTarget();

         if (target == null)
         {
            continue;
         }

         if (!shapeCache.contains(target.getId()))
         {
            shapes.add(BPMN.createBPMNShape(target));
            shapes.addAll(BPMN.createBPMNAnnotationShapes(target));
            shapeCache.add(target.getId());
         }
      }
      return shapes;
   }

   private static List<Edge> createBPMNEdges(final BaseProcess process)
   {
      final List<Edge> edges = new ArrayList<>(process.getFlows().stream().map(flow -> BPMN.createBPMNEdge(flow, process)).collect(Collectors.toList()));

      // Association Edges
      edges.addAll(process.getFlows().stream().filter(flow -> (flow.getTarget() != null)).map(flow -> BPMN.createBPMNAssociationEdges(flow.getTarget())).flatMap(List::stream)
            .collect(Collectors.toList()));

      return edges;
   }

   private static Plane createBPMNPlane(final BaseProcess process)
   {
      return new Plane(Plane.DEFAULT_ID, BPMN.createBPMNProcess(process).getId(), BPMN.createBPMNShapes(process), BPMN.createBPMNEdges(process));
   }

   private static Diagram createBPMNDiagram(final BaseProcess process)
   {
      return new Diagram(Diagram.DEFAULT_ID, BPMN.createBPMNPlane(process));
   }

   private static String getBPMNProcessFQN(final BaseProcess process)
   {
      return process.getClass().getCanonicalName();
   }

   // private static String getBPMNProcessFQN(Process process)
   // {
   // final Optional<BPMNAnnotation> annotation = Arrays.asList(process.getClass().getAnnotationsByType(BPMNAnnotation.class)).stream()
   // .filter(a -> a.value().trim().startsWith(BPMNUtil.KEYWORD_EXTENDS))
   // .findAny();
   // return annotation.isPresent() ? annotation.get().value().split(BPMNUtil.KEYWORD_EXTENDS)[1].trim() : process.getClass().getCanonicalName();
   // }

   private static String getBPMNProcessName(final BaseProcess process)
   {
      return process.getClass().getSimpleName();
   }

   // private static String getBPMNProcessName(Process process)
   // {
   // final Optional<BPMNAnnotation> annotation = Arrays.asList(process.getClass().getAnnotationsByType(BPMNAnnotation.class)).stream()
   // .filter(a -> a.value().trim().startsWith(BPMNUtil.KEYWORD_EXTENDS))
   // .findAny();
   // final String bpmnProcessFQN = annotation.isPresent() ? annotation.get().value() : process.getClass().getCanonicalName();
   // return annotation.isPresent() ? bpmnProcessFQN.substring(bpmnProcessFQN.lastIndexOf(".") + 1) : process.getClass().getSimpleName();
   // }

   public static BPMNDefinitions createBPMNDefinitions(final BaseProcess process)
   {
      return new BPMNDefinitions(BPMN.getBPMNProcessFQN(process), BPMN.getBPMNProcessName(process), BPMN.createBPMNProcess(process),
            BPMN.createBPMNDiagram(process));
   }
}
