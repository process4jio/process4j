package io.process4j.core.bpmn.di;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Plane
{
   public static final String DEFAULT_ID = "BPMNPlane";

   @XmlAttribute
   private String id;

   @XmlAttribute
   private String bpmnElement;

   @XmlElement(name = "BPMNShape", namespace = "http://www.omg.org/spec/BPMN/20100524/DI")
   private final List<Shape> shapes = new ArrayList<>();

   @XmlElement(name = "BPMNEdge", namespace = "http://www.omg.org/spec/BPMN/20100524/DI")
   private final List<Edge> edges = new ArrayList<>();

   public Plane()
   {
   }

   public Plane(String id, String bpmnElement, List<Shape> shapes, List<Edge> edges)
   {
      this.id = id;
      this.bpmnElement = bpmnElement;
      this.shapes.addAll(shapes);
      this.edges.addAll(edges);
   }

   public String getId()
   {
      return this.id;
   }

   public String getBpmnElement()
   {
      return this.bpmnElement;
   }

   public List<Shape> getShapes()
   {
      return this.shapes;
   }

   public List<Edge> getEdges()
   {
      return this.edges;
   }
}
