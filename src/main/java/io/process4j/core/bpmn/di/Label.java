package io.process4j.core.bpmn.di;

import javax.xml.bind.annotation.XmlElement;

public class Label
{
   @XmlElement(name = "Bounds", namespace = "http://www.omg.org/spec/DD/20100524/DC")
   private Bounds bounds;

   public Label()
   {
   }

   public Label(Bounds bounds)
   {
      this.bounds = bounds;
   }

   public Bounds getBounds()
   {
      return this.bounds;
   }
}
