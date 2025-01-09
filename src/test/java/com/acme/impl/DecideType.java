
package com.acme.impl;

import io.process4j.core.BaseDecisionTable;
import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.vertx.core.json.JsonObject;

public class DecideType
      extends BaseDecisionTable
{

   @Override
   public void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      processData.getData().put("type", result);
   }

}
