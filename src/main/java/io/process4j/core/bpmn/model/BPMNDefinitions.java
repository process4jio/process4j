package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.process4j.core.bpmn.di.Diagram;

@XmlRootElement(name = "definitions", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL")
public class BPMNDefinitions
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlAttribute
   private final String targetNamespace = "http://bpmn.process4j.io/";

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "process")
   private BPMNProcess process;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/DI", name = "BPMNDiagram")
   private Diagram diagram;

   public BPMNDefinitions()
   {
   }

   public BPMNDefinitions(final String id, final String name, final BPMNProcess process, final Diagram diagram)
   {
      this.id = id;
      this.name = name;
      this.process = process;
      this.diagram = diagram;
   }

   public String getId()
   {
      return this.id;
   }

   public String getName()
   {
      return this.name;
   }

   public String getTargetNamespace()
   {
      return this.targetNamespace;
   }

   public BPMNProcess getProcess()
   {
      return this.process;
   }

   public Diagram getDiagram()
   {
      return this.diagram;
   }
}
