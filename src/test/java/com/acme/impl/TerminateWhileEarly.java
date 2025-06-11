
package com.acme.impl;

import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.process4j.core.Task;
import io.process4j.core.annotations.Generated;
import io.vertx.core.json.JsonObject;

@Generated(from = "com.acme.acme.bpmn", with = "io.process4j.core-n/a", timestamp = "2022-09-19 17:09:55")
public class TerminateWhileEarly
      implements Task
{

   @Override
   public void execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      if (iteration.value() == 7)
      {
         iteration.terminate();
      }
   }

}
