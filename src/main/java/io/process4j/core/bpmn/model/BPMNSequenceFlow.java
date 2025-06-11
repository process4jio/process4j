package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlAttribute;

public class BPMNSequenceFlow
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlAttribute
   private String sourceRef;

   @XmlAttribute
   private String targetRef;

   public BPMNSequenceFlow()
   {
   }

   public BPMNSequenceFlow(String id, String name, String sourceRef, String targetRef)
   {
      this.id = id;
      this.name = name;
      this.sourceRef = sourceRef;
      this.targetRef = targetRef;
   }

   public String getId()
   {
      return this.id;
   }

   public String getName()
   {
      return this.name;
   }

   public String getSourceRef()
   {
      return this.sourceRef;
   }

   public String getTargetRef()
   {
      return this.targetRef;
   }

}
