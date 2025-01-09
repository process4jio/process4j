package io.process4j.core.bpmn.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BPMNProcess
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "startEvent")
   private BPMNStartEvent startEvent;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "task")
   private final List<BPMNTask> task = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "businessRuleTask")
   private final List<BPMNBusinessRuleTask> businessRuleTask = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "exclusiveGateway")
   private final List<BPMNExclusiveGateway> exclusiveGateway = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "subProcess")
   private final List<BPMNSubProcess> subProcess = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "endEvent")
   private final List<BPMNEndEvent> endEvent = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "sequenceFlow")
   private final List<BPMNSequenceFlow> sequenceFlow = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "textAnnotation")
   private final List<BPMNTextAnnotation> textAnnotation = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "association")
   private final List<BPMNAssociation> association = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "documentation")
   private final List<BPMNDocumentation> documentation = new ArrayList<>();

   // private final Map<String, SequenceFlow> positions = new LinkedHashMap<>();

   private Map<String, List<String>> annotations;

   public BPMNProcess()
   {
   }

   public BPMNProcess(final String id, final String name, final BPMNStartEvent startEvent, final BPMNEndEvent endEvent)
   {
      this.id = id;
      this.name = name;
      this.startEvent = startEvent;
      this.endEvent.addAll(Collections.singletonList(endEvent));
   }

   public String getId()
   {
      return this.id;
   }

   public String getName()
   {
      return this.name;
   }

   public BPMNStartEvent getStartEvent()
   {
      return this.startEvent;
   }

   public List<BPMNTask> getTask()
   {
      return this.task;
   }

   public List<BPMNBusinessRuleTask> getBusinessRuleTask()
   {
      return this.businessRuleTask;
   }

   public List<BPMNExclusiveGateway> getExclusiveGateway()
   {
      return this.exclusiveGateway;
   }

   public List<BPMNSubProcess> getSubProcess()
   {
      return this.subProcess;
   }

   public List<BPMNEndEvent> getEndEvent()
   {
      return this.endEvent;
   }

   public List<BPMNSequenceFlow> getSequenceFlow()
   {
      return this.sequenceFlow;
   }

   public List<BPMNTextAnnotation> getTextAnnotation()
   {
      return this.textAnnotation;
   }

   public List<BPMNAssociation> getAssociation()
   {
      return this.association;
   }

   // public Map<String, SequenceFlow> getPositions()
   // {
   // return this.positions;
   // }

   public List<BPMNDocumentation> getDocumentation()
   {
      return this.documentation;
   }

   public Map<String, List<String>> getAnnotations()
   {
      if (this.annotations != null)
      {
         return this.annotations;
      }

      this.annotations = new HashMap<>();

      // Get text annotations (indexed by id)
      final Map<String, String> annotations = this.textAnnotation.stream().collect(Collectors.toMap(BPMNTextAnnotation::getId, t -> t.getText().getValue()));

      // Get annotation associations (indexed by association source)
      final Map<String, List<String>> annotationAssociations = new HashMap<>();
      final Iterator<BPMNAssociation> iter = this.association.iterator();
      while (iter.hasNext())
      {
         final BPMNAssociation association = iter.next();
         List<String> targets;
         if ((targets = annotationAssociations.get(association.getSourceRef())) == null)
         {
            targets = new ArrayList<>();
         }
         targets.add(annotations.get(association.getTargetRef()));
         annotationAssociations.put(association.getSourceRef(), targets);
      }

      this.annotations.putAll(annotationAssociations);
      return this.annotations;
   }

   public List<NodeElement> getNodes()
   {
      final List<NodeElement> list = new ArrayList<>();
      list.addAll(this.businessRuleTask);
      list.addAll(this.exclusiveGateway);
      list.addAll(this.subProcess);
      list.addAll(this.task);
      return list;
   }
}
