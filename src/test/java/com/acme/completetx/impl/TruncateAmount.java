
package com.acme.completetx.impl;

import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.process4j.core.Task;
import io.vertx.core.json.JsonObject;

public class TruncateAmount
      implements Task
{

   @Override
   public void execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      final JsonObject tx = businessData.getJsonArray("transactions").getJsonObject(iteration.value());
      tx.put("amount", tx.getDouble("amount").intValue());
   }

}
