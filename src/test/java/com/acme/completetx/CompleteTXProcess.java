
package com.acme.completetx;

import io.process4j.core.BaseDecisionTableNode;
import io.process4j.core.BaseProcess;
import io.process4j.core.BaseTaskNode;
import io.process4j.core.Flow;
import io.process4j.core.annotations.Id;
import io.process4j.core.annotations.Implementation;
import io.process4j.core.annotations.Name;
import io.process4j.core.annotations.Rules;
import io.process4j.core.bpmn.annotations.BPMNEdge;
import io.process4j.core.bpmn.annotations.BPMNEdgeLabel;
import io.process4j.core.bpmn.annotations.BPMNEndEventShape;
import io.process4j.core.bpmn.annotations.BPMNNodeShape;
import io.process4j.core.bpmn.annotations.BPMNStartEventShape;

@BPMNEdge(flow = "Flow_13egodz", waypointCoordinates = "192,99;280,99")
@BPMNEdgeLabel(flow = "Flow_13egodz", x = "218", y = "81", width = "36", height = "14")
@BPMNEdge(flow = "Flow_1bvjg4t", waypointCoordinates = "570,99;672,99")
@BPMNEdgeLabel(flow = "Flow_1bvjg4t", x = "588", y = "66", width = "67", height = "27")
@BPMNEdge(flow = "Flow_0j0l32g", waypointCoordinates = "380,99;470,99")
@BPMNEdgeLabel(flow = "Flow_0j0l32g", x = "394", y = "66", width = "63", height = "27")
@BPMNStartEventShape(x = "156", y = "81")
@BPMNEndEventShape(x = "672", y = "81")
public class CompleteTXProcess
    extends BaseProcess
{


    public CompleteTXProcess() {
        super();
        CompleteTXProcess.DecideCurrency decideCurrency = new CompleteTXProcess.DecideCurrency();
        CompleteTXProcess.TruncateAmount truncateAmount = new CompleteTXProcess.TruncateAmount();
        this.addFlow(Flow.startFlow("Flow_13egodz", "START", decideCurrency));
        this.addFlow(Flow.endFlow("Flow_1bvjg4t", "AMOUNT TRUNCATED", truncateAmount));
        this.addFlow(Flow.flow("Flow_0j0l32g", "CURRENCY DECIDED", decideCurrency, truncateAmount));
    }

    @BPMNNodeShape(x = "280", y = "59")
    @Implementation("com.acme.completetx.impl.DecideCurrency")
    @Rules(resource = "rules/complete-tx.json", key = "Decide currency")
    @Id("Activity_1auceox")
    @Name("Decide currency")
    public final static class DecideCurrency
        extends BaseDecisionTableNode
    {


    }

    @BPMNNodeShape(x = "470", y = "59")
    @Implementation("com.acme.completetx.impl.TruncateAmount")
    @Id("Activity_1li072t")
    @Name("Truncate amount")
    public final static class TruncateAmount
        extends BaseTaskNode
    {


    }

}
