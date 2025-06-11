package io.process4j.core.bpmn.model;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BPMNStartEvent implements NodeElement
{
   public static final String DEFAULT_ID = "StartEvent";
   public static final String DEFAULT_NAME = "Start";

   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "outgoing")
   private BPMNOutgoing outgoing;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "documentation")
   private List<BPMNDocumentation> documentation;

   public BPMNStartEvent()
   {
   }

   public BPMNStartEvent(final String id, final String name, final BPMNOutgoing outgoing)
   {
      this.id = id;
      this.name = name;
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

   @Override
   public List<BPMNOutgoing> getOutgoing()
   {
      return Collections.singletonList(this.outgoing);
   }

   @Override
   public List<BPMNIncoming> getIncoming()
   {
      return Collections.emptyList();
   }

   @Override
   public List<BPMNDocumentation> getDocumentation()
   {
      return this.documentation;
   }

}
