package io.process4j.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.process4j.TestUtil;
import io.vertx.core.json.JsonObject;

class BaseDecisionTableTest
{
   static String EXPRESSION = "/transactions/${i}/amount > 251";
   static String RESULT = "/transactions/${i}/typ";

   static String EXPRESSION2 = "/transactions/${i}/amount > 200";
   static String RESULT2 = "/transactions/${i}/typ";

   List<Rule> rules;

   @BeforeEach
   void setUp() throws Exception
   {
      this.rules = new ArrayList<>();
      this.rules.add(new Rule(EXPRESSION, RESULT));
      this.rules.add(new Rule(EXPRESSION2, RESULT2));
   }

   @Test
   void testWithArrayItems() throws URISyntaxException, IOException
   {
      final JsonObject foo = TestUtil.getJsonObjectResource("data/businessdata.02.json");
      final DecisionTable table = new FooTable();
      table.rules(true, false, this.rules);

      for (int i = 0; i < foo.getJsonArray("transactions").size(); ++i)
      {
         final ProcessData pd = new ProcessData();
         table.execute(foo, pd, Iteration.nr(i));
         Assertions.assertEquals("/transactions/" + i + "/typ", pd.getData().getString("result"));
      }
   }

   @Test
   void testWithObjectEntries() throws URISyntaxException, IOException
   {
      final JsonObject fum = TestUtil.getJsonObjectResource("data/businessdata.03.json");
      final DecisionTable table = new FumTable();
      table.rules(true, false, this.rules);

      for (int i = 0; i < fum.getJsonObject("transactions").size(); ++i)
      {
         final ProcessData pd = new ProcessData();
         table.execute(fum, pd, Iteration.nr(i));
         if (i != 1)
         {
            Assertions.assertEquals("/transactions/tx" + (i + 1) + "/typ", pd.getData().getString("result"));
         }
         else
         {
            Assertions.assertNull(pd.getData().getString("result"));
         }
      }
   }

   private static class FooTable extends BaseDecisionTable
   {

      @Override
      public void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
      {
         processData.getData().put("result", result);
      }
   }

   private static class FumTable extends BaseDecisionTable
   {
      @Override
      public void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
      {
         processData.getData().put("result", result);
      }

      @Override
      public Rule recompile(final Rule rule, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
      {
         final String substitute = businessData.getJsonObject("transactions").stream().collect(Collectors.toList()).get(iteration.value()).getKey();
         final String expression = rule.getExpression().replaceAll(Rule.VARIABLE_REGEX, substitute);
         final String result = rule.getResult().replaceAll(Rule.VARIABLE_REGEX, substitute);
         return new Rule(expression, result);
      }
   }
}
