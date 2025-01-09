package io.process4j.core.bpmn.di;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Bounds
{
   @XmlAttribute
   private String x;

   @XmlAttribute
   private String y;

   @XmlAttribute
   private String width;

   @XmlAttribute
   private String height;

   public Bounds()
   {
   }

   public Bounds(String x, String y, String width, String height)
   {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public String getX()
   {
      return this.x;
   }

   public Bounds setX(String x)
   {
      this.x = x;
      return this;
   }

   public String getY()
   {
      return this.y;
   }

   public Bounds setY(String y)
   {
      this.y = y;
      return this;
   }

   public String getWidth()
   {
      return this.width;
   }

   public Bounds setWidth(String width)
   {
      this.width = width;
      return this;
   }

   public String getHeight()
   {
      return this.height;
   }

   public Bounds setHeight(String height)
   {
      this.height = height;
      return this;
   }
}
