package io.process4j.core.bpmn.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BPMNEndEvent implements NodeElement
{
   public static final String DEFAULT_ID = "EndEvent";
   public static final String DEFAULT_NAME = "End";

   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "incoming")
   private final List<BPMNIncoming> incoming = new ArrayList<>();

   @XmlElement(namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", name = "documentation")
   private List<BPMNDocumentation> documentation;

   public BPMNEndEvent()
   {
   }

   public BPMNEndEvent(final String id, final String name, final BPMNIncoming... incoming)
   {
      this.id = id;
      this.name = name;
      this.incoming.addAll(Arrays.asList(incoming));
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
   public List<BPMNIncoming> getIncoming()
   {
      return this.incoming;
   }

   @Override
   public List<BPMNOutgoing> getOutgoing()
   {
      return Collections.emptyList();
   }

   @Override
   public List<BPMNDocumentation> getDocumentation()
   {
      return this.documentation;
   }

}
