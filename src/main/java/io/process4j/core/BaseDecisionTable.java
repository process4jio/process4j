package io.process4j.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.process4j.core.infix.BooleanEvaluator;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class BaseDecisionTable implements DecisionTable
{
   private static final Logger LOG = Logger.getLogger(BaseDecisionTable.class.getName());

   private static final String RESULT_ATTR = "result";
   private static final String EXPRESSION_ATTR = "expression";

   private final BooleanEvaluator evaluator = new BooleanEvaluator();

   protected final List<Rule> rules = new ArrayList<>();

   @Override
   public final BaseDecisionTable rules(final boolean lexicographical, final boolean shortlex, final String resource, final String key) throws IOException
   {
      return this.rules(lexicographical, shortlex, this.getRulesFromResource(resource, key));
   }

   @Override
   public final BaseDecisionTable rules(final boolean lexicographical, final boolean shortlex, final List<Rule> rules)
   {
      this.evaluator.setLexicographical(lexicographical);
      this.evaluator.setShortlex(shortlex);

      this.rules.clear();
      if (rules != null)
      {
         this.rules.addAll(rules);
      }
      return this;
   }

   @Override
   public abstract void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration);

   /**
    * Default rule re-compilation inserting the current iteration value in Rule.VARIABLE_REGEX occurrences
    *
    * Example (iteration 3):
    *
    * Original rule expression: '/items/${i}/price > 1000', original rule result: '/items/${i}/id'
    *
    * Re-compiled rule expression: '/items/3/price > 1000', re-compiled rule result: '/items/3/id'
    *
    * Implementors can override this method in order to insert something else
    *
    * Example (iteration 3):
    *
    * Original rule expression: '/items/${i}/price > 1000', original rule result: '/items/${i}/id'
    *
    * Re-compiled rule expression: '/items/item3/price > 1000', re-compiled rule result: '/items/item3/id'
    *
    */
   @Override
   public Rule recompile(final Rule rule, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      final String expression = rule.getExpression().replaceAll(Rule.VARIABLE_REGEX, String.valueOf(iteration.value()));
      final String result = rule.getResult().replaceAll(Rule.VARIABLE_REGEX, String.valueOf(iteration.value()));
      return new Rule(expression, result);
   }

   @Override
   public final void execute(final JsonObject businessData, final ProcessData processData)
   {
      this.execute(businessData, processData, Iteration.nr(0));
   }

   @Override
   public final void execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      if (this.rules.isEmpty())
      {
         LOG.warning(String.format("Executing decision table %s without rules!", this.getClass().getSimpleName()));
      }
      for (final Rule rule : this.rules)
      {
         // Recompile expression and result with current iteration
         final Rule effectiveRule = this.recompile(rule, businessData, processData, iteration);

         if (effectiveRule.match(this.evaluator, businessData, processData))
         {
            this.apply(effectiveRule.result(businessData, processData), businessData, processData, iteration);
            break; // return on first match
         }
      }
   }

   private final List<Rule> getRulesFromResource(final String resource, final String key) throws IOException
   {
      try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
      {
         if (is == null)
         {
            return null;
         }

         final ByteArrayOutputStream baos = new ByteArrayOutputStream();

         int nRead;
         final byte[] data = new byte[4];

         while ((nRead = is.read(data, 0, data.length)) != -1)
         {
            baos.write(data, 0, nRead);
         }

         baos.flush();

         final Buffer buffer = Buffer.buffer(baos.toByteArray());

         // Note: No need to ensure closing of baos since closing a ByteArrayOutputStream has no effect

         final JsonArray rules = buffer.toJsonObject().getJsonArray(key);

         return rules != null ? rules.stream().map(JsonObject::mapFrom).map(json -> new Rule(json.getString(EXPRESSION_ATTR), json.getString(RESULT_ATTR)))
               .collect(Collectors.toList()) : Collections.emptyList();
      }
   }

}
