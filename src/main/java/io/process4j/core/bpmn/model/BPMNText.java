package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlValue;

public class BPMNText
{
   @XmlValue
   private String value;

   public BPMNText()
   {
   }

   public BPMNText(String value)
   {
      this.value = value;
   }

   public String getValue()
   {
      return this.value;
   }
}
