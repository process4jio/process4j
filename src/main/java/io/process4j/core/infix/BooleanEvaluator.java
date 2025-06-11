package io.process4j.core.infix;

import java.util.Iterator;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;

public final class BooleanEvaluator extends AbstractEvaluator<String>
{
   private static final Operator IS = new Operator("?", 1, Operator.Associativity.RIGHT, 4);
   private static final Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 4);
   private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
   private static final Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);
   private static final Operator EQUALS = new Operator("==", 2, Operator.Associativity.LEFT, 3);
   private static final Operator NEQUALS = new Operator("!=", 2, Operator.Associativity.LEFT, 3);
   private static final Operator GT = new Operator(">", 2, Operator.Associativity.LEFT, 3);
   private static final Operator GTE = new Operator(">=", 2, Operator.Associativity.LEFT, 3);
   private static final Operator LT = new Operator("<", 2, Operator.Associativity.LEFT, 3);
   private static final Operator LTE = new Operator("<=", 2, Operator.Associativity.LEFT, 3);
   private static final Operator IN = new Operator("~", 2, Operator.Associativity.LEFT, 3);
   private static final Operator STARTS = new Operator("^=", 2, Operator.Associativity.LEFT, 3);
   private static final Operator CONTAINS = new Operator("*=", 2, Operator.Associativity.LEFT, 3);
   private static final Operator ENDS = new Operator("$=", 2, Operator.Associativity.LEFT, 3);

   private static final Parameters PARAMETERS;

   private static final Object TOKEN_NULL = "null";
   private static final Object TOKEN_TRUE = "true";
   private static final Object TOKEN_FALSE = "false";

   static
   {
      PARAMETERS = new Parameters();
      PARAMETERS.add(AND);
      PARAMETERS.add(OR);
      PARAMETERS.add(NEGATE);
      PARAMETERS.add(EQUALS);
      PARAMETERS.add(NEQUALS);
      PARAMETERS.add(GT);
      PARAMETERS.add(GTE);
      PARAMETERS.add(LT);
      PARAMETERS.add(LTE);
      PARAMETERS.add(IS);
      PARAMETERS.add(IN);
      PARAMETERS.add(STARTS);
      PARAMETERS.add(CONTAINS);
      PARAMETERS.add(ENDS);
   }

   private boolean lexicographical = true;
   private boolean shortlex = false;

   public BooleanEvaluator()
   {
      super(PARAMETERS);
   }

   public boolean isLexicographical()
   {
      return this.lexicographical;
   }

   public void setLexicographical(final boolean lexicographical)
   {
      this.lexicographical = lexicographical;
   }

   public boolean isShortlex()
   {
      return this.shortlex;
   }

   public void setShortlex(final boolean shortlex)
   {
      this.shortlex = shortlex;
   }

   @Override
   protected String toValue(final String literal, final Object evaluationContext)
   {
      return literal;
   }

   @Override
   protected String evaluate(final Operator operator, final Iterator<String> operands, final Object evaluationContext)
   {
      return String.valueOf(this.eval(operator, operands));
   }

   Object decode(final String json)
   {
      // Json Array, Json Object or Json Token 'null', 'true', 'false'
      if (json.startsWith("[") || json.startsWith("{") || json.equals(TOKEN_NULL) || json.equals(TOKEN_TRUE) || json.equals(TOKEN_FALSE))
      {
         return Json.decodeValue(json);
      }

      // Number
      try
      {
         Double.parseDouble(json);
         return Json.decodeValue(json);
      }

      // Json String
      catch (final NumberFormatException | DecodeException e)
      {
         return Json.decodeValue("\"" + json.replaceAll("^\"|\"$", "") + "\"");
      }
   }

   boolean eval(final Operator operator, final Iterator<String> operands)
   {
      String o1 = null;
      String o2 = null;

      if (operator == NEGATE)
      {
         o1 = operands.next();
         return !Boolean.parseBoolean(o1);
      }

      else if (operator == OR)
      {
         o1 = operands.next();
         o2 = operands.next();
         return Boolean.parseBoolean(o1) || Boolean.parseBoolean(o2);
      }

      else if (operator == AND)
      {
         o1 = operands.next();
         o2 = operands.next();
         return Boolean.parseBoolean(o1) && Boolean.parseBoolean(o2);
      }

      else if (operator == EQUALS)
      {
         o1 = operands.next();
         o2 = operands.next();
         final Object obj1 = this.decode(o1);
         final Object obj2 = this.decode(o2);
         return obj1 == null && obj2 == null ? true : obj1 != null && obj1.equals(obj2);
      }

      else if (operator == NEQUALS)
      {
         o1 = operands.next();
         o2 = operands.next();
         final Object obj1 = this.decode(o1);
         final Object obj2 = this.decode(o2);
         return !(obj1 == null && obj2 == null ? true : obj1 != null && obj1.equals(obj2));
      }

      else if (operator == GT)
      {
         o1 = operands.next();
         o2 = operands.next();
         final Object obj1 = this.decode(o1);
         final Object obj2 = this.decode(o2);
         if (obj1 instanceof Number && obj2 instanceof Number)
         {
            return ((Number) obj1).doubleValue() > ((Number) obj2).doubleValue();
         }
         else if (this.lexicographical)
         {
            return this.lexicographicallyGT(o1, o2);
         }
      }

      else if (operator == GTE)
      {
         o1 = operands.next();
         o2 = operands.next();
         final Object obj1 = this.decode(o1);
         final Object obj2 = this.decode(o2);
         if (obj1 instanceof Number && obj2 instanceof Number)
         {
            return ((Number) obj1).doubleValue() >= ((Number) obj2).doubleValue();
         }
         else if (this.lexicographical)
         {
            return this.lexicographicallyGTE(o1, o2);
         }
      }

      else if (operator == LT)
      {
         o1 = operands.next();
         o2 = operands.next();
         final Object obj1 = this.decode(o1);
         final Object obj2 = this.decode(o2);
         if (obj1 instanceof Number && obj2 instanceof Number)
         {
            return ((Number) obj1).doubleValue() < ((Number) obj2).doubleValue();
         }
         else if (this.lexicographical)
         {
            return this.lexicographicallyLT(o1, o2);
         }
      }

      else if (operator == LTE)
      {
         o1 = operands.next();
         o2 = operands.next();
         final Object obj1 = this.decode(o1);
         final Object obj2 = this.decode(o2);
         if (obj1 instanceof Number && obj2 instanceof Number)
         {
            return ((Number) obj1).doubleValue() <= ((Number) obj2).doubleValue();
         }
         else if (this.lexicographical)
         {
            return this.lexicographicallyLTE(o1, o2);
         }
      }

      else if (operator == IN)
      {
         o1 = operands.next();
         o2 = operands.next();

         final Object obj1 = this.decode(o1);
         final JsonArray arr2 = (JsonArray) this.decode(o2);

         if (arr2 == null)
         {
            return false;
         }

         final Iterator<?> iterator = arr2.iterator();
         while (iterator.hasNext())
         {
            final Object item = iterator.next();
            if (obj1 == null && item == null)
            {
               return true;
            }
            if (obj1 != null && obj1.equals(item))
            {
               return true;
            }
         }
         return false;
      }

      else if (operator == STARTS)
      {
         o1 = operands.next();
         o2 = operands.next();
         final String obj1 = (String) this.decode(o1);
         final String obj2 = (String) this.decode(o2);
         if (obj1 == null || obj2 == null)
         {
            return false;
         }
         return obj1.startsWith(obj2);
      }

      else if (operator == CONTAINS)
      {
         o1 = operands.next();
         o2 = operands.next();
         final String obj1 = (String) this.decode(o1);
         final String obj2 = (String) this.decode(o2);
         if (obj1 == null || obj2 == null)
         {
            return false;
         }
         return obj1.contains(obj2);
      }

      else if (operator == ENDS)
      {
         o1 = operands.next();
         o2 = operands.next();
         final String obj1 = (String) this.decode(o1);
         final String obj2 = (String) this.decode(o2);
         if (obj1 == null || obj2 == null)
         {
            return false;
         }
         return obj1.endsWith(obj2);
      }

      else if (operator == IS)
      {
         o1 = operands.next();
         return !o1.equals(TOKEN_NULL);
      }

      throw new RuntimeException(String.format("%s %s %s not supported!", o1, operator.getSymbol(), o2));
   }

   boolean lexicographicallyGT(final String o1, final String o2)
   {
      if (o1.equals(TOKEN_NULL))
      {
         return false;
      }

      if (o2.equals(TOKEN_NULL))
      {
         return true;
      }

      return this.shortlex && o1.length() != o2.length() ? o1.length() > o2.length() : o1.compareTo(o2) > 0;
   }

   boolean lexicographicallyLT(final String o1, final String o2)
   {
      if (o2.equals(TOKEN_NULL))
      {
         return false;
      }

      if (o1.equals(TOKEN_NULL))
      {
         return true;
      }

      return this.shortlex && o1.length() != o2.length() ? o1.length() < o2.length() : o1.compareTo(o2) < 0;
   }

   boolean lexicographicallyGTE(final String o1, final String o2)
   {
      if (o1.equals(o2))
      {
         return true;
      }

      return this.lexicographicallyGT(o1, o2);
   }

   boolean lexicographicallyLTE(final String o1, final String o2)
   {
      if (o1.equals(o2))
      {
         return true;
      }

      return this.lexicographicallyLT(o1, o2);
   }
}
