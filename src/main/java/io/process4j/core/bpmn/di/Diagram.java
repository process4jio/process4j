package io.process4j.core.bpmn.di;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Diagram
{
   public static final String DEFAULT_ID = "BPMNDiagram";

   @XmlAttribute
   private String id;

   @XmlElement(name = "BPMNPlane", namespace = "http://www.omg.org/spec/BPMN/20100524/DI")
   private Plane plane;

   public Diagram()
   {
   }

   public Diagram(String id, Plane plane)
   {
      this.id = id;
      this.plane = plane;
   }

   public String getId()
   {
      return this.id;
   }

   public Plane getPlane()
   {
      return this.plane;
   }
}
