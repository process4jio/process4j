package io.process4j.core;

import java.io.IOException;
import java.util.List;

import io.vertx.core.json.JsonObject;

public interface DecisionTable
{
   BaseDecisionTable rules(final boolean lexicographical, final boolean shortlex, final String resource, final String key) throws IOException;

   BaseDecisionTable rules(final boolean lexicographical, final boolean shortlex, final List<Rule> rules);

   void execute(final JsonObject businessData, final ProcessData processData);

   void execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration);

   void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration);

   Rule recompile(final Rule rule, final JsonObject businessData, final ProcessData processData, final Iteration iteration);
}
