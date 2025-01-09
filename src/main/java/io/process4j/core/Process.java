package io.process4j.core;

import io.vertx.core.json.JsonObject;

public interface Process
{
   Process init() throws InitializationException;

   Execution execute(final JsonObject businessData, final ProcessData processData) throws ExecutionException;

   Execution execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration) throws ExecutionException;
}
