package io.process4j.core;

public abstract class BaseTaskNode extends BaseActivityNode<Task>
{
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
