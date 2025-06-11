package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class BPMNDocumentation
{
   @XmlAttribute
   private String id;

   @XmlValue
   private String text;

   public BPMNDocumentation()
   {
   }

   public BPMNDocumentation(final String id, final String text)
   {
      this.id = id;
      this.text = text;
   }

   public String getId()
   {
      return this.id;
   }

   public String getText()
   {
      return this.text;
   }
}
