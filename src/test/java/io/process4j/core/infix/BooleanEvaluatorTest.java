package io.process4j.core.infix;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.process4j.TestUtil;
import io.process4j.core.BaseDecisionTable;
import io.process4j.core.Iteration;
import io.process4j.core.ProcessData;
import io.process4j.core.Rule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class BooleanEvaluatorTest
{
   private static final String BUSINESS_DATA = "data/businessdata.json";
   private static final boolean LEXICOGRAPHICAL = true;
   private static final boolean SHORTLEX = false;
   private static final String TRUE = Boolean.TRUE.toString();

   JsonObject businessData;

   BaseDecisionTable decisionTable = new BaseDecisionTable()
   {
      @Override
      public void apply(final String result, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
      {
         processData.getData().put("match", Boolean.parseBoolean(result));
      }
   };

   @BeforeAll
   static void setUpBeforeClass() throws Exception
   {

   }

   @BeforeEach
   void setUp() throws Exception
   {
      this.businessData = TestUtil.getJsonObjectResource(BUSINESS_DATA);
   }

   private void test(final Rule rule, final boolean matchExpected) throws Exception
   {
      this.test(LEXICOGRAPHICAL, SHORTLEX, rule, matchExpected);
   }

   private void test(final boolean lexicographical, final boolean shortlex, final Rule rule, final boolean matchExpected)
   {
      final ProcessData pd = new ProcessData();
      this.decisionTable.rules(lexicographical, shortlex, Collections.singletonList(rule)).execute(this.businessData, pd);
      if (matchExpected)
      {
         Assertions.assertTrue(pd.getData().getBoolean("match", false));
      }
      else
      {
         Assertions.assertFalse(pd.getData().getBoolean("match", false));
      }
   }

   @Test
   void testIs() throws Exception
   {
      this.test(new Rule("?/name", TRUE), true);
      this.test(new Rule("?/height", TRUE), true);
      this.test(new Rule("?/birthday", TRUE), true);
      this.test(new Rule("?/member", TRUE), true);
      this.test(new Rule("?/admin", TRUE), true);
      this.test(new Rule("?/notes", TRUE), false);
      this.test(new Rule("?/missing", TRUE), false);
      this.test(new Rule("?/birthday", TRUE), true);
   }

   @Test
   void testEquals() throws Exception
   {
      this.test(new Rule("/name == John Doe", TRUE), true);
      this.test(new Rule("/name == Jane Doe", TRUE), false);
      this.test(new Rule("/height == 1.83", TRUE), true);
      this.test(new Rule("/height == 1.830", TRUE), true);
      this.test(new Rule("/height == 1.8299999999999999", TRUE), false);
      this.test(new Rule("/height == 1.82999999999999999", TRUE), true); // Due to Double precision of 15-16 decimal points
      this.test(new Rule("/height == 1,83", TRUE), false);
      this.test(new Rule("/birthday == 1970-01-01", TRUE), true);
      this.test(new Rule("/birthday == 19700101", TRUE), false);
      this.test(new Rule("/birthday == 1970-1-1", TRUE), false);
      this.test(new Rule("/member == true", TRUE), true);
      this.test(new Rule("/member == false", TRUE), false);
      this.test(new Rule("/member == bogus", TRUE), false);
      this.test(new Rule("/member == 1", TRUE), false);
      this.test(new Rule("/admin == false", TRUE), true);
      this.test(new Rule("/admin == true", TRUE), false);
      this.test(new Rule("/admin == null", TRUE), false);
      this.test(new Rule("/notes == null", TRUE), true);
      this.test(new Rule("/notes == yadayada", TRUE), false);
      this.test(new Rule("/missing == null", TRUE), true);
      this.test(new Rule("/missing == 1", TRUE), false);
      this.test(new Rule("/birthmonth == 2003-01", TRUE), true);
   }

   @Test
   void testNequals() throws Exception
   {
      this.test(new Rule("/name != Jane Doe", TRUE), true);
      this.test(new Rule("/name != John Doe", TRUE), false);
      this.test(new Rule("/height != 1.0", TRUE), true);
      this.test(new Rule("/height != 1.83", TRUE), false);
      this.test(new Rule("/birthday != 1970-01-01", TRUE), false);
      this.test(new Rule("/birthday != 19700101", TRUE), true);
      this.test(new Rule("/birthday != 1970-1-1", TRUE), true);
      this.test(new Rule("/member != false", TRUE), true);
      this.test(new Rule("/member != true", TRUE), false);
      this.test(new Rule("/member != 1", TRUE), true);
      this.test(new Rule("/admin != true", TRUE), true);
      this.test(new Rule("/admin != false", TRUE), false);
      this.test(new Rule("/admin != null", TRUE), true);
      this.test(new Rule("/notes != yadayada", TRUE), true);
      this.test(new Rule("/notes != null", TRUE), false);
      this.test(new Rule("/missing != null", TRUE), false);
      this.test(new Rule("/missing != bogus", TRUE), true);
      this.test(new Rule("/birthmonth != 2004-01", TRUE), true);
   }

   /*
    * ASCII SORT ORDER
    *
    * ! ” # $ % & ' () * + , - ./ 0 1 2 3 4 5 6 7 8 9 :;< = >? @ A B C D E F G H I J K L M N O P Q R S T U V W X Y Z [\] ^ _ ` {|} ~
    */

   @Test
   void testGreaterThan() throws Exception
   {
      this.test(new Rule("/name > John Another Doe", TRUE), true);
      this.test(new Rule("/name > Quintus", TRUE), false);
      this.test(true, true, new Rule("/name > Quintus", TRUE), true);
      this.test(true, true, new Rule("/name > aaaaaaaaa", TRUE), false);
      this.test(new Rule("/height > 0.99", TRUE), true);
      this.test(new Rule("/height > 0", TRUE), true);
      this.test(new Rule("/height > -1.0", TRUE), true);
      this.test(new Rule("/height > -1000000", TRUE), true);
      this.test(new Rule("/height > 1.83", TRUE), false);
      this.test(new Rule("/height > 1.830", TRUE), false);
      this.test(new Rule("/height > 2", TRUE), false);
      this.test(new Rule("/birthday > 1970-01-01", TRUE), false);
      this.test(new Rule("/birthday > 19700101", TRUE), false);
      this.test(new Rule("/birthday > 1970-1-1", TRUE), false);
      this.test(true, true, new Rule("/birthday > 1970-1-1", TRUE), true);
      this.test(new Rule("/member > 0", TRUE), true); // 'true' is lexicographically greater than '0'
      this.test(new Rule("/admin > null", TRUE), true); // 'false' is lexicographically greater than 'null'
      this.test(new Rule("/notes > nul", TRUE), false); // 'null' is never greater than anything
      this.test(new Rule("/missing > null", TRUE), false); // 'null' is never greater than anything
      this.test(new Rule("/birthmonth > 2003-02", TRUE), false);
   }

   @Test
   void testGreaterThanOrEquals() throws Exception
   {
      this.test(new Rule("/name >= John Another Doe", TRUE), true);
      this.test(new Rule("/name >= Quintus", TRUE), false);
      this.test(true, true, new Rule("/name >= Quintus", TRUE), true);
      this.test(true, true, new Rule("/name >= aaaaaaaaa", TRUE), false);
      this.test(true, true, new Rule("/name >= John Doe", TRUE), true);
      this.test(new Rule("/name >= John Doe", TRUE), true);
      this.test(new Rule("/height >= 0.99", TRUE), true);
      this.test(new Rule("/height >= 0", TRUE), true);
      this.test(new Rule("/height >= -1.0", TRUE), true);
      this.test(new Rule("/height >= -1000000", TRUE), true);
      this.test(new Rule("/height >= 1.83", TRUE), true);
      this.test(new Rule("/height >= 1.830", TRUE), true);
      this.test(new Rule("/height >= 2", TRUE), false);
      this.test(new Rule("/birthday >= 1970-01-01", TRUE), true);
      this.test(new Rule("/birthday >= 19700101", TRUE), false);
      this.test(new Rule("/birthday >= 1970-1-1", TRUE), false);
      this.test(true, true, new Rule("/birthday >= 1970-1-1", TRUE), true);
      this.test(new Rule("/member >= 0", TRUE), true); // 'true' is lexicographically greater than '0'
      this.test(new Rule("/admin >= null", TRUE), true); // 'false' is lexicographically greater than 'null'
      this.test(new Rule("/notes >= nul", TRUE), false); // 'null' is never greater than anything
      this.test(new Rule("/notes >= null", TRUE), true); // 'null' is equal to 'null'
      this.test(new Rule("/missing >= null", TRUE), true); // 'null' is equal to 'null'
      this.test(new Rule("/birthmonth >= 2003-01", TRUE), true);
   }

   @Test
   void testLessThan() throws Exception
   {
      this.test(new Rule("/name < John Another Doe", TRUE), false);
      this.test(new Rule("/name < Quintus", TRUE), true);
      this.test(true, true, new Rule("/name < Quintus", TRUE), false);
      this.test(true, true, new Rule("/name < aaaaaaaaa", TRUE), true);
      this.test(new Rule("/height < 0.99", TRUE), false);
      this.test(new Rule("/height < 0", TRUE), false);
      this.test(new Rule("/height < -1.0", TRUE), false);
      this.test(new Rule("/height < -1000000", TRUE), false);
      this.test(new Rule("/height < 1.83", TRUE), false);
      this.test(new Rule("/height < 1.830", TRUE), false);
      this.test(new Rule("/height < 2", TRUE), true);
      this.test(new Rule("/birthday < 1970-01-01", TRUE), false);
      this.test(new Rule("/birthday < 19700101", TRUE), true);
      this.test(new Rule("/birthday < 1970-1-1", TRUE), true);
      this.test(true, true, new Rule("/birthday < 1970-1-1", TRUE), false);
      this.test(new Rule("/member < 0", TRUE), false); // 'true' is lexicographically greater than '0'
      this.test(new Rule("/admin < null", TRUE), false); // 'false' is lexicographically greater than 'null'
      this.test(new Rule("/notes < nul", TRUE), true); // anything is greater than 'null'
      this.test(new Rule("/missing < null", TRUE), false); // anything is never less than 'null'
      this.test(new Rule("/birthmonth < 2003-01", TRUE), false);
   }

   @Test
   void testLessThanOrEquals() throws Exception
   {
      this.test(new Rule("/name <= John Another Doe", TRUE), false);
      this.test(new Rule("/name <= Quintus", TRUE), true);
      this.test(true, true, new Rule("/name <= Quintus", TRUE), false);
      this.test(true, true, new Rule("/name <= aaaaaaaaa", TRUE), true);
      this.test(true, true, new Rule("/name <= John Doe", TRUE), true);
      this.test(new Rule("/name <= John Doe", TRUE), true);
      this.test(new Rule("/height <= 0.99", TRUE), false);
      this.test(new Rule("/height <= 0", TRUE), false);
      this.test(new Rule("/height <= -1.0", TRUE), false);
      this.test(new Rule("/height <= -1000000", TRUE), false);
      this.test(new Rule("/height <= 1.83", TRUE), true);
      this.test(new Rule("/height <= 1.830", TRUE), true);
      this.test(new Rule("/height <= 2", TRUE), true);
      this.test(new Rule("/birthday <= 1970-01-01", TRUE), true);
      this.test(new Rule("/birthday <= 19700101", TRUE), true);
      this.test(new Rule("/birthday <= 1970-1-1", TRUE), true);
      this.test(true, true, new Rule("/birthday <= 1970-1-1", TRUE), false);
      this.test(new Rule("/member <= 0", TRUE), false); // 'true' is lexicographically greater than '0'
      this.test(new Rule("/admin <= null", TRUE), false); // 'false' is lexicographically greater than 'null'
      this.test(new Rule("/notes <= nul", TRUE), true); // 'null' is never greater than anything
      this.test(new Rule("/notes <= null", TRUE), true); // 'null' is equal to 'null'
      this.test(new Rule("/missing <= null", TRUE), true); // 'null' is equal to 'null'
      this.test(new Rule("/birthmonth <= 2003-01", TRUE), true);
   }

   @Test
   void testIN() throws Exception
   {
      this.test(new Rule("gold ~ /strings", TRUE), true);
      this.test(new Rule("/strings/0 ~ [\"silver\",\"bronze\"]", TRUE), true);

      this.test(new Rule("34 ~ /numbers", TRUE), true);
      this.test(new Rule("12 ~ /numbers", TRUE), false);
      this.test(new Rule("12.0 ~ /numbers", TRUE), true);
      this.test(new Rule("\"155\" ~ [\"156\",\"1555\",\"155\"]", TRUE), true);
      this.test(new Rule("\"155\" ~ [\"156\",\"1555\",\"0155\"]", TRUE), false);
      this.test(new Rule("0155 ~ [\"156\",\"1555\",\"0155\"]", TRUE), true);
      this.test(new Rule("\"0155\" ~ [\"156\",\"1555\",\"0155\"]", TRUE), true);
      this.test(new Rule("\"155\" ~ [156,1555,155]", TRUE), false);

      final String jsonObject = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":100,\"anotherAmount\":200.1,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject), TRUE), true);

      final String jsonObject2 = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":100.0,\"anotherAmount\": 200.1,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject2), TRUE), true);

      final String jsonObject3 = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":100.9,\"anotherAmount\": 200.1,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject3), TRUE), true);

      final String jsonObject4 = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":101,\"anotherAmount\": 200.1,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject4), TRUE), false);

      final String jsonObject5 = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":101.0,\"anotherAmount\": 200.1,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject5), TRUE), false);

      final String jsonObject6 = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":100,\"anotherAmount\": 200,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject6), TRUE), true);

      final String jsonObject7 = "{\"id\":\"374b53af-e69b-3e18-bb02-ef0efffd2b25\",\"amount\":100,\"anotherAmount\": 200.11,\"name\":\"Fum\"}";
      this.test(new Rule(String.format("%s ~ /objects", jsonObject7), TRUE), false);

   }

   @Test
   void testStarts() throws Exception
   {
      this.test(new Rule("/objects/0/name ^= F", TRUE), true);
      this.test(new Rule("/objects/0/name ^= f", TRUE), false);
      this.test(new Rule("/objects/0/name ^= Fo", TRUE), true);
      this.test(new Rule("/objects/0/name ^= Foo", TRUE), true);
      this.test(new Rule("/notes ^= Foo", TRUE), false);
      this.test(new Rule("/notes ^= null", TRUE), false);
   }

   @Test
   void testContains() throws Exception
   {
      this.test(new Rule("/objects/0/name *= F", TRUE), true);
      this.test(new Rule("/objects/0/name *= f", TRUE), false);
      this.test(new Rule("/objects/0/name *= oo", TRUE), true);
      this.test(new Rule("/objects/0/name *= Fo", TRUE), true);
      this.test(new Rule("/objects/0/name *= Foo", TRUE), true);
      this.test(new Rule("/objects/0/name *= oo", TRUE), true);
      this.test(new Rule("/notes *= Foo", TRUE), false);
      this.test(new Rule("/notes *= null", TRUE), false);
   }

   @Test
   void testEnds() throws Exception
   {
      this.test(new Rule("/objects/0/name $= F", TRUE), false);
      this.test(new Rule("/objects/0/name $= o", TRUE), true);
      this.test(new Rule("/objects/0/name $= O", TRUE), false);
      this.test(new Rule("/objects/0/name $= Foo", TRUE), true);
      this.test(new Rule("/objects/0/name $= Fo", TRUE), false);
      this.test(new Rule("/notes $= Foo", TRUE), false);
      this.test(new Rule("/notes $= null", TRUE), false);
   }

   @Test
   void testNegate() throws Exception
   {
      // Negate IS
      this.test(new Rule("!?/name", TRUE), false);
      this.test(new Rule("!?/height", TRUE), false);
      this.test(new Rule("!?/birthday", TRUE), false);
      this.test(new Rule("!?/member", TRUE), false);
      this.test(new Rule("!?/admin", TRUE), false);
      this.test(new Rule("!?/notes", TRUE), true);
      this.test(new Rule("!?/missing", TRUE), true);
      this.test(new Rule("!?/birthmonth", TRUE), false);

   }

   @Test
   void testAnd() throws Exception
   {
      this.test(new Rule("/name == John Doe && /height == 1.83 && /member && /birthday == 1970-01-01", TRUE), true);
      this.test(new Rule("/name == John Doe && /height == 1.83 && /admin", TRUE), false);
      this.test(new Rule("/name == John Doe && /height == 1.83 && !/admin", TRUE), true);
      this.test(new Rule("/name == John Doe && /height == 1.83 && !/admin && /member && /notes == null && !?/missing", TRUE), true);
   }

   @Test
   void testOr() throws Exception
   {
      this.test(new Rule("/name == John Doe || /name == Jane Doe", TRUE), true);
      this.test(new Rule("/name == Jane Doe || /name != Jane Doe", TRUE), true);
      this.test(new Rule("/name == Jane Doe || /name != John Doe || /birthday < 20-10-01", TRUE), true);
      this.test(new Rule("/name == Jane Doe || /height == 1.83 || /admin", TRUE), true);
      this.test(new Rule("/name == Jane Doe || /height == 1,83 || null > null || /notes", TRUE), false);
   }

   @Test
   void testUnsupported()
   {
      Assertions.assertThrows(RuntimeException.class, () -> {
         this.test(false, true, new Rule("/name <= fum", TRUE), true);
      });
   }

   @Test
   void testDecode()
   {
      final BooleanEvaluator evaluator = new BooleanEvaluator();

      String json = "Foo";
      Object obj = evaluator.decode(json);
      Assertions.assertTrue(obj instanceof String);

      json = "1";
      obj = evaluator.decode(json);
      Assertions.assertTrue(obj instanceof Integer);

      json = "1.0";
      obj = evaluator.decode(json);
      Assertions.assertTrue(obj instanceof Double);

      json = "2021-11-02";
      obj = evaluator.decode(json);
      Assertions.assertTrue(obj instanceof String);

      json = "{\"name\": \"John Doe\", \"age\": 40}";
      obj = evaluator.decode(json);
      Assertions.assertTrue(obj instanceof JsonObject);

      json = "[{\"name\": \"John Doe\", \"age\": 40},1,1.0,\"Foo\"]";
      obj = evaluator.decode(json);
      Assertions.assertTrue(obj instanceof JsonArray);
   }
}
