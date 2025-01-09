package io.process4j.core.bpmn.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class BPMNTask extends BPMNActivity
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "incoming")
   private final List<BPMNIncoming> incoming = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "outgoing")
   private BPMNOutgoing outgoing;

   public BPMNTask()
   {
   }

   public BPMNTask(String id, String name, BPMNOutgoing outgoing, BPMNIncoming... incoming)
   {
      this.id = id;
      this.name = name;
      this.incoming.addAll(Arrays.asList(incoming));
      this.outgoing = outgoing;
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public String getName()
   {
      return this.name;
   }

   @XmlTransient
   public BPMNTask setName(String name)
   {
      this.name = name;
      return this;
   }

   @Override
   public List<BPMNIncoming> getIncoming()
   {
      return this.incoming;
   }

   @Override
   public List<BPMNOutgoing> getOutgoing()
   {
      return Collections.singletonList(this.outgoing);
   }
}
