package io.process4j.core;

public abstract class BaseProcessNode extends BaseActivityNode<BaseProcess>
{
   @Override
   final void doInit() throws InitializationException
   {
      super.doInit();
      if (this.impl != null)
      {
         this.impl.init();
      }
   }

   @Override
   final State executeImpl(final State state) throws ExecutionException
   {
      state.setStarted(System.nanoTime());
      if (this.impl != null)
      {
         final Execution execution = this.impl.execute(new State(this.getName(), this.impl.getDefaultPosition(), state.getBusinessData(), state.getProcessData(), state.getIteration()));
         state.setExecution(execution);
      }
      return state.setPosition(this.getExit()).setCompleted(System.nanoTime());
   }
}
