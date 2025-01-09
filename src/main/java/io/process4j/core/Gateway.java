package io.process4j.core;

import io.vertx.core.json.JsonObject;

public interface Gateway
{
   String execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration);
}
