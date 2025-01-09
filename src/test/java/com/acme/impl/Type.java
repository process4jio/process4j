
package com.acme.impl;

import io.process4j.core.Gateway;
import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.vertx.core.json.JsonObject;

public class Type
      implements Gateway
{

   @Override
   public String execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      return processData.getData().getString("type");
   }

}
