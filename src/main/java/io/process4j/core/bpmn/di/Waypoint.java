package io.process4j.core.bpmn.di;

import javax.xml.bind.annotation.XmlAttribute;

public class Waypoint
{
   @XmlAttribute
   private String x;

   @XmlAttribute
   private String y;

   public Waypoint()
   {
   }

   public Waypoint(String x, String y)
   {
      this.x = x;
      this.y = y;
   }

   public String getX()
   {
      return this.x;
   }

   public String getY()
   {
      return this.y;
   }

}
