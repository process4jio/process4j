package io.process4j.core;

import java.util.logging.Logger;

import io.process4j.core.annotations.Rules;

public abstract class BaseDecisionTableNode extends BaseActivityNode<BaseDecisionTable>
{
   private static final Logger LOG = Logger.getLogger(BaseDecisionTableNode.class.getName());
   private static final String MISSING_RULES = "Missing rules for decision table %s";

   @Override
   final void doInit() throws InitializationException
   {
      super.doInit();

      // Do not overwrite rules
      if (this.impl != null && this.impl.rules.isEmpty())
      {
         final Rules rulesAnnotation = this.getClass().getDeclaredAnnotation(Rules.class);
         if (rulesAnnotation != null)
         {
            try
            {
               this.impl.rules(rulesAnnotation.lexicographical(), rulesAnnotation.shortlex(), rulesAnnotation.resource(), rulesAnnotation.key());
            }
            catch (final Exception e)
            {
               throw new InitializationException(String.format(InitializationException.MESSAGE, this.getName()), e);
            }
         }

         // No rules
         if (this.impl.rules.isEmpty())
         {
            LOG.warning(String.format(MISSING_RULES, this.getName()));
         }
      }
   }

   @Override
   final State executeImpl(final State state)
   {
      state.setStarted(System.nanoTime());
      if (this.impl != null)
      {
         this.impl.execute(state.getBusinessData(), state.getProcessData(), state.getIteration());
      }
      return state.setPosition(this.getExit()).setCompleted(System.nanoTime());
   }
}
