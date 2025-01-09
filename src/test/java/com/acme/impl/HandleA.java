
package com.acme.impl;

import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.process4j.core.Task;
import io.vertx.core.json.JsonObject;

public class HandleA
      implements Task
{

   @Override
   public void execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      processData.getData().put("isAHandled", true);
   }

}
