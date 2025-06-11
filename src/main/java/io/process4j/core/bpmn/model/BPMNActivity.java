package io.process4j.core.bpmn.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public abstract class BPMNActivity implements NodeElement
{
   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "multiInstanceLoopCharacteristics")
   private BPMNMultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "standardLoopCharacteristics")
   private BPMNStandardLoopCharacteristics standardLoopCharacteristics;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "documentation")
   private List<BPMNDocumentation> documentation;

   public BPMNMultiInstanceLoopCharacteristics getMultiInstanceLoopCharacteristics()
   {
      return this.multiInstanceLoopCharacteristics;
   }

   public BPMNStandardLoopCharacteristics getStandardLoopCharacteristics()
   {
      return this.standardLoopCharacteristics;
   }

   @Override
   public List<BPMNDocumentation> getDocumentation()
   {
      return this.documentation;
   }
}
