package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlAttribute;

public class BPMNAssociation
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String sourceRef;

   @XmlAttribute
   private String targetRef;

   public BPMNAssociation()
   {
   }

   public BPMNAssociation(String id, String sourceRef, String targetRef)
   {
      this.id = id;
      this.sourceRef = sourceRef;
      this.targetRef = targetRef;
   }

   public String getId()
   {
      return this.id;
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
