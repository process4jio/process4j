package io.process4j.core.bpmn.model;

import java.util.List;

public interface NodeElement
{
   String getId();

   String getName();

   List<BPMNOutgoing> getOutgoing();

   List<BPMNIncoming> getIncoming();

   List<BPMNDocumentation> getDocumentation();
}
