package io.process4j.core.bpmn.model;

import javax.xml.bind.annotation.XmlValue;

public class BPMNOutgoing
{
   @XmlValue
   private String value;

   public BPMNOutgoing()
   {
   }

   public BPMNOutgoing(String value)
   {
      this.value = value;
   }

   public String getValue()
   {
      return this.value;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (this.value == null ? 0 : this.value.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (this.getClass() != obj.getClass())
      {
         return false;
      }
      final BPMNOutgoing other = (BPMNOutgoing) obj;
      if (this.value == null)
      {
         if (other.value != null)
         {
            return false;
         }
      }
      else if (!this.value.equals(other.value))
      {
         return false;
      }
      return true;
   }

}
