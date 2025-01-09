package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BPMNTextAnnotation
{
   @XmlAttribute
   private String id;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "text")
   private BPMNText text;

   public BPMNTextAnnotation()
   {
   }

   public BPMNTextAnnotation(String id, BPMNText text)
   {
      this.id = id;
      this.text = text;
   }

   public String getId()
   {
      return this.id;
   }

   public BPMNText getText()
   {
      return this.text;
   }
}
