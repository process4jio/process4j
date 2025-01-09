package io.process4j.core;

import java.util.Map;

public abstract class BaseGatewayNode extends BaseNode<Gateway>
{
   private static final String NULL_MESSAGE = "Gateway node %s returned a null flow";
   private static final String ID_PREFIX = "Gateway_";

   @Override
   final String getIdPrefix()
   {
      return ID_PREFIX;
   }

   Map<String, Flow> getExits()
   {
      return this.exits;
   }

   @Override
   final State executeImpl(final State state)
   {
      state.setStarted(System.nanoTime());
      final Flow position = this.exits.get(this.impl.execute(state.getBusinessData(), state.getProcessData(), state.getIteration()));
      if (position == null)
      {
         throw new NullPointerException(String.format(NULL_MESSAGE, this.getName()));
      }
      return state.setPosition(position).setCompleted(System.nanoTime());
   }
}
