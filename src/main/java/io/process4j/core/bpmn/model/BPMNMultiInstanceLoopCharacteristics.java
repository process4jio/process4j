package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlAttribute;

public class BPMNMultiInstanceLoopCharacteristics
{
   @XmlAttribute
   private boolean isSequential;

   public boolean getIsSequential()
   {
      return this.isSequential;
   }
}
