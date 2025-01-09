package io.process4j.core;

import io.vertx.core.json.JsonObject;

public interface Task
{
   void execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration);
}
