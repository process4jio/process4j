
package com.acme.completetx.impl;

import io.process4j.core.BaseDecisionTable;
import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.vertx.core.json.JsonObject;

public class DecideCurrency
      extends BaseDecisionTable
{

   @Override
   public void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      businessData.getJsonArray("transactions").getJsonObject(iteration.value()).put("currency", result);
   }

}
