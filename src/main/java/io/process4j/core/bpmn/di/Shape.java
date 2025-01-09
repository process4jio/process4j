package io.process4j.core.bpmn.di;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Shape
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String bpmnElement;

   @XmlElement(name = "Bounds", namespace = "http://www.omg.org/spec/DD/20100524/DC")
   private Bounds bounds;

   @XmlElement(name = "BPMNLabel", namespace = "http://www.omg.org/spec/BPMN/20100524/DI")
   private Label label;

   public Shape()
   {
   }

   public Shape(String bpmnElement, Bounds bounds)
   {
      this(bpmnElement, bounds, null);
   }

   public Shape(String bpmnElement, Bounds bounds, Label label)
   {
      this.id = bpmnElement + "_di";
      this.bpmnElement = bpmnElement;
      this.bounds = bounds;
      this.label = label;
   }

   public String getId()
   {
      return this.id;
   }

   public String getBpmnElement()
   {
      return this.bpmnElement;
   }

   public Bounds getBounds()
   {
      return this.bounds;
   }

   public Label getLabel()
   {
      return this.label;
   }
}
