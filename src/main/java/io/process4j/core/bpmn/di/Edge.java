package io.process4j.core.bpmn.di;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Edge
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String bpmnElement;

   @XmlElement(name = "waypoint", namespace = "http://www.omg.org/spec/DD/20100524/DI")
   private final List<Waypoint> waypoints = new ArrayList<>();

   @XmlElement(name = "BPMNLabel", namespace = "http://www.omg.org/spec/BPMN/20100524/DI")
   private Label label;

   public Edge()
   {
   }

   public Edge(String bpmnElement, Label label, Waypoint... waypoints)
   {
      this.id = bpmnElement + "_di";
      this.bpmnElement = bpmnElement;
      this.label = label;
      this.waypoints.addAll(Arrays.asList(waypoints));
   }

   public String getId()
   {
      return this.id;
   }

   public String getBpmnElement()
   {
      return this.bpmnElement;
   }

   public List<Waypoint> getWaypoints()
   {
      return this.waypoints;
   }

   public Label getLabel()
   {
      return this.label;
   }

}
