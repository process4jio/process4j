package io.process4j.core.bpmn.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class BPMNExclusiveGateway implements NodeElement
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "incoming")
   private List<BPMNIncoming> incoming;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "outgoing")
   private List<BPMNOutgoing> outgoing;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "documentation")
   List<BPMNDocumentation> documentation;

   public BPMNExclusiveGateway()
   {
   }

   public BPMNExclusiveGateway(final String id, final String name, final List<BPMNIncoming> incoming, final List<BPMNOutgoing> outgoing)
   {
      this.id = id;
      this.name = name;
      this.incoming = incoming;
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
   public BPMNExclusiveGateway setName(final String name)
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
      return this.outgoing;
   }

   public List<BPMNDocumentation> getDocumentation()
   {
      return this.documentation;
   }

}
